package com.unbounded.input;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import java.util.List;

public class KeyboardGestureController {
    private final List<KeySlot> keys;
    private final KeyboardActionDispatcher dispatcher;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final Handler multiTapTimer = new Handler(Looper.getMainLooper());
    private final GestureRecognizer recognizer = new GestureRecognizer();

    private KeySlot activeKey;
    private boolean isGestureConsumed, isLongPressed;
    private float startX, startY;
    private int longPressSelectedIndex = -1;
    private String[] currentPopupItems;
    private final Runnable longPressRunnable;
    private final Runnable multiTapTimeoutRunnable;

    private final SessionAccess session;

    public interface SessionAccess {
        StringBuilder composingDigits();
        List<String> candidates();
        List<android.graphics.Rect> candidateRects();
        float candidateBarHeight();
        void invalidateView();
        void nextPage();
        void prevPage();
        int getTotalCandidatePages();
        NineKeyKeyboard.InputMode getInputMode();
        MultiTapEngine getMultiTapEngine();
        void toggleInputMode();
    }

    public KeyboardGestureController(List<KeySlot> keys, KeyboardActionDispatcher dispatcher, final SessionAccess session) {
        this.keys = keys;
        this.dispatcher = dispatcher;
        this.session = session;
        this.longPressRunnable = new LongPressTask(this);
        this.multiTapTimeoutRunnable = new Runnable() {
            public void run() {
                session.getMultiTapEngine().commitCurrent();
                String result = session.getMultiTapEngine().getCommitted();
                session.composingDigits().setLength(0);
                if (!result.isEmpty()) {
                    session.composingDigits().append(result);
                    session.candidates().clear();
                    session.candidates().add(result);
                }
                session.invalidateView();
            }
        };
    }

    private static class LongPressTask implements Runnable {
        private final KeyboardGestureController ctrl;
        LongPressTask(KeyboardGestureController ctrl) { this.ctrl = ctrl; }
        public void run() {
            if (ctrl.activeKey != null && !ctrl.isGestureConsumed) {
                ctrl.isLongPressed = true;
                ctrl.isGestureConsumed = true;
                ctrl.currentPopupItems = KeyboardRenderer.getPopupItemsForKey(ctrl.activeKey);
                ctrl.longPressSelectedIndex = 0;
                ctrl.session.invalidateView();
            }
        }
    }

    public KeySlot getActiveKey() { return activeKey; }
    public boolean isLongPressed() { return isLongPressed; }
    public String[] getCurrentPopupItems() { return currentPopupItems; }
    public int getLongPressSelectedIndex() { return longPressSelectedIndex; }

    public void reset() {
        activeKey = null;
        isGestureConsumed = false;
        isLongPressed = false;
        longPressSelectedIndex = -1;
        currentPopupItems = null;
        longPressHandler.removeCallbacks(longPressRunnable);
        multiTapTimer.removeCallbacks(multiTapTimeoutRunnable);
    }

