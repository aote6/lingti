package com.unbounded.input;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutManager;
import com.unbounded.input.core.layout.LayoutProfile;

public class NineKeyKeyboard extends View implements KeyboardGestureController.SessionAccess {
    static final int PAD = 2;
    static final int CANDIDATE_PAGE_SIZE = 4;

    private final KeyboardRenderer renderer = new KeyboardRenderer();
    private final KeyboardGestureController gestureController;
    private final LayoutManager layoutManager = new LayoutManager();

    private final StringBuilder composingDigits = new StringBuilder();
    private final List<String> candidates = new ArrayList<>();
    private final List<Rect> candidateRects = new ArrayList<>();
    private int candidatePage = 0;
    private float candidateBarHeight;
    private int cols = 3;

    public enum InputMode { CHINESE, ENGLISH, TERMINAL }
    private InputMode inputMode = InputMode.CHINESE;
    private final MultiTapEngine multiTapEngine = new MultiTapEngine();

    public NineKeyKeyboard(Context context, final KeyboardActionDispatcher dispatcher, final List<KeyModel> keyModels) {
        super(context);
        final LayoutProfile profile = new LayoutProfile("inline");
        int rows = (int) Math.ceil((float) keyModels.size() / 3);
        for (int r = 0; r < rows; r++) {
            com.unbounded.input.core.layout.RowSpec row = new com.unbounded.input.core.layout.RowSpec();
            for (int c = 0; c < 3; c++) {
                int idx = r * 3 + c;
                if (idx < keyModels.size()) row.add(keyModels.get(idx));
            }
            profile.addRow(row);
        }
        List<KeyModel> allKeys = profile.allKeys();
        layoutManager.setLayout(new KeyboardLayout() {
            public String id() { return "inline"; }
            public LayoutProfile build() { return profile; }
        }, getWidth(), getHeight());
        gestureController = new KeyboardGestureController(allKeys, dispatcher, this);
        detectOrientation();
    }

    private void detectOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        cols = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 6 : 3;
    }

    public int getCols() { return cols; }

    public void setInputMode(InputMode mode) {
        if (this.inputMode != mode) {
            this.inputMode = mode;
            resetSession();
        }
    }
    public InputMode getInputMode() { return inputMode; }

    @Override public void toggleInputMode() {
        if (inputMode == InputMode.CHINESE) setInputMode(InputMode.ENGLISH);
        else if (inputMode == InputMode.ENGLISH) setInputMode(InputMode.CHINESE);
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

    public MultiTapEngine getMultiTapEngine() { return multiTapEngine; }

    public void resetSession() {
        composingDigits.setLength(0);
        candidates.clear();
        candidateRects.clear();
        candidatePage = 0;
        multiTapEngine.reset();
        gestureController.reset();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        detectOrientation();
        candidateBarHeight = h * 0.16f;
        layoutManager.setCandidateBarHeight(candidateBarHeight);
        layoutManager.setSize(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int start = candidatePage * CANDIDATE_PAGE_SIZE;
        int end = Math.min(start + CANDIDATE_PAGE_SIZE, candidates.size());
        List<String> pageCandidates = candidates.isEmpty() ? candidates : candidates.subList(start, end);
        int totalPages = getTotalCandidatePages();

        String modeLabel;
        switch (inputMode) {
            case ENGLISH: modeLabel = "EN"; break;
            case TERMINAL: modeLabel = "TERM"; break;
            default: modeLabel = "中"; break;
        }

        LayoutProfile profile = layoutManager.getProfile();
        List<KeyModel> keys = profile != null ? profile.allKeys() : new ArrayList<KeyModel>();
        renderer.drawKeyboard(canvas, keys, candidateBarHeight,
                gestureController.getActiveKey(), gestureController.isLongPressed(),
                composingDigits, pageCandidates, candidateRects,
                candidatePage, totalPages, modeLabel, cols);
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
