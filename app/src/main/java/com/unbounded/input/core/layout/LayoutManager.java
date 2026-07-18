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
        if (currentProfile == null || currentProfile.rows.isEmpty() || viewWidth <= 0 || viewHeight <= 0) return;

        float maxSpan = 0f;
        for (RowSpec row : currentProfile.rows) {
            float s = row.totalSpan();
            if (s > maxSpan) maxSpan = s;
        }
        if (maxSpan == 0f) maxSpan = 10f;

        float unit = viewWidth / maxSpan;
        float remainingHeight = viewHeight - candidateBarHeight;
        float rowH = remainingHeight / (float) currentProfile.rows.size();
        float y = candidateBarHeight;

        for (RowSpec row : currentProfile.rows) {
            float rowWidth = row.totalSpan() * unit;
            float x = (viewWidth - rowWidth) / 2f;
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
