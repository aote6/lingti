package com.unbounded.input;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;

public class KeyboardGestureController {
    private final List<KeyModel> keys;
    private final KeyboardActionDispatcher dispatcher;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final Handler multiTapTimer = new Handler(Looper.getMainLooper());
    private final GestureRecognizer recognizer = new GestureRecognizer();

    private KeyModel activeKey;
    private boolean isGestureConsumed, isLongPressed;
    private float startX, startY;
    private int longPressSelectedIndex = -1;
    private String[] currentPopupItems;
    private final Runnable longPressRunnable;
    private final Runnable multiTapTimeoutRunnable;
    private boolean isCandidateBarPress = false;

    private final SessionAccess session;
    private final int touchSlop;

    public interface SessionAccess {
        StringBuilder composingDigits();
        List<String> candidates();
        List<String> visibleCandidates();
        void resetCandidatePage();
        List<android.graphics.Rect> candidateRects();
        float candidateBarHeight();
        void invalidateView();
        void nextPage();
        void prevPage();
        int getTotalCandidatePages();
        NineKeyKeyboard.InputMode getInputMode();
        MultiTapEngine getMultiTapEngine();
        void toggleInputMode();
        float getDpScale();
        android.content.Context getKeyboardContext();
        float getPopupBoxX();
        float getPopupItemWidth();
    }

