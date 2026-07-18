// 手势识别：根据触控坐标判断TAP/SWIPE方向
package com.unbounded.input;

public class GestureRecognizer {
    public enum Gesture { NONE, TAP, SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, LONG_PRESS }

    private float downX, downY;
    private boolean moved;
    private static final float SWIPE_THRESHOLD = 18f;

    public void onDown(float x, float y) { downX = x; downY = y; moved = false; }

    public Gesture onMove(float x, float y) {
        float dx = x - downX, dy = y - downY;
        if (Math.abs(dx) < SWIPE_THRESHOLD && Math.abs(dy) < SWIPE_THRESHOLD) return Gesture.NONE;
        moved = true;
        if (Math.abs(dx) > Math.abs(dy)) return dx > 0 ? Gesture.SWIPE_RIGHT : Gesture.SWIPE_LEFT;
        else return dy > 0 ? Gesture.SWIPE_DOWN : Gesture.SWIPE_UP;
    }

    public Gesture onUp() {
        if (!moved) return Gesture.TAP;
        return Gesture.NONE;
    }
}
