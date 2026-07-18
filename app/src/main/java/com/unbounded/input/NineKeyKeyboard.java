// 键盘View：持有Renderer和GestureController，管理剪贴板面板状态
package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutManager;
import com.unbounded.input.core.layout.LayoutProfile;

public class NineKeyKeyboard extends View implements KeyboardGestureController.SessionAccess {
    private final KeyboardRenderer renderer = new KeyboardRenderer();
    private final KeyboardGestureController gestureController;
    private final LayoutManager layoutManager = new LayoutManager();

    private float candidateBarHeight;
    private float dpScale = 1f;
    private boolean clipboardPanelOpen = false;

    public enum InputMode { CHINESE, ENGLISH, TERMINAL }
    private InputMode inputMode = InputMode.TERMINAL;

    public NineKeyKeyboard(Context context, final KeyboardActionDispatcher dispatcher, final LayoutProfile profile) {
        super(context);
        dpScale = getResources().getDisplayMetrics().density;
        List<KeyModel> allKeys = profile.allKeys();
        layoutManager.setLayout(new KeyboardLayout() {
            public String id() { return "inline"; }
            public LayoutProfile build() { return profile; }
        }, getWidth(), getHeight());
        gestureController = new KeyboardGestureController(allKeys, dispatcher, this);
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
        candidateBarHeight = 0;
        layoutManager.setCandidateBarHeight(candidateBarHeight);
        layoutManager.setSize(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        LayoutProfile profile = layoutManager.getProfile();
        renderer.drawKeyboard(canvas, profile, candidateBarHeight,
                gestureController.getActiveKey(), gestureController.isLongPressed());
        if (clipboardPanelOpen) {
            renderer.drawClipboardPopup(canvas, com.unbounded.input.SimpleImeService.getClipboardHistory());
        }
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
        return gestureController.onTouchEvent(event);
    }
}
