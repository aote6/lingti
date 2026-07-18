package com.unbounded.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.List;

public class KeyboardRenderer {
    private final Paint bgPaint, textPaint, borderPaint, popupPaint;

    public KeyboardRenderer() {
        bgPaint = ThemeTokens.newBgPaint();
        borderPaint = ThemeTokens.newBorderPaint();
        popupPaint = ThemeTokens.newBgPaint();
        textPaint = ThemeTokens.newTextPaint();
    }

    public static String convertToT9Label(String rawLabel) {
        if (rawLabel == null) return null;
        switch (rawLabel) { case "2": return "ABC"; case "3": return "DEF"; case "4": return "GHI"; case "5": return "JKL"; case "6": return "MNO"; case "7": return "PQRS"; case "8": return "TUV"; case "9": return "WXYZ"; default: return rawLabel; }
    }

    public static String cmdLabel(Command cmd) {
        if (cmd == null) return null;
        if (cmd.type == Command.Type.INSERT_TEXT && !cmd.text.isEmpty()) return cmd.text;
        return cmd.type.name().toLowerCase();
    }

    public static String[] getPopupItemsForKey(KeySlot k) {
        String main = convertToT9Label(cmdLabel(k.tap));
        if (main == null) return new String[]{"?"};
        switch (main) {
            case "ABC": return new String[]{"A","B","C","2","a","b","c"};
            case "DEF": return new String[]{"D","E","F","3","d","e","f"};
            case "GHI": return new String[]{"G","H","I","4","g","h","i"};
            case "JKL": return new String[]{"J","K","L","5","j","k","l"};
            case "MNO": return new String[]{"M","N","O","6","m","n","o"};
            case "PQRS": return new String[]{"P","Q","R","S","7"};
            case "TUV": return new String[]{"T","U","V","8"};
            case "WXYZ": return new String[]{"W","X","Y","Z","9"};
            default: return new String[]{main};
        }
    }

    public static boolean isNumeric(String str) { return str != null && str.matches("[0-9]"); }

    public void drawKeyboard(Canvas canvas, List<KeySlot> keys, float candidateBarHeight,
                              KeySlot activeKey, boolean isLongPressed,
                              StringBuilder composingDigits, List<String> candidates,
                              List<Rect> candidateRects,
                              int currentPage, int totalPages) {
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

            // 翻页指示器
            if (totalPages > 1) {
                float pageY = candidateBarHeight * 0.25f;
                textPaint.setTextSize(candidateBarHeight * 0.3f);
                textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
                textPaint.setTextAlign(Paint.Align.RIGHT);
                String pageInfo = (currentPage + 1) + "/" + totalPages;
                canvas.drawText(pageInfo, canvas.getWidth() - 20f, pageY, textPaint);
            }
        }

        if (keys.isEmpty()) return;
        float remainingHeight = canvas.getHeight() - candidateBarHeight;
        int rows = keys.size() / NineKeyKeyboard.COLS;
        float kh = remainingHeight / rows;
        for (KeySlot k : keys) {
            Rect r = k.rect;
            float l = r.left, t = r.top, r_ = r.right, b = r.bottom;
            boolean pressed = (k == activeKey && !isLongPressed);
            bgPaint.setColor(pressed ? ThemeTokens.PRESS_BG : ThemeTokens.SURFACE);
            canvas.drawRect(l, t, r_, b, bgPaint);
            borderPaint.setColor(pressed ? ThemeTokens.BORDER_ACTIVE : ThemeTokens.BORDER);
            canvas.drawRect(l, t, r_, b, borderPaint);
            float cx = (l + r_) / 2f, cy = (t + b) / 2f;
            String mainStr = convertToT9Label(cmdLabel(k.tap));
            if (mainStr != null) {
                textPaint.setColor(pressed ? ThemeTokens.TEXT_ACCENT : ThemeTokens.TEXT_PRIMARY);
                textPaint.setTextSize(kh * 0.3f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mainStr, cx, cy + kh * 0.08f, textPaint);
            }
            String upNum = cmdLabel(k.swipeUp);
            if (upNum != null && upNum.matches("[0-9]")) {
                textPaint.setColor(ThemeTokens.TEXT_SECONDARY);
                textPaint.setTextSize(kh * 0.18f);
                canvas.drawText(upNum, cx, t + kh * 0.22f, textPaint);
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
