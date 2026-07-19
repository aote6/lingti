// Canvas渲染器：逐行绘制按键、剪贴板弹出面板
package com.unbounded.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class KeyboardRenderer {

    public void drawKeyboard(Canvas canvas, LayoutProfile profile, float barHeight,
                             KeyModel activeKey, boolean isLongPressed) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        // 模式标签
        Paint labelPaint = ThemeTokens.newTextPaint();
        labelPaint.setTextSize(24f);
        labelPaint.setColor(ThemeTokens.TEXT_ACCENT);
        canvas.drawText("TERM", 8, barHeight + 18, labelPaint);

        // 按 LayoutProfile 逐行渲染
        // 坐标不在此处计算：key.rect 由 LayoutManager.computeRects() 唯一权威计算
        // （包括百分比坐标分支），渲染器只负责绘制，不重新计算位置。
        if (profile == null || profile.rows.isEmpty()) return;

        List<RowSpec> rows = profile.rows;

        for (RowSpec row : rows) {
            for (KeyModel key : row.keys) {
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

            }
        }
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
    private static final int MAX_VISIBLE_CLIPBOARD_ITEMS = 6;

    public void drawClipboardPopup(Canvas canvas, java.util.List<String> history) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        Paint overlay = new Paint();
        overlay.setColor(0xCC000000);
        canvas.drawRect(0, 0, w, h, overlay);

        if (history == null || history.isEmpty()) {
            Paint emptyPaint = ThemeTokens.newTextPaint();
            emptyPaint.setTextSize(20f);
            emptyPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            canvas.drawText("剪贴板为空", w / 2f - 60, h / 2f, emptyPaint);
            return;
        }

        int visibleCount = Math.min(history.size(), MAX_VISIBLE_CLIPBOARD_ITEMS);
        float itemHeight = h / (float) visibleCount;
        Paint bgPaint = ThemeTokens.newBgPaint();
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(18f);
        Paint borderPaint = ThemeTokens.newBorderPaint();
        borderPaint.setColor(ThemeTokens.BORDER);

        for (int i = 0; i < visibleCount; i++) {
            String raw = history.get(history.size() - 1 - i);
            float top = i * itemHeight;
            float bottom = top + itemHeight;

            bgPaint.setColor(ThemeTokens.SURFACE_RAISED);
            canvas.drawRect(0, top, w, bottom, bgPaint);
            canvas.drawLine(0, bottom, w, bottom, borderPaint);

            String display = raw.replace("\n", " ");
            if (display.length() > 40) display = display.substring(0, 40) + "...";
            textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = top + itemHeight / 2 - (fm.ascent + fm.descent) / 2;
            canvas.drawText(display, 16, textY, textPaint);
        }
    }

    public int hitTestClipboardItem(int viewHeight, int totalCount, float y) {
        if (totalCount == 0) return -1;
        int visibleCount = Math.min(totalCount, MAX_VISIBLE_CLIPBOARD_ITEMS);
        float itemHeight = viewHeight / (float) visibleCount;
        int idx = (int) (y / itemHeight);
        if (idx < 0 || idx >= visibleCount) return -1;
        return idx;
    }
}
