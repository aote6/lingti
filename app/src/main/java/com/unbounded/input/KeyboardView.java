// 键盘View：持有Renderer和GestureController，管理剪贴板面板状态、布局编辑模式和布局槽位切换
package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutManager;
import com.unbounded.input.core.layout.LayoutProfile;

public class KeyboardView extends View implements KeyboardGestureController.SessionAccess {
    public interface SlotSwitchListener {
        void onSwitchSlot(int slot);
    }

    private final KeyboardRenderer renderer = new KeyboardRenderer();
    private final KeyboardGestureController gestureController;
    private final LayoutManager layoutManager = new LayoutManager();

    private float candidateBarHeight;
    private float controlBarHeight;
    private float dpScale = 1f;
    private boolean clipboardPanelOpen = false;

    private boolean editMode = false;
    private KeyModel dragKey = null;
    private Rect editButtonRect = new Rect();
    private Rect saveButtonRect = new Rect();
    private Rect restoreButtonRect = new Rect();
    private Rect[] slotButtonRects = new Rect[3];
    private final String layoutFileName;
    private final String layoutStateName;
    private final Runnable onRestore;
    private final int activeSlot;
    private final SlotSwitchListener slotSwitchListener;

    private String flashMessage = null;
    private long flashUntil = 0;

    public enum InputMode { CHINESE, ENGLISH, TERMINAL }
    private InputMode inputMode = InputMode.TERMINAL;

    public KeyboardView(Context context, final KeyboardActionDispatcher dispatcher, final LayoutProfile profile,
                        String layoutFileName, String layoutStateName, Runnable onRestore,
                        int activeSlot, SlotSwitchListener slotSwitchListener) {
        super(context);
        this.layoutFileName = layoutFileName;
        this.layoutStateName = layoutStateName;
        this.onRestore = onRestore;
        this.activeSlot = activeSlot;
        this.slotSwitchListener = slotSwitchListener;
        dpScale = getResources().getDisplayMetrics().density;
        List<KeyModel> allKeys = profile.allKeys();
        layoutManager.setLayout(new KeyboardLayout() {
            public String id() { return "inline"; }
            public LayoutProfile build() { return profile; }
        }, getWidth(), getHeight());
        gestureController = new KeyboardGestureController(allKeys, dispatcher, this);
        for (int i = 0; i < slotButtonRects.length; i++) slotButtonRects[i] = new Rect();
    }

    public float getDpScale() { return dpScale; }
    public Context getKeyboardContext() { return getContext(); }

    public void setInputMode(InputMode mode) {
        this.inputMode = mode;
    }
    public InputMode getInputMode() { return inputMode; }

    @Override public void invalidateView() { invalidate(); }

    public void openClipboardPanel() {
        clipboardPanelOpen = true;
        invalidate();
    }

    public void closeClipboardPanel() {
        clipboardPanelOpen = false;
        invalidate();
    }

    public void resetSession() {
        gestureController.reset();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        controlBarHeight = 36 * dpScale;
        candidateBarHeight = controlBarHeight;
        layoutManager.setCandidateBarHeight(candidateBarHeight);
        layoutManager.setSize(w, h);

        int btnH = Math.round(controlBarHeight - 6 * dpScale);
        int btnW = Math.round(56 * dpScale);
        int gap = Math.round(6 * dpScale);
        int topY = Math.round(3 * dpScale);
        int rightX = w - Math.round(6 * dpScale);
        editButtonRect.set(rightX - btnW, topY, rightX, topY + btnH);
        saveButtonRect.set(rightX - btnW * 2 - gap, topY, rightX - btnW - gap, topY + btnH);
        restoreButtonRect.set(rightX - btnW * 3 - gap * 2, topY, rightX - btnW * 2 - gap * 2, topY + btnH);

        // 槽位切换按钮放在控制栏左侧，常驻显示，和右侧编辑组不冲突。
        int slotBtnW = Math.round(36 * dpScale);
        int leftX = Math.round(6 * dpScale);
        for (int i = 0; i < slotButtonRects.length; i++) {
            int sx = leftX + i * (slotBtnW + gap);
            slotButtonRects[i].set(sx, topY, sx + slotBtnW, topY + btnH);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        LayoutProfile profile = layoutManager.getProfile();
        renderer.drawKeyboard(canvas, profile, candidateBarHeight,
                gestureController.getActiveKey(), gestureController.isLongPressed());
        if (clipboardPanelOpen) {
            renderer.drawClipboardPopup(canvas, com.unbounded.input.SimpleImeService.getClipboardHistory());
        }
        drawSlotButtons(canvas);
        drawEditControls(canvas);
        drawFlashMessage(canvas);
    }

    private void drawSlotButtons(Canvas canvas) {
        Paint bg = ThemeTokens.newBgPaint();
        Paint border = ThemeTokens.newBorderPaint();
        border.setColor(ThemeTokens.BORDER);
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);

        for (int i = 0; i < slotButtonRects.length; i++) {
            int slotNum = i + 1;
            boolean isActive = slotNum == activeSlot;
            bg.setColor(isActive ? ThemeTokens.BORDER_ACTIVE : ThemeTokens.SURFACE);
            canvas.drawRect(slotButtonRects[i], bg);
            canvas.drawRect(slotButtonRects[i], border);
            text.setColor(ThemeTokens.TEXT_PRIMARY);
            drawCenteredText(canvas, slotButtonRects[i], String.valueOf(slotNum), text);
        }
    }

