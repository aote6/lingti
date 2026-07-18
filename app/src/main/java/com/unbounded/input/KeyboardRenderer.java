package com.unbounded.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

public class KeyboardRenderer {
    static final int COLOR_BG = 0xFF000000;
    static final int COLOR_KEY_BG = 0xFF020705;
    static final int COLOR_BORDER = 0xFF081C12;
    static final int COLOR_ACTIVE = 0xFF55EEAA;
    static final int COLOR_MAIN = 0xFF22CC88;
    static final int COLOR_SUB = 0xFF1C9E6A;
    static final int COLOR_SHADOW = 0xFF0A3D28;
    private static final int COLOR_POPUP_BG = 0xFF030D08;

    private final Paint bgPaint, textPaint, borderPaint, popupPaint;

    public KeyboardRenderer() {
        bgPaint = new Paint(); bgPaint.setAntiAlias(true);
        borderPaint = new Paint(); borderPaint.setStyle(Paint.Style.STROKE); borderPaint.setStrokeWidth(1.5f); borderPaint.setAntiAlias(true);
        popupPaint = new Paint(); popupPaint.setAntiAlias(true);
        textPaint = new Paint(); textPaint.setTypeface(Typeface.MONOSPACE); textPaint.setAntiAlias(true);
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
                              List<Rect> candidateRects) {
        canvas.drawColor(COLOR_BG);
        // 候选栏分隔线
        borderPaint.setColor(COLOR_BORDER);
        canvas.drawLine(0, candidateBarHeight, canvas.getWidth(), candidateBarHeight, borderPaint);

        // 候选词
        candidateRects.clear();
        if (composingDigits.length() > 0) {
            float currentX = 30f;
            textPaint.setTextSize(candidateBarHeight * 0.5f);
            textPaint.setTextAlign(Paint.Align.LEFT);
            float yOffset = candidateBarHeight * 0.65f;
            textPaint.setColor(COLOR_ACTIVE);
            for (String cand : candidates) {
                float w = textPaint.measureText(cand);
                candidateRects.add(new Rect((int) currentX - 15, 0, (int) (currentX + w + 15), (int) candidateBarHeight));
                canvas.drawText(cand, currentX, yOffset, textPaint);
                currentX += w + 45f;
            }
        }

        // 键位
        if (keys.isEmpty()) return;
        float remainingHeight = canvas.getHeight() - candidateBarHeight;
        int rows = keys.size() / NineKeyKeyboard.COLS;
        float kh = remainingHeight / rows;
        for (KeySlot k : keys) {
            Rect r = k.rect;
            float l = r.left, t = r.top, r_ = r.right, b = r.bottom;
            boolean pressed = (k == activeKey && !isLongPressed);
            bgPaint.setColor(pressed ? COLOR_SHADOW : COLOR_KEY_BG);
            canvas.drawRect(l, t, r_, b, bgPaint);
            borderPaint.setColor(pressed ? COLOR_ACTIVE : COLOR_BORDER);
            canvas.drawRect(l, t, r_, b, borderPaint);
            float cx = (l + r_) / 2f, cy = (t + b) / 2f;
            String rawLabel = cmdLabel(k.tap);
            String mainStr = convertToT9Label(rawLabel);
            if (mainStr != null) {
                textPaint.setColor(pressed ? COLOR_ACTIVE : COLOR_MAIN);
                textPaint.setTextSize(kh * 0.3f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mainStr, cx, cy + kh * 0.08f, textPaint);
            }
            String upNum = cmdLabel(k.swipeUp);
            if (upNum != null && upNum.matches("[0-9]")) {
                textPaint.setColor(COLOR_SUB);
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
        popupPaint.setColor(COLOR_POPUP_BG); popupPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        popupPaint.setColor(COLOR_ACTIVE); popupPaint.setStyle(Paint.Style.STROKE); popupPaint.setStrokeWidth(3f);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        float itemWidth = boxWidth / items.length;
        textPaint.setTextSize(boxHeight * 0.5f); textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < items.length; i++) {
            float ix = boxX + i * itemWidth + itemWidth / 2f;
            if (i == selectedIndex) {
                popupPaint.setColor(COLOR_SHADOW); popupPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(boxX + i * itemWidth, boxY, boxX + (i + 1) * itemWidth, boxY + boxHeight, popupPaint);
                textPaint.setColor(COLOR_ACTIVE);
            } else textPaint.setColor(COLOR_SUB);
            canvas.drawText(items[i], ix, boxY + boxHeight * 0.65f, textPaint);
        }
    }
}
