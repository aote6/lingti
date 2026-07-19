// 布局管理器：持有LayoutProfile，计算按键rect坐标
package com.unbounded.input.core.layout;

import android.util.Log;

public class LayoutManager {
    private static final String TAG = "LayoutManager";

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

    // 拖拽摆放模式用：把当前所有键（不论原来是span还是百分比）统一转成百分比坐标，
    // 基于它们当前已经算好的像素rect反推百分比值。转换后 computeRects() 的结果应与转换前一致（幂等）。
    public void convertAllToPercent() {
        if (currentProfile == null || viewWidth <= 0 || viewHeight <= 0) return;
        for (RowSpec row : currentProfile.rows) {
            for (KeyModel key : row.keys) {
                if (key.hasPercentRect) continue;
                float px = key.rect.left * 100f / viewWidth;
                float py = key.rect.top * 100f / viewHeight;
                float pw = key.rect.width() * 100f / viewWidth;
                float ph = key.rect.height() * 100f / viewHeight;
                key.setPercentRect(px, py, pw, ph);
            }
        }
        computeRects();
    }

    public void computeRects() {
        if (currentProfile == null || currentProfile.rows.isEmpty() || viewWidth <= 0 || viewHeight <= 0) return;

        for (RowSpec row : currentProfile.rows) {
            if (row.isMixedRow()) {
                Log.w(TAG, "行内混用百分比与span坐标，该行百分比键将退化为span处理");
            }
        }

        float maxSpan = 0f;
        int spanRowCount = 0;
        for (RowSpec row : currentProfile.rows) {
            boolean purePercent = row.isPercentRow() && !row.isMixedRow();
            if (purePercent) continue;
            float s = row.totalSpan();
            if (s > maxSpan) maxSpan = s;
            spanRowCount++;
        }
        if (maxSpan == 0f) maxSpan = 10f;

        float unit = viewWidth / maxSpan;
        float remainingHeight = viewHeight - candidateBarHeight;
        float rowH = spanRowCount > 0 ? remainingHeight / (float) spanRowCount : remainingHeight;
        float y = candidateBarHeight;

        for (RowSpec row : currentProfile.rows) {
            boolean purePercent = row.isPercentRow() && !row.isMixedRow();

            if (purePercent) {
                for (KeyModel key : row.keys) {
                    float kx = viewWidth * key.percentX / 100f;
                    float ky = viewHeight * key.percentY / 100f;
                    float kw = viewWidth * key.percentW / 100f;
                    float kh = viewHeight * key.percentH / 100f;
                    key.rect.set(
                        Math.round(kx + key.padLeft),
                        Math.round(ky + key.padTop),
                        Math.round(kx + kw - key.padRight),
                        Math.round(ky + kh - key.padBottom)
                    );
                }
                continue;
            }

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
