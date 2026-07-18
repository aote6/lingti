package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class NineKeyKeyboard extends View implements KeyboardGestureController.SessionAccess {
    static final int COLS = 3, PAD = 2;
    static final int CANDIDATE_PAGE_SIZE = 4;

    private final List<KeySlot> keys = new ArrayList<>();
    private final KeyboardRenderer renderer = new KeyboardRenderer();
    private final KeyboardGestureController gestureController;

    private final StringBuilder composingDigits = new StringBuilder();
    private final List<String> candidates = new ArrayList<>();
    private final List<Rect> candidateRects = new ArrayList<>();
    private int candidatePage = 0;
    private float candidateBarHeight;

    public NineKeyKeyboard(Context context, KeyboardActionDispatcher dispatcher, List<RuleLoader.KeyDef> defs) {
        super(context);
        for (RuleLoader.KeyDef d : defs) {
            KeySlot k = new KeySlot();
            k.tap = d.tap; k.swipeUp = d.swipeUp; k.swipeDown = d.swipeDown;
            k.swipeLeft = d.swipeLeft; k.swipeRight = d.swipeRight; k.longPress = d.longPress;
            keys.add(k);
        }
        gestureController = new KeyboardGestureController(keys, dispatcher, this);
    }

    @Override public StringBuilder composingDigits() { return composingDigits; }
    @Override public List<String> candidates() { return candidates; }
    @Override public List<Rect> candidateRects() { return candidateRects; }
    @Override public float candidateBarHeight() { return candidateBarHeight; }
    @Override public void invalidateView() { invalidate(); }

    public int getCandidatePage() { return candidatePage; }
    public int getTotalCandidatePages() {
        if (candidates.isEmpty()) return 0;
        return (candidates.size() - 1) / CANDIDATE_PAGE_SIZE + 1;
    }
    public void nextPage() {
        int total = getTotalCandidatePages();
        if (total > 1) candidatePage = (candidatePage + 1) % total;
    }
    public void prevPage() {
        int total = getTotalCandidatePages();
        if (total > 1) candidatePage = (candidatePage - 1 + total) % total;
    }

    public void resetSession() {
        composingDigits.setLength(0);
        candidates.clear();
        candidateRects.clear();
        candidatePage = 0;
        gestureController.reset();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeKeyRects(w, h);
    }

    private void computeKeyRects(int w, int h) {
        if (keys.isEmpty()) return;
        candidateBarHeight = h * 0.16f;
        float remainingHeight = h - candidateBarHeight;
        int rows = keys.size() / COLS;
        float kw = (float) w / COLS;
        float kh = remainingHeight / rows;
        for (int i = 0; i < keys.size(); i++) {
            KeySlot k = keys.get(i);
            int row = i / COLS;
            int col = i % COLS;
            float l = col * kw + PAD;
            float t = candidateBarHeight + row * kh + PAD;
            float r = (col + 1) * kw - PAD;
            float b = candidateBarHeight + (row + 1) * kh - PAD;
            k.rect.set((int) l, (int) t, (int) r, (int) b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int start = candidatePage * CANDIDATE_PAGE_SIZE;
        int end = Math.min(start + CANDIDATE_PAGE_SIZE, candidates.size());
        List<String> pageCandidates = candidates.subList(start, end);
        int totalPages = getTotalCandidatePages();

        renderer.drawKeyboard(canvas, keys, candidateBarHeight,
                gestureController.getActiveKey(), gestureController.isLongPressed(),
                composingDigits, pageCandidates, candidateRects,
                candidatePage, totalPages);
        if (gestureController.isLongPressed() && gestureController.getCurrentPopupItems() != null) {
            renderer.drawHorizontalPopup(canvas, candidateBarHeight,
                    gestureController.getCurrentPopupItems(),
                    gestureController.getLongPressSelectedIndex());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureController.onTouchEvent(event);
    }
}

class KeySlot {
    Rect rect = new Rect();
    Command tap, swipeUp, swipeDown, swipeLeft, swipeRight, longPress;
}
