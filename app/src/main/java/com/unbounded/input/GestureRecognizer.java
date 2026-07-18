package com.unbounded.input;


public class GestureRecognizer {
    public enum Gesture { TAP, SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, LONG_PRESS, NONE }

    private float downX, downY;
    private long downTime;
    private boolean isSwiping, isLongPress;
    private Gesture result;
    private static final int SWIPE_THRESHOLD = 45;
    private static final int LONG_PRESS_MS = 350;

    public GestureRecognizer() { reset(); }
    public void reset() { downX = 0; downY = 0; downTime = 0; isSwiping = false; isLongPress = false; result = Gesture.NONE; }
    public void onDown(float x, float y) { reset(); downX = x; downY = y; downTime = System.currentTimeMillis(); }

    public Gesture onMove(float x, float y) {
        if (isLongPress) return Gesture.NONE;
        float dx = x - downX, dy = y - downY;
        if (Math.abs(dx) > SWIPE_THRESHOLD || Math.abs(dy) > SWIPE_THRESHOLD) {
            isSwiping = true;
            if (Math.abs(dy) > Math.abs(dx)) result = dy < -SWIPE_THRESHOLD ? Gesture.SWIPE_UP : Gesture.SWIPE_DOWN;
            else result = dx < -SWIPE_THRESHOLD ? Gesture.SWIPE_LEFT : Gesture.SWIPE_RIGHT;
            return result;
        }
        return Gesture.NONE;
    }

    public Gesture onUp() { if (isSwiping) return result; if (isLongPress) return Gesture.LONG_PRESS; return Gesture.TAP; }
    public Gesture checkLongPress() { if (!isSwiping && (System.currentTimeMillis() - downTime) >= LONG_PRESS_MS) { isLongPress = true; return Gesture.LONG_PRESS; } return Gesture.NONE; }
    public boolean shouldCheckLongPress() { return !isSwiping; }
}
