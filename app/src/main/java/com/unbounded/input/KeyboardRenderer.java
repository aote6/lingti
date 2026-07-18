package com.unbounded.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;

public class KeyboardRenderer {
    private final Paint bgPaint, textPaint, borderPaint, popupPaint;

    public KeyboardRenderer() {
        bgPaint = ThemeTokens.newBgPaint();
        borderPaint = ThemeTokens.newBorderPaint();
        popupPaint = ThemeTokens.newBgPaint();
        textPaint = ThemeTokens.newTextPaint();
    }

    public void drawKeyboard(Canvas canvas, List<KeyModel> keys, float candidateBarHeight,
                              KeyModel activeKey, boolean isLongPressed,
                              StringBuilder composingDigits, List<String> candidates,
                              List<Rect> candidateRects,
                              int currentPage, int totalPages,
                              String modeLabel, int cols) {
        canvas.drawColor(ThemeTokens.BG);
        borderPaint.setColor(ThemeTokens.BORDER);
        canvas.drawLine(0, candidateBarHeight, canvas.getWidth(), candidateBarHeight, borderPaint);

        candidateRects.clear();
        if (composingDigits.length() > 0) {
            float currentX = 30f;
            textPaint.setTextSize(candidateBarHeight * 0.45f);
            textPaint.setTextAlign(Paint.Align.LEFT);
            float yOffset = candidateBarHeight * 0.63f;
            textPaint.setColor(ThemeTokens.TEXT_ACCENT);
            for (String cand : candidates) {
                float w = textPaint.measureText(cand);
                candidateRects.add(new Rect((int) currentX - 12, 0, (int) (currentX + w + 12), (int) candidateBarHeight));
                canvas.drawText(cand, currentX, yOffset, textPaint);
                currentX += w + 35f;
            }
            if (totalPages > 1) {
                float pageY = candidateBarHeight * 0.25f;
                textPaint.setTextSize(candidateBarHeight * 0.3f);
                textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
                textPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText((currentPage + 1) + "/" + totalPages, canvas.getWidth() - 20f, pageY, textPaint);
            }
        }

        textPaint.setTextSize(candidateBarHeight * 0.35f);
        textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(modeLabel, 12f, candidateBarHeight * 0.55f, textPaint);

        if (keys.isEmpty()) return;
        float remainingHeight = canvas.getHeight() - candidateBarHeight;
        int rows = (int) Math.ceil((float) keys.size() / cols);
        float kh = remainingHeight / rows;
        for (int i = 0; i < keys.size(); i++) {
            KeyModel k = keys.get(i);
            Rect r = k.rect;
            float l = r.left, t = r.top, r_ = r.right, b = r.bottom;
            boolean pressed = k.pressed;
            bgPaint.setColor(pressed ? ThemeTokens.PRESS_BG : ThemeTokens.SURFACE);
            canvas.drawRect(l, t, r_, b, bgPaint);
            borderPaint.setColor(pressed ? ThemeTokens.BORDER_ACTIVE : ThemeTokens.BORDER);
            canvas.drawRect(l, t, r_, b, borderPaint);
            float cx = (l + r_) / 2f, cy = (t + b) / 2f;
            String displayLabel = k.label;
            if (displayLabel != null) {
                textPaint.setColor(pressed ? ThemeTokens.TEXT_ACCENT : ThemeTokens.TEXT_PRIMARY);
                float fontSize = kh * (displayLabel.length() > 2 ? 0.22f : 0.3f);
                if (cols > 3) fontSize *= 1.3f;
                textPaint.setTextSize(fontSize);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(displayLabel, cx, cy + kh * 0.08f, textPaint);
            }
        }
    }

    public void drawHorizontalPopup(Canvas canvas, float candidateBarHeight,
                                     String[] items, int selectedIndex) {
        if (items == null) return;
        float boxHeight = candidateBarHeight * 1.2f, boxY = candidateBarHeight + 20f;
        float boxWidth = canvas.getWidth() * 0.85f, boxX = (canvas.getWidth() - boxWidth) / 2f;
        popupPaint.setColor(ThemeTokens.SURFACE_RAISED);
        popupPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        popupPaint.setColor(ThemeTokens.TEXT_ACCENT);
        popupPaint.setStyle(Paint.Style.STROKE);
        popupPaint.setStrokeWidth(3f);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        float itemWidth = boxWidth / items.length;
        textPaint.setTextSize(boxHeight * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < items.length; i++) {
            float ix = boxX + i * itemWidth + itemWidth / 2f;
            if (i == selectedIndex) {
                popupPaint.setColor(ThemeTokens.PRESS_BG);
                popupPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(boxX + i * itemWidth, boxY, boxX + (i + 1) * itemWidth, boxY + boxHeight, popupPaint);
                textPaint.setColor(ThemeTokens.TEXT_ACCENT);
            } else {
                textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
            }
            canvas.drawText(items[i], ix, boxY + boxHeight * 0.65f, textPaint);
        }
    }
}