    private KeySlot findKey(float x, float y) {
        for (KeySlot k : keys) if (k.rect.contains((int) x, (int) y)) return k;
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        float barHeight = session.candidateBarHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x; startY = y; isLongPressed = false; isGestureConsumed = false;
                if (y < barHeight && session.composingDigits().length() > 0) {
                    List<android.graphics.Rect> rects = session.candidateRects();
                    List<String> cands = session.candidates();
                    for (int i = 0; i < rects.size(); i++) {
                        if (rects.get(i).contains((int) x, (int) y)) {
                            if (session.getInputMode() == NineKeyKeyboard.InputMode.ENGLISH) {
                                if (i < cands.size()) {
                                    dispatcher.onCommand(new InsertText(cands.get(i)));
                                }
                                session.composingDigits().setLength(0);
                                cands.clear();
                                session.getMultiTapEngine().reset();
                            } else {
                                if (i < cands.size()) {
                                    dispatcher.onCommand(new InsertText(cands.get(i)));
                                }
                                session.composingDigits().setLength(0);
                                cands.clear();
                            }
                            session.invalidateView();
                            return true;
                        }
                    }
                }
                activeKey = findKey(x, y);
                if (activeKey != null) {
                    recognizer.onDown(x, y);
                    longPressHandler.postDelayed(longPressRunnable, 450);
                    session.invalidateView();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activeKey == null) {
                    if (startY < barHeight && y < barHeight && session.getTotalCandidatePages() > 1) {
                        float dy = y - startY;
                        if (Math.abs(dy) > 40f) {
                            if (dy > 0) session.nextPage(); else session.prevPage();
                            startY = y;
                            session.invalidateView();
                        }
                    }
                    return true;
                }
                if (isLongPressed && currentPopupItems != null) {
                    int idx = (int) ((x - startX) / 50);
                    if (idx < 0) idx = 0;
                    if (idx >= currentPopupItems.length) idx = currentPopupItems.length - 1;
                    if (idx != longPressSelectedIndex) { longPressSelectedIndex = idx; session.invalidateView(); }
                    return true;
                }
                if (!isGestureConsumed && (Math.abs(x - startX) > 25 || Math.abs(y - startY) > 25))
                    longPressHandler.removeCallbacks(longPressRunnable);
                if (!isGestureConsumed) {
                    GestureRecognizer.Gesture g = recognizer.onMove(x, y);
                    if (g != GestureRecognizer.Gesture.NONE) { execGesture(g); isGestureConsumed = true; session.invalidateView(); }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                longPressHandler.removeCallbacks(longPressRunnable);
                if (isLongPressed && currentPopupItems != null && longPressSelectedIndex >= 0) {
                    dispatcher.onCommand(new InsertText(currentPopupItems[longPressSelectedIndex]));
                    isLongPressed = false; currentPopupItems = null; activeKey = null;
                    session.invalidateView();
                    return true;
                }
                if (activeKey != null && !isGestureConsumed) {
                    GestureRecognizer.Gesture fg = recognizer.onUp();
                    if (fg != GestureRecognizer.Gesture.NONE) execGesture(fg);
                }
                activeKey = null; isGestureConsumed = false;
                session.invalidateView();
                return true;
        }
        return true;
    }

    private void execGesture(GestureRecognizer.Gesture g) {
        if (activeKey == null || dispatcher == null) return;
        Command cmd = null;
        switch (g) {
            case TAP: cmd = activeKey.tap; break;
            case SWIPE_UP: cmd = activeKey.swipeUp; break;
            case SWIPE_LEFT: cmd = activeKey.swipeLeft; break;
            case SWIPE_DOWN: cmd = activeKey.swipeDown; break;
            case SWIPE_RIGHT: cmd = activeKey.swipeRight; break;
            default: return;
        }
        if (cmd == null) return;

        // 终端模式：所有手势直接 commit 文本，不走 T9/MultiTap
        if (session.getInputMode() == NineKeyKeyboard.InputMode.TERMINAL) {
            dispatcher.onCommand(cmd);
            return;
        }

        if (g == GestureRecognizer.Gesture.TAP && session.getInputMode() == NineKeyKeyboard.InputMode.ENGLISH) {
            String rawText = KeyboardRenderer.cmdLabel(activeKey.tap);
            if (KeyboardRenderer.isNumeric(rawText)) {
                int digit = Integer.parseInt(rawText);
                String result = session.getMultiTapEngine().processDigit(digit);
                session.composingDigits().setLength(0);
                session.composingDigits().append(result);
                session.candidates().clear();
                session.candidates().add(result);
                multiTapTimer.removeCallbacks(multiTapTimeoutRunnable);
                multiTapTimer.postDelayed(multiTapTimeoutRunnable, 800);
                session.invalidateView();
                return;
            }
        }

        String rawText = KeyboardRenderer.cmdLabel(activeKey.tap);
        if (g == GestureRecognizer.Gesture.TAP && KeyboardRenderer.isNumeric(rawText)) {
            session.composingDigits().append(rawText);
            session.candidates().clear();
            session.candidates().addAll(T9Engine.getCandidates(session.composingDigits().toString()));
            session.invalidateView();
            return;
        }
        dispatcher.onCommand(cmd);
    }
}
