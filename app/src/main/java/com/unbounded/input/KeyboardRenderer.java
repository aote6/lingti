package com.unbounded.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class KeyboardRenderer {

    public void drawKeyboard(Canvas canvas, LayoutProfile profile, float candidateBarHeight,
                             KeyModel activeKey, boolean isLongPressed,
                             StringBuilder composingDigits, List<String> pageCandidates,
                             List<Rect> candidateRects, int candidatePage, int totalPages,
                             String modeLabel) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        // 候选栏
        float barY = drawCandidateBar(canvas, w, candidateBarHeight, composingDigits,
                pageCandidates, candidateRects, candidatePage, totalPages);
        // 模式标签
        Paint labelPaint = ThemeTokens.newTextPaint();
        labelPaint.setTextSize(24f);
        labelPaint.setColor(ThemeTokens.TEXT_ACCENT);
        canvas.drawText(modeLabel, 8, barY - 6, labelPaint);

        // 按 LayoutProfile 逐行渲染
        if (profile == null || profile.rows.isEmpty()) return;

        List<RowSpec> rows = profile.rows;
        float maxSpan = 0f;
        for (RowSpec row : rows) {
            float s = row.totalSpan();
            if (s > maxSpan) maxSpan = s;
        }
        if (maxSpan == 0f) maxSpan = 10f;

        float remainingHeight = h - barY;
        float rowH = remainingHeight / rows.size();
        float unit = w / maxSpan;
        float y = barY;

        for (RowSpec row : rows) {
            float rowWidth = row.totalSpan() * unit;
            float x = (w - rowWidth) / 2f;
            for (KeyModel key : row.keys) {
                float kw = unit * key.span;
                float left = x + key.padLeft;
                float top = y + key.padTop;
                float right = x + kw - key.padRight;
                float bottom = y + rowH - key.padBottom;

                key.rect.set((int) left, (int) top, (int) right, (int) bottom);

                // 按键背景
                Paint bgPaint = ThemeTokens.newBgPaint();
                if (key == activeKey) {
                    bgPaint.setColor(isLongPressed ? ThemeTokens.PRESS_BG : ThemeTokens.BORDER_ACTIVE);
                } else if (key.enabled) {
                    bgPaint.setColor(ThemeTokens.SURFACE);
                } else {
                    bgPaint.setColor(ThemeTokens.BG);
                }
                canvas.drawRect(key.rect, bgPaint);

                // 按键边框
                if (key.enabled) {
                    Paint borderPaint = ThemeTokens.newBorderPaint();
                    borderPaint.setColor(key == activeKey ? ThemeTokens.BORDER_ACTIVE : ThemeTokens.BORDER);
                    canvas.drawRect(key.rect, borderPaint);
                }

                // 按键文字
                if (key.enabled && key.label != null && !key.label.isEmpty()) {
                    Paint textPaint = ThemeTokens.newTextPaint();
                    textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
                    float textSize = key.label.length() > 2 ? 16f : 22f;
                    textSize = key.label.length() > 4 ? 12f : textSize;
                    textPaint.setTextSize(textSize);
                    Paint.FontMetrics fm = textPaint.getFontMetrics();
                    float textY = key.rect.centerY() - (fm.ascent + fm.descent) / 2;
                    float textX = key.rect.centerX() - textPaint.measureText(key.label) / 2;
                    canvas.drawText(key.label, textX, textY, textPaint);
                }

                x += kw;
            }
            y += rowH;
        }
    }

    private float drawCandidateBar(Canvas canvas, int w, float barHeight,
                                   StringBuilder composingDigits, List<String> pageCandidates,
                                   List<Rect> candidateRects, int candidatePage, int totalPages) {
        Paint bgPaint = ThemeTokens.newBgPaint();
        bgPaint.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(0, 0, w, barHeight, bgPaint);

        // 拼写中数字
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
        textPaint.setTextSize(18f);
        String digits = composingDigits.toString();
        if (!digits.isEmpty()) {
            canvas.drawText(digits, 8, barHeight * 0.35f, textPaint);
        }

        // 候选词
        candidateRects.clear();
        if (pageCandidates.isEmpty()) return barHeight;

        float itemW = w / (float) pageCandidates.size();
        textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
        textPaint.setTextSize(16f);
        for (int i = 0; i < pageCandidates.size(); i++) {
            float cx = i * itemW;
            Rect rect = new Rect((int) cx, (int) (barHeight * 0.4f),
                    (int) (cx + itemW), (int) barHeight);
            candidateRects.add(rect);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = rect.centerY() - (fm.ascent + fm.descent) / 2;
            canvas.drawText(pageCandidates.get(i), cx + 4, textY, textPaint);
        }

        // 翻页指示
        if (totalPages > 1) {
            textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
            textPaint.setTextSize(12f);
            String pageInfo = (candidatePage + 1) + "/" + totalPages;
            canvas.drawText(pageInfo, w - 40, barHeight * 0.35f, textPaint);
        }

        return barHeight;
    }

    public void drawHorizontalPopup(Canvas canvas, float candidateBarHeight,
                                    String[] items, int selectedIndex) {
        if (items == null || items.length == 0) return;
        int w = canvas.getWidth();
        float boxWidth = w * 0.85f, boxX = (w - boxWidth) / 2f;
        float boxHeight = 60f, boxY = candidateBarHeight + 8f;

        Paint popupPaint = ThemeTokens.newBgPaint();
        popupPaint.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);

        popupPaint.setStyle(Paint.Style.STROKE);
        popupPaint.setStrokeWidth(3f);
        popupPaint.setColor(ThemeTokens.BORDER_ACTIVE);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        popupPaint.setStyle(Paint.Style.FILL);

        float itemWidth = boxWidth / items.length;
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(20f);
        textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
        for (int i = 0; i < items.length; i++) {
            float ix = boxX + i * itemWidth;
            if (i == selectedIndex) {
                Paint selPaint = ThemeTokens.newBgPaint();
                selPaint.setColor(ThemeTokens.PRESS_BG);
                canvas.drawRect(ix, boxY, ix + itemWidth, boxY + boxHeight, selPaint);
            }
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = boxY + boxHeight / 2 - (fm.ascent + fm.descent) / 2;
            float textX = ix + itemWidth / 2 - textPaint.measureText(items[i]) / 2;
            canvas.drawText(items[i], textX, textY, textPaint);
        }
    }
}