    private void drawEditControls(Canvas canvas) {
        Paint bg = ThemeTokens.newBgPaint();
        Paint border = ThemeTokens.newBorderPaint();
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);
        text.setColor(ThemeTokens.TEXT_PRIMARY);

        bg.setColor(editMode ? ThemeTokens.BORDER_ACTIVE : ThemeTokens.SURFACE);
        canvas.drawRect(editButtonRect, bg);
        border.setColor(ThemeTokens.BORDER);
        canvas.drawRect(editButtonRect, border);
        drawCenteredText(canvas, editButtonRect, editMode ? "退出" : "编辑", text);

        if (editMode) {
            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(saveButtonRect, bg);
            canvas.drawRect(saveButtonRect, border);
            drawCenteredText(canvas, saveButtonRect, "保存", text);

            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(restoreButtonRect, bg);
            canvas.drawRect(restoreButtonRect, border);
            drawCenteredText(canvas, restoreButtonRect, "还原", text);
        }
    }

    private void drawCenteredText(Canvas canvas, Rect rect, String label, Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = rect.centerY() - (fm.ascent + fm.descent) / 2;
        float textX = rect.centerX() - paint.measureText(label) / 2;
        canvas.drawText(label, textX, textY, paint);
    }

    private void showFlash(String message) {
        flashMessage = message;
        flashUntil = System.currentTimeMillis() + 1200;
        invalidate();
        postDelayed(new Runnable() {
            @Override public void run() { invalidate(); }
        }, 1300);
    }

    private void drawFlashMessage(Canvas canvas) {
        if (flashMessage == null) return;
        if (System.currentTimeMillis() >= flashUntil) {
            flashMessage = null;
            return;
        }
        Paint boxPaint = ThemeTokens.newBgPaint();
        boxPaint.setColor(ThemeTokens.SURFACE_RAISED);
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(18f);
        textPaint.setColor(ThemeTokens.TEXT_PRIMARY);

        float textWidth = textPaint.measureText(flashMessage);
        float boxW = textWidth + 40 * dpScale;
        float boxH = 40 * dpScale;
        float boxX = (getWidth() - boxW) / 2f;
        float boxY = candidateBarHeight + 12 * dpScale;

        canvas.drawRect(boxX, boxY, boxX + boxW, boxY + boxH, boxPaint);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = boxY + boxH / 2 - (fm.ascent + fm.descent) / 2;
        canvas.drawText(flashMessage, boxX + 20 * dpScale, textY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clipboardPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                java.util.List<String> history = com.unbounded.input.SimpleImeService.getClipboardHistory();
                int count = history.size();
                int visibleIdx = renderer.hitTestClipboardItem(getHeight(), count, event.getY());
                if (visibleIdx >= 0) {
                    int realIndex = count - 1 - visibleIdx;
                    android.inputmethodservice.InputMethodService svc = (android.inputmethodservice.InputMethodService) getContext();
                    if (svc instanceof com.unbounded.input.SimpleImeService) {
                        ((com.unbounded.input.SimpleImeService) svc).pasteClipboardItem(realIndex);
                    }
                }
                closeClipboardPanel();
            }
            return true;
        }

        int x = (int) event.getX(), y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < slotButtonRects.length; i++) {
                if (slotButtonRects[i].contains(x, y)) {
                    int slotNum = i + 1;
                    if (slotNum != activeSlot && slotSwitchListener != null) {
                        slotSwitchListener.onSwitchSlot(slotNum);
                    }
                    return true;
                }
            }
            if (editButtonRect.contains(x, y)) {
                editMode = !editMode;
                if (editMode) layoutManager.convertAllToPercent();
                dragKey = null;
                invalidate();
                return true;
            }
            if (editMode && saveButtonRect.contains(x, y)) {
                RuleLoader.save(getContext(), layoutManager.getProfile(), layoutFileName, layoutStateName);
                showFlash("已保存到槽位 " + activeSlot);
                return true;
            }
            if (editMode && restoreButtonRect.contains(x, y)) {
                if (onRestore != null) onRestore.run();
                showFlash("槽位 " + activeSlot + " 已还原");
                return true;
            }
        }

        if (editMode) {
            return handleEditTouch(event, x, y);
        }
        return gestureController.onTouchEvent(event);
    }

    private boolean handleEditTouch(MotionEvent event, int x, int y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragKey = findTopmostKey(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragKey == null) return true;
                float touchXPercent = x * 100f / getWidth();
                float touchYPercent = y * 100f / getHeight();
                float barPercent = candidateBarHeight * 100f / getHeight();
                float newX = touchXPercent - dragKey.percentW;
                float newY = touchYPercent;
                newX = Math.max(0f, Math.min(newX, 100f - dragKey.percentW));
                newY = Math.max(barPercent, Math.min(newY, 100f - dragKey.percentH));
                dragKey.setPercentPosition(newX, newY);
                layoutManager.computeRects();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragKey = null;
                return true;
        }
        return true;
    }

    private KeyModel findTopmostKey(int x, int y) {
        List<KeyModel> keys = layoutManager.getProfile().allKeys();
        for (int i = keys.size() - 1; i >= 0; i--) {
            KeyModel k = keys.get(i);
            if (k.rect.contains(x, y)) return k;
        }
        return null;
    }
}
