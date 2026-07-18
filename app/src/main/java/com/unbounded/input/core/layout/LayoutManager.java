package com.unbounded.input.core.layout;


public class LayoutManager {
    private KeyboardLayout currentLayout;
    private LayoutProfile currentProfile;
    private float candidateBarHeight;
    private int viewWidth, viewHeight;

    public void setLayout(KeyboardLayout layout, int width, int height) {
        this.currentLayout = layout;
        this.viewWidth = width;
        this.viewHeight = height;
        this.currentProfile = layout.build();
        computeRects();
    }

    public LayoutProfile getProfile() { return currentProfile; }

    public void setSize(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
        computeRects();
    }

    public void setCandidateBarHeight(float h) { this.candidateBarHeight = h; }

    public void computeRects() {
        if (currentProfile == null || viewWidth == 0 || viewHeight == 0) return;
        float y = candidateBarHeight;
        float totalSpan = 0;
        for (RowSpec row : currentProfile.rows) {
            float s = row.totalSpan();
            if (s > totalSpan) totalSpan = s;
        }
        if (totalSpan == 0) totalSpan = 10f;
        float remainingHeight = viewHeight - candidateBarHeight;
        float rowH = remainingHeight / (float) currentProfile.rows.size();
        for (RowSpec row : currentProfile.rows) {
            float unit = viewWidth / totalSpan;
            float x = 0f;
            for (KeyModel key : row.keys) {
                float kw = unit * key.span;
                key.rect.set(
                    Math.round(x + key.padLeft),
                    Math.round(y + key.padTop),
                    Math.round(x + kw - key.padRight),
                    Math.round(y + rowH - key.padBottom)
                );
                x += kw;
            }
            y += rowH;
        }
    }
}
