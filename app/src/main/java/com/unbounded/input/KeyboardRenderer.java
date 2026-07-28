// Canvas渲染器：逐行绘制按键、剪贴板弹出面板
package com.unbounded.input;

import android.graphics.Rect;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class KeyboardRenderer {

    public void drawKeyboard(Canvas canvas, LayoutProfile profile, float barHeight,
                             KeyModel activeKey, boolean isLongPressed) {
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

    public static class ClipboardHit {
        public static final int NONE = 0, EXIT = 1, PASTE = 2, PIN = 3, DELETE = 4;
        public int action = NONE;
        public int index = -1;
    }

    private final Rect clipExitRect = new Rect();
    private final java.util.List<Rect> clipTextRects = new java.util.ArrayList<>();
    private final java.util.List<Rect> clipPinRects = new java.util.ArrayList<>();
    private final java.util.List<Rect> clipDeleteRects = new java.util.ArrayList<>();
    private final java.util.List<Integer> clipRowRealIndex = new java.util.ArrayList<>();

    public void drawClipboardPopup(Canvas canvas, java.util.List<SimpleImeService.ClipboardEntry> history, int currentSlot) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        clipTextRects.clear();
        clipPinRects.clear();
        clipDeleteRects.clear();
        clipRowRealIndex.clear();

        Paint overlay = new Paint();
        overlay.setColor(0xCC000000);
        canvas.drawRect(0, 0, w, h, overlay);

        int topBarHeight = 90;
        int exitW = 110, exitPad = 10;
        clipExitRect.set(w - exitW - exitPad, exitPad, w - exitPad, topBarHeight - exitPad);

        Paint barBg = ThemeTokens.newBgPaint();
        barBg.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(0, 0, w, topBarHeight, barBg);
        Paint exitBorder = ThemeTokens.newBorderPaint();
        exitBorder.setColor(ThemeTokens.BORDER_ACTIVE);
        canvas.drawRect(clipExitRect, exitBorder);
        Paint exitText = ThemeTokens.newTextPaint();
        exitText.setTextSize(20f);
        exitText.setColor(ThemeTokens.TEXT_PRIMARY);
        Paint.FontMetrics efm = exitText.getFontMetrics();
        float exitTextY = clipExitRect.centerY() - (efm.ascent + efm.descent) / 2;
        canvas.drawText("退出", clipExitRect.centerX() - exitText.measureText("退出") / 2, exitTextY, exitText);

        java.util.List<Integer> pinnedIdx = new java.util.ArrayList<>();
        java.util.List<Integer> regularIdx = new java.util.ArrayList<>();
        if (history != null) {
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).slotOwner != currentSlot) continue;
                if (history.get(i).pinned) pinnedIdx.add(i); else regularIdx.add(i);
            }
        }
        if (pinnedIdx.isEmpty() && regularIdx.isEmpty()) {
            Paint emptyPaint = ThemeTokens.newTextPaint();
            emptyPaint.setTextSize(20f);
            emptyPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            canvas.drawText("剪贴板为空", w / 2f - 60, topBarHeight + (h - topBarHeight) / 2f, emptyPaint);
            return;
        }
        int regularVisible = Math.min(regularIdx.size(), MAX_VISIBLE_CLIPBOARD_ITEMS);
        java.util.List<Integer> rows = new java.util.ArrayList<>();
        rows.addAll(pinnedIdx);
        for (int i = regularIdx.size() - regularVisible; i < regularIdx.size(); i++) {
            rows.add(regularIdx.get(i));
        }

        float listTop = topBarHeight;
        float listHeight = h - topBarHeight;
        float itemHeight = listHeight / rows.size();
        Paint bgPaint = ThemeTokens.newBgPaint();
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(18f);
        Paint borderPaint = ThemeTokens.newBorderPaint();
        borderPaint.setColor(ThemeTokens.BORDER);
        Paint btnTextPaint = ThemeTokens.newTextPaint();
        btnTextPaint.setTextSize(15f);

        int btnW = 90;

        for (int row = 0; row < rows.size(); row++) {
            int realIdx = rows.get(row);
            SimpleImeService.ClipboardEntry entry = history.get(realIdx);
            float top = listTop + row * itemHeight;
            float bottom = top + itemHeight;

            bgPaint.setColor(ThemeTokens.SURFACE_RAISED);
            canvas.drawRect(0, top, w, bottom, bgPaint);
            canvas.drawLine(0, bottom, w, bottom, borderPaint);

            int pinBtnLeft = w - btnW * 2;
            int delBtnLeft = w - btnW;

            Rect textRect = new Rect(0, (int) top, pinBtnLeft, (int) bottom);
            Rect pinRect = new Rect(pinBtnLeft, (int) top, delBtnLeft, (int) bottom);
            Rect delRect = new Rect(delBtnLeft, (int) top, w, (int) bottom);
            clipTextRects.add(textRect);
            clipPinRects.add(pinRect);
            clipDeleteRects.add(delRect);
            clipRowRealIndex.add(realIdx);

            String prefix = entry.pinned ? "* " : "";
            String display = prefix + entry.text.replace("\n", " ");
            int maxChars = Math.max(4, (pinBtnLeft - 16) / 20);
            if (display.length() > maxChars) display = display.substring(0, maxChars) + "...";
            textPaint.setColor(entry.pinned ? 0xFFFFC107 : ThemeTokens.TEXT_PRIMARY);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = top + itemHeight / 2 - (fm.ascent + fm.descent) / 2;
            canvas.drawText(display, 16, textY, textPaint);

            canvas.drawLine(pinBtnLeft, top, pinBtnLeft, bottom, borderPaint);
            canvas.drawLine(delBtnLeft, top, delBtnLeft, bottom, borderPaint);

            btnTextPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            String pinLabel = entry.pinned ? "取消" : "固定";
            Paint.FontMetrics bfm = btnTextPaint.getFontMetrics();
            float btnTextY = top + itemHeight / 2 - (bfm.ascent + bfm.descent) / 2;
            canvas.drawText(pinLabel, pinBtnLeft + btnW / 2f - btnTextPaint.measureText(pinLabel) / 2, btnTextY, btnTextPaint);
            canvas.drawText("删除", delBtnLeft + btnW / 2f - btnTextPaint.measureText("删除") / 2, btnTextY, btnTextPaint);
        }
    }

    public ClipboardHit hitTestClipboard(float x, float y) {
        ClipboardHit hit = new ClipboardHit();
        if (clipExitRect.contains((int) x, (int) y)) {
            hit.action = ClipboardHit.EXIT;
            return hit;
        }
        for (int row = 0; row < clipTextRects.size(); row++) {
            int realIdx = clipRowRealIndex.get(row);
            if (clipPinRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.PIN;
                hit.index = realIdx;
                return hit;
            }
            if (clipDeleteRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.DELETE;
                hit.index = realIdx;
                return hit;
            }
            if (clipTextRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.PASTE;
                hit.index = realIdx;
                return hit;
            }
        }
        return hit;
    }
}