    public KeyboardGestureController(List<KeyModel> keys, KeyboardActionDispatcher dispatcher, final SessionAccess session) {
        this.keys = keys;
        this.dispatcher = dispatcher;
        this.session = session;
        Context ctx = session.getKeyboardContext();
        this.touchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
        this.longPressRunnable = new LongPressTask(this);
        this.multiTapTimeoutRunnable = new Runnable() {
            public void run() {
                session.getMultiTapEngine().commitCurrent();
                String result = session.getMultiTapEngine().getCommitted();
                session.composingDigits().setLength(0);
                if (!result.isEmpty()) {
                    session.composingDigits().append(result);
                    session.candidates().clear();
                    session.resetCandidatePage();
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
                ctrl.currentPopupItems = getPopupItemsForKey(ctrl.activeKey);
                ctrl.longPressSelectedIndex = 0;
                ctrl.session.invalidateView();
            }
        }
    }

    private static String[] getPopupItemsForKey(KeyModel k) {
        if (k == null || k.label == null) return new String[]{"?"};
        switch (k.label) {
            case "ABC": return new String[]{"A","B","C","2","a","b","c"};
            case "DEF": return new String[]{"D","E","F","3","d","e","f"};
            case "GHI": return new String[]{"G","H","I","4","g","h","i"};
            case "JKL": return new String[]{"J","K","L","5","j","k","l"};
            case "MNO": return new String[]{"M","N","O","6","m","n","o"};
            case "PQRS": return new String[]{"P","Q","R","S","7"};
            case "TUV": return new String[]{"T","U","V","8"};
            case "WXYZ": return new String[]{"W","X","Y","Z","9"};
            default: return new String[]{k.label};
        }
    }

    public KeyModel getActiveKey() { return activeKey; }
    public boolean isLongPressed() { return isLongPressed; }
    public String[] getCurrentPopupItems() { return currentPopupItems; }
    public int getLongPressSelectedIndex() { return longPressSelectedIndex; }

    public void reset() {
        if (activeKey != null) activeKey.pressed = false;
        activeKey = null;
        isGestureConsumed = false;
        isLongPressed = false;
        longPressSelectedIndex = -1;
        currentPopupItems = null;
        isCandidateBarPress = false;
        longPressHandler.removeCallbacks(longPressRunnable);
        multiTapTimer.removeCallbacks(multiTapTimeoutRunnable);
    }

    private KeyModel findKey(float x, float y) {
        for (KeyModel k : keys) if (k.rect.contains((int) x, (int) y)) return k;
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        float barHeight = session.candidateBarHeight();
        float dp = session.getDpScale();
        float popupItemWidth = 50 * dp;
        float pageScrollThreshold = 12 * dp;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x; startY = y; isLongPressed = false; isGestureConsumed = false;
                isCandidateBarPress = (y < barHeight && session.composingDigits().length() > 0);
                if (isCandidateBarPress) {
                    if (session.getInputMode() == NineKeyKeyboard.InputMode.ENGLISH) {
                        session.getMultiTapEngine().commitCurrent();
                        String committed = session.getMultiTapEngine().getCommitted();
                        if (!committed.isEmpty()) {
                            dispatcher.onCommand(new InsertText(committed));
                            session.getMultiTapEngine().reset();
                            session.composingDigits().setLength(0);
                            session.candidates().clear();
                            session.resetCandidatePage();
                            session.invalidateView();
                            return true;
                        }
                    }
                    List<android.graphics.Rect> rects = session.candidateRects();
                    List<String> visible = session.visibleCandidates();
                    for (int i = 0; i < rects.size(); i++) {
                        if (rects.get(i).contains((int) x, (int) y)) {
                            if (i < visible.size()) {
                                String chosen = visible.get(i);
                                dispatcher.onCommand(new InsertText(chosen));
                                T9Engine.onCandidateSelected(chosen);
                            }
                            session.composingDigits().setLength(0);
                            session.candidates().clear();
                            session.resetCandidatePage();
                            if (session.getInputMode() == NineKeyKeyboard.InputMode.ENGLISH) {
                                session.getMultiTapEngine().reset();
                            }
                            session.invalidateView();
                            return true;
                        }
                    }
                }
                activeKey = findKey(x, y);
                if (activeKey != null) {
                    activeKey.pressed = true;
                    recognizer.onDown(x, y);
                    longPressHandler.postDelayed(longPressRunnable, 450);
                    session.invalidateView();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activeKey == null) {
                    if (startY < barHeight && y < barHeight && session.getTotalCandidatePages() > 1) {
                        float dy = y - startY;
                        if (Math.abs(dy) > pageScrollThreshold) {
                            if (dy > 0) session.nextPage(); else session.prevPage();
                            startY = y;
                            session.invalidateView();
                        }
                    }
                    return true;
                }
                if (isLongPressed && currentPopupItems != null) {
                    float popupBoxX = session.getPopupBoxX();
                    float pw = session.getPopupItemWidth();
                    if (pw <= 0) pw = popupItemWidth;
                    int idx = (int) ((x - popupBoxX) / pw);
                    if (idx < 0) idx = 0;
                    if (idx >= currentPopupItems.length) idx = currentPopupItems.length - 1;
                    if (idx != longPressSelectedIndex) { longPressSelectedIndex = idx; session.invalidateView(); }
                    return true;
                }
                if (!isGestureConsumed) {
                    float dx = x - startX;
                    float dy = y - startY;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance > touchSlop) {
                        longPressHandler.removeCallbacks(longPressRunnable);
                        GestureRecognizer.Gesture g = recognizer.onMove(x, y);
                        if (g != GestureRecognizer.Gesture.NONE) {
                            execGesture(g);
                            isGestureConsumed = true;
                            session.invalidateView();
                        }
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                longPressHandler.removeCallbacks(longPressRunnable);
                if (isLongPressed && currentPopupItems != null && longPressSelectedIndex >= 0) {
                    dispatcher.onCommand(new InsertText(currentPopupItems[longPressSelectedIndex]));
                    
                    isLongPressed = false; currentPopupItems = null;
                    if (activeKey != null) activeKey.pressed = false;
                    activeKey = null;
                    session.invalidateView();
                    return true;
                }
                if (activeKey != null && !isGestureConsumed) {
                    GestureRecognizer.Gesture fg = recognizer.onUp();
                    if (fg != GestureRecognizer.Gesture.NONE) execGesture(fg);
                }
                if (activeKey != null) activeKey.pressed = false;
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

        if (session.getInputMode() == NineKeyKeyboard.InputMode.TERMINAL) {
            dispatcher.onCommand(cmd);
            return;
        }

        if (g == GestureRecognizer.Gesture.TAP && session.getInputMode() == NineKeyKeyboard.InputMode.ENGLISH) {
            String rawText = activeKey.label;
            if (rawText != null && rawText.matches("[0-9]")) {
                int digit = Integer.parseInt(rawText);
                String result = session.getMultiTapEngine().processDigit(digit);
                session.composingDigits().setLength(0);
                session.composingDigits().append(result);
                session.candidates().clear();
                session.resetCandidatePage();
                session.candidates().add(result);
                multiTapTimer.removeCallbacks(multiTapTimeoutRunnable);
                multiTapTimer.postDelayed(multiTapTimeoutRunnable, 800);
                session.invalidateView();
                return;
            }
        }

        String rawText = activeKey.label;
        if (g == GestureRecognizer.Gesture.TAP && rawText != null && rawText.matches("[0-9]")) {
            session.composingDigits().append(rawText);
            session.candidates().clear();
            session.resetCandidatePage();
            session.candidates().addAll(T9Engine.getCandidates(session.composingDigits().toString()));
            session.invalidateView();
            return;
        }
        dispatcher.onCommand(cmd);
    }
}
