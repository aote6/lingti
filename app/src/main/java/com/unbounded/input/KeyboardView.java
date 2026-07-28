// 键盘View：持有Renderer和GestureController，管理剪贴板面板状态、布局编辑模式和布局槽位切换
package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;
import java.util.ArrayList;

import com.unbounded.input.core.component.ComponentContext;
import com.unbounded.input.core.component.ComponentDescriptor;
import com.unbounded.input.core.component.ComponentRegistry;
import com.unbounded.input.core.component.ComponentCategory;
import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutManager;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class KeyboardView extends View implements KeyboardGestureController.SessionAccess {
    public interface SlotSwitchListener {
        void onSwitchSlot(int slot);
    }

    private final KeyboardRenderer renderer = new KeyboardRenderer();
    private final KeyboardGestureController gestureController;
    private final LayoutManager layoutManager = new LayoutManager();

    private float candidateBarHeight;
    private float controlBarHeight;
    private float trashZoneHeight;
    private float dpScale = 1f;
    private boolean clipboardPanelOpen = false;

    private boolean editMode = false;
    private KeyModel dragKey = null;
    private KeyModel resizeKey = null;
    private static final float RESIZE_HANDLE_DP = 32f;
    private Rect editButtonRect = new Rect();
    private Rect saveButtonRect = new Rect();
    private Rect restoreButtonRect = new Rect();
    private Rect trashZoneRect = new Rect();
    private Rect[] slotButtonRects = new Rect[3];
    private Rect foldButtonRect = new Rect();
    private boolean componentPanelOpen = false;
    private boolean justOpenedComponentPanel = false;
    private Rect componentButtonRect = new Rect();
    private Rect componentPanelBg = new Rect();
    private int expandedGroupIdx = -1;
    private final List<ComponentCategory> categoryList = new ArrayList<ComponentCategory>();
    private final List<String> panelEntries = new ArrayList<String>();
    private final List<Integer> panelEntryGroupIdx = new ArrayList<Integer>();
    private final List<String> panelEntryComponentId = new ArrayList<String>();
    private final List<Rect> panelEntryRects = new ArrayList<Rect>();
    private final String layoutFileName;
    private final String layoutStateName;
    private final Runnable onRestore;
    private final int activeSlot;
    private final SlotSwitchListener slotSwitchListener;
    private final SimpleImeService imeService;

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
        this.imeService = (context instanceof SimpleImeService) ? (SimpleImeService) context : null;
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
        int gap = Math.round(4 * dpScale);
        int topPad = Math.round(3 * dpScale);
        int rowH = Math.round(30 * dpScale);
        controlBarHeight = topPad * 2 + rowH * 2 + gap;
        candidateBarHeight = controlBarHeight;
        layoutManager.setCandidateBarHeight(candidateBarHeight);
        layoutManager.setSize(w, h);

        int btnW = Math.round(48 * dpScale);
        int row1Y = topPad;
        int row2Y = topPad + rowH + gap;
        int rightX = w - Math.round(6 * dpScale);

        editButtonRect.set(rightX - btnW, row1Y, rightX, row1Y + rowH);

        int slotBtnW = Math.round(36 * dpScale);
        int foldBtnW = Math.round(36 * dpScale);
        int leftX = Math.round(6 * dpScale);
        foldButtonRect.set(leftX, row1Y, leftX + foldBtnW, row1Y + rowH);
        int slotStartX = leftX + foldBtnW + gap;
        for (int i = 0; i < slotButtonRects.length; i++) {
            int sx = slotStartX + i * (slotBtnW + gap);
            slotButtonRects[i].set(sx, row1Y, sx + slotBtnW, row1Y + rowH);
        }

        saveButtonRect.set(rightX - btnW, row2Y, rightX, row2Y + rowH);
        restoreButtonRect.set(rightX - btnW * 2 - gap, row2Y, rightX - btnW - gap, row2Y + rowH);
        componentButtonRect.set(rightX - btnW * 3 - gap * 2, row2Y, rightX - btnW * 2 - gap * 2, row2Y + rowH);

        trashZoneHeight = 40 * dpScale;
        trashZoneRect.set(0, h - Math.round(trashZoneHeight), w, h);

        rebuildComponentPanelEntries();
        layoutComponentPanelRects();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        LayoutProfile profile = layoutManager.getProfile();
        renderer.drawKeyboard(canvas, profile, candidateBarHeight,
                gestureController.getActiveKey(), gestureController.isLongPressed());
        if (clipboardPanelOpen) {
            renderer.drawClipboardPopup(canvas, com.unbounded.input.SimpleImeService.getClipboardHistory(), activeSlot);
        } else {
            drawSlotButtons(canvas);
            drawEditControls(canvas);
            if (editMode) {
                drawTrashZone(canvas);
                drawResizeHandles(canvas);
            }
            if (componentPanelOpen) drawComponentPanel(canvas);
        }
        drawFlashMessage(canvas);
    }

    private void drawTrashZone(Canvas canvas) {
        Paint bg = new Paint();
        boolean hovering = dragKey != null && trashZoneRect.contains(dragKey.rect.centerX(), dragKey.rect.centerY());
        bg.setColor(hovering ? 0xAAFF3333 : 0x66992222);
        canvas.drawRect(trashZoneRect, bg);
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);
        text.setColor(0xFFFFFFFF);
        drawCenteredText(canvas, trashZoneRect, hovering ? "松手删除" : "拖到此处删除", text);
    }

    private void drawResizeHandles(Canvas canvas) {
        List<KeyModel> keys = layoutManager.getProfile().allKeys();
        Paint handlePaint = new Paint();
        handlePaint.setAntiAlias(true);
        handlePaint.setColor(0xAAFFFFFF);
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        for (KeyModel k : keys) {
            float right = k.rect.right;
            float bottom = k.rect.bottom;
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(right, bottom - handlePx * 0.5f);
            path.lineTo(right, bottom);
            path.lineTo(right - handlePx * 0.5f, bottom);
            path.close();
            canvas.drawPath(path, handlePaint);
        }
    }

    private void drawSlotButtons(Canvas canvas) {
        Paint bg = ThemeTokens.newBgPaint();
        Paint border = ThemeTokens.newBorderPaint();
        border.setColor(ThemeTokens.BORDER);
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);

        bg.setColor(ThemeTokens.SURFACE);
        canvas.drawRect(foldButtonRect, bg);
        canvas.drawRect(foldButtonRect, border);
        text.setColor(ThemeTokens.TEXT_PRIMARY);
        drawCenteredText(canvas, foldButtonRect, "v", text);

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

            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(componentButtonRect, bg);
            canvas.drawRect(componentButtonRect, border);
            drawCenteredText(canvas, componentButtonRect, "组件", text);
        }
    }

    private void drawComponentPanel(Canvas canvas) {
        Paint bg = ThemeTokens.newBgPaint();
        bg.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(componentPanelBg, bg);
        Paint border = ThemeTokens.newBorderPaint();
        border.setColor(ThemeTokens.BORDER);
        canvas.drawRect(componentPanelBg, border);
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);
        text.setColor(ThemeTokens.TEXT_PRIMARY);
        for (int i = 0; i < panelEntries.size(); i++) {
            canvas.drawRect(panelEntryRects.get(i), border);
            drawCenteredText(canvas, panelEntryRects.get(i), panelEntries.get(i), text);
        }
    }

    private void drawCenteredText(Canvas canvas, Rect rect, String label, Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = rect.centerY() - (fm.ascent + fm.descent) / 2;
        float textX = rect.centerX() - paint.measureText(label) / 2;
        canvas.drawText(label, textX, textY, paint);
    }

    public void showDiagFlash(String message) {
        showFlash(message);
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
                KeyboardRenderer.ClipboardHit hit = renderer.hitTestClipboard(event.getX(), event.getY());
                switch (hit.action) {
                    case KeyboardRenderer.ClipboardHit.EXIT:
                        closeClipboardPanel();
                        break;
                    case KeyboardRenderer.ClipboardHit.PASTE:
                        if (imeService != null) imeService.pasteClipboardItem(hit.index);
                        invalidate();
                        break;
                    case KeyboardRenderer.ClipboardHit.PIN:
                        com.unbounded.input.SimpleImeService.toggleClipboardPin(hit.index);
                        invalidate();
                        break;
                    case KeyboardRenderer.ClipboardHit.DELETE:
                        com.unbounded.input.SimpleImeService.deleteClipboardItem(hit.index);
                        invalidate();
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

        int x = (int) event.getX(), y = (int) event.getY();

        if (componentPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (justOpenedComponentPanel) {
                    justOpenedComponentPanel = false;
                } else {
                    for (int i = 0; i < panelEntryRects.size(); i++) {
                        if (panelEntryRects.get(i).contains(x, y)) {
                            String compId = panelEntryComponentId.get(i);
                            if (compId == null) {
                                // 点击的是分类标题，展开/折叠
                                expandedGroupIdx = (expandedGroupIdx == panelEntryGroupIdx.get(i)) ? -1 : panelEntryGroupIdx.get(i);
                                rebuildComponentPanelEntries();
                                layoutComponentPanelRects();
                            } else {
                                // 点击的是具体组件
                                instantiateComponent(compId);
                                componentPanelOpen = false;
                                expandedGroupIdx = -1;
                            }
                            break;
                        }
                    }
                }
                invalidate();
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (foldButtonRect.contains(x, y)) {
                if (imeService != null) imeService.collapseKeyboard();
                return true;
            }
            if (editMode && componentButtonRect.contains(x, y)) {
                componentPanelOpen = true;
                justOpenedComponentPanel = true;
                invalidate();
                return true;
            }
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
            case MotionEvent.ACTION_DOWN: {
                KeyModel topKey = findTopmostKey(x, y);
                if (topKey != null && isInResizeHandle(topKey, x, y)) {
                    resizeKey = topKey;
                    dragKey = null;
                } else {
                    dragKey = topKey;
                    resizeKey = null;
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE:
                if (resizeKey != null) {
                    float touchXPercent = x * 100f / getWidth();
                    float touchYPercent = y * 100f / getHeight();
                    float minWPercent = RESIZE_HANDLE_DP * dpScale * 100f / getWidth();
                    float minHPercent = RESIZE_HANDLE_DP * dpScale * 100f / getHeight();
                    float newW = touchXPercent - resizeKey.percentX;
                    float newH = touchYPercent - resizeKey.percentY;
                    newW = Math.max(minWPercent, Math.min(newW, 100f - resizeKey.percentX));
                    newH = Math.max(minHPercent, Math.min(newH, 100f - resizeKey.percentY));
                    resizeKey.percentW = newW;
                    resizeKey.percentH = newH;
                    resizeKey.hasPercentRect = true;
                    layoutManager.computeRects();
                    invalidate();
                    return true;
                }
                if (dragKey == null) return true;
                float touchXPercent2 = x * 100f / getWidth();
                float touchYPercent2 = y * 100f / getHeight();
                float barPercent = candidateBarHeight * 100f / getHeight();
                float newX = touchXPercent2 - dragKey.percentW;
                float newY = touchYPercent2;
                newX = Math.max(0f, Math.min(newX, 100f - dragKey.percentW));
                newY = Math.max(barPercent, Math.min(newY, 100f - dragKey.percentH));
                dragKey.setPercentPosition(newX, newY);
                layoutManager.computeRects();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (resizeKey != null) {
                    resizeKey = null;
                    invalidate();
                    return true;
                }
                if (dragKey != null && trashZoneRect.contains(x, y)) {
                    String deletedLabel = dragKey.label;
                    layoutManager.getProfile().removeKey(dragKey);
                    gestureController.updateKeys(layoutManager.getProfile().allKeys());
                    showFlash("已删除: " + deletedLabel);
                }
                dragKey = null;
                invalidate();
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

    private boolean isInResizeHandle(KeyModel key, int x, int y) {
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        float left = key.rect.right - handlePx;
        float top = key.rect.bottom - handlePx;
        return x >= left && x <= key.rect.right && y >= top && y <= key.rect.bottom;
    }

    private void instantiateComponent(String componentId) {
        LayoutProfile profile = layoutManager.getProfile();
        RowSpec newRow = new RowSpec();
        long stamp = System.currentTimeMillis();
        ComponentContext ctx = new ComponentContext(stamp);
        List<KeyModel> keys = ComponentRegistry.getInstance().instantiate(componentId, ctx);
        for (KeyModel k : keys) newRow.add(k);
        profile.addRow(newRow);
        gestureController.updateKeys(profile.allKeys());
        layoutManager.computeRects();
        showFlash("已添加：" + componentId);
        invalidate();
    }

    private void rebuildComponentPanelEntries() {
        categoryList.clear();
        categoryList.addAll(ComponentRegistry.getInstance().getCategories());
        panelEntries.clear();
        panelEntryGroupIdx.clear();
        panelEntryComponentId.clear();
        for (int g = 0; g < categoryList.size(); g++) {
            ComponentCategory cat = categoryList.get(g);
            String arrow = (g == expandedGroupIdx) ? "v " : "> ";
            panelEntries.add(arrow + cat.getDefaultLabel());
            panelEntryGroupIdx.add(g);
            panelEntryComponentId.add(null); // null 表示这是分类标题
            if (g == expandedGroupIdx) {
                List<ComponentDescriptor> items = ComponentRegistry.getInstance().getByCategory(cat);
                for (ComponentDescriptor desc : items) {
                    panelEntries.add("    " + desc.getLabel());
                    panelEntryGroupIdx.add(g);
                    panelEntryComponentId.add(desc.getId());
                }
            }
        }
    }

    private void layoutComponentPanelRects() {
        int panelW = Math.round(200 * dpScale);
        int itemH = Math.round(36 * dpScale);
        int panelX = (getWidth() - panelW) / 2;
        int panelY = Math.round(controlBarHeight + 8 * dpScale);
        panelEntryRects.clear();
        for (int i = 0; i < panelEntries.size(); i++) {
            panelEntryRects.add(new Rect(panelX, panelY + i * itemH,
                    panelX + panelW, panelY + (i + 1) * itemH));
        }
        int totalH = itemH * Math.max(panelEntries.size(), 1);
        componentPanelBg.set(panelX, panelY, panelX + panelW, panelY + totalH);
    }
}
