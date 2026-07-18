package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

interface KeyboardActionDispatcher {
    void onCommand(Command cmd);
}

public class NineKeyKeyboard extends View {
    private List<KeySlot> keys = new ArrayList<>();
    private Paint bgPaint, textPaint, borderPaint, popupPaint;
    private KeySlot activeKey;
    private GestureRecognizer recognizer = new GestureRecognizer();
    private boolean isGestureConsumed, isLongPressed;
    private float startX, startY;
    private int longPressSelectedIndex = -1;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private String[] currentPopupItems;

    private KeyboardActionDispatcher dispatcher;
    private StringBuilder composingDigits = new StringBuilder();
    private List<String> candidates = new ArrayList<>();
    private List<Rect> candidateRects = new ArrayList<>();

    private static final int COLOR_BG = 0xFF000000;
    private static final int COLOR_KEY_BG = 0xFF020705;
    private static final int COLOR_BORDER = 0xFF081C12;
    private static final int COLOR_ACTIVE = 0xFF55EEAA;
    private static final int COLOR_MAIN = 0xFF22CC88;
    private static final int COLOR_SUB = 0xFF1C9E6A;
    private static final int COLOR_SHADOW = 0xFF0A3D28;

    private static final int COLS = 3, PAD = 2;
    private float candidateBarHeight;

    public NineKeyKeyboard(Context context, KeyboardActionDispatcher dispatcher, List<RuleLoader.KeyDef> defs) {
        super(context);
        this.dispatcher = dispatcher;
        bgPaint = new Paint(); bgPaint.setAntiAlias(true);
        borderPaint = new Paint(); borderPaint.setStyle(Paint.Style.STROKE); borderPaint.setStrokeWidth(1.5f); borderPaint.setAntiAlias(true);
        popupPaint = new Paint(); popupPaint.setAntiAlias(true);
        textPaint = new Paint(); textPaint.setTypeface(Typeface.MONOSPACE); textPaint.setAntiAlias(true);
        for (RuleLoader.KeyDef d : defs) { KeySlot k = new KeySlot(); k.tap = d.tap; k.swipeUp = d.swipeUp; k.swipeDown = d.swipeDown; k.swipeLeft = d.swipeLeft; k.swipeRight = d.swipeRight; k.longPress = d.longPress; keys.add(k); }
        longPressRunnable = new Runnable() {
            public void run() {
                if (activeKey != null && !isGestureConsumed) {
                    isLongPressed = true;
                    isGestureConsumed = true;
                    currentPopupItems = getPopupItemsForKey(activeKey);
                    longPressSelectedIndex = 0;
                    invalidate();
                }
            }
        };
    }

    public void resetSession() {
        composingDigits.setLength(0);
        candidates.clear();
        candidateRects.clear();
        activeKey = null;
        isGestureConsumed = false;
        isLongPressed = false;
        longPressSelectedIndex = -1;
        currentPopupItems = null;
        longPressHandler.removeCallbacks(longPressRunnable);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeKeyRects(w, h);
    }

    private void computeKeyRects(int w, int h) {
        if (keys.isEmpty()) return;
        candidateBarHeight = h * 0.16f;
        float remainingHeight = h - candidateBarHeight;
        int rows = keys.size() / COLS;
        float kw = (float) w / COLS;
        float kh = remainingHeight / rows;
        for (int i = 0; i < keys.size(); i++) {
            KeySlot k = keys.get(i);
            int row = i / COLS;
            int col = i % COLS;
            float l = col * kw + PAD;
            float t = candidateBarHeight + row * kh + PAD;
            float r = (col + 1) * kw - PAD;
            float b = candidateBarHeight + (row + 1) * kh - PAD;
            k.rect.set((int) l, (int) t, (int) r, (int) b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(COLOR_BG);
        borderPaint.setColor(COLOR_BORDER);
        canvas.drawLine(0, candidateBarHeight, getWidth(), candidateBarHeight, borderPaint);
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
        if (keys.isEmpty()) return;
        float remainingHeight = getHeight() - candidateBarHeight;
        int rows = keys.size() / COLS;
        float kh = remainingHeight / rows;
        for (int i = 0; i < keys.size(); i++) {
            KeySlot k = keys.get(i);
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
            if (upNum != null && upNum.matches("[0-9]")) { textPaint.setColor(COLOR_SUB); textPaint.setTextSize(kh * 0.18f); canvas.drawText(upNum, cx, t + kh * 0.22f, textPaint); }
        }
        if (isLongPressed && currentPopupItems != null) drawHorizontalPopup(canvas);
    }

    private void drawHorizontalPopup(Canvas canvas) {
        float boxHeight = candidateBarHeight * 1.2f, boxY = candidateBarHeight + 20f;
        float boxWidth = getWidth() * 0.85f, boxX = (getWidth() - boxWidth) / 2f;
        popupPaint.setColor(0xFF030D08); popupPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        popupPaint.setColor(COLOR_ACTIVE); popupPaint.setStyle(Paint.Style.STROKE); popupPaint.setStrokeWidth(3f);
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, popupPaint);
        float itemWidth = boxWidth / currentPopupItems.length;
        textPaint.setTextSize(boxHeight * 0.5f); textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < currentPopupItems.length; i++) {
            float ix = boxX + i * itemWidth + itemWidth / 2f;
            if (i == longPressSelectedIndex) { popupPaint.setColor(COLOR_SHADOW); popupPaint.setStyle(Paint.Style.FILL); canvas.drawRect(boxX + i * itemWidth, boxY, boxX + (i + 1) * itemWidth, boxY + boxHeight, popupPaint); textPaint.setColor(COLOR_ACTIVE); }
            else textPaint.setColor(COLOR_SUB);
            canvas.drawText(currentPopupItems[i], ix, boxY + boxHeight * 0.65f, textPaint);
        }
    }

    private String[] getPopupItemsForKey(KeySlot k) {
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

    private String convertToT9Label(String rawLabel) {
        if (rawLabel == null) return null;
        switch (rawLabel) { case "2": return "ABC"; case "3": return "DEF"; case "4": return "GHI"; case "5": return "JKL"; case "6": return "MNO"; case "7": return "PQRS"; case "8": return "TUV"; case "9": return "WXYZ"; default: return rawLabel; }
    }

    private String cmdLabel(Command cmd) {
        if (cmd == null) return null;
        if (cmd.type == Command.Type.INSERT_TEXT && !cmd.text.isEmpty()) return cmd.text;
        return cmd.type.name().toLowerCase();
    }

    private KeySlot findKey(float x, float y) { for (KeySlot k : keys) if (k.rect.contains((int) x, (int) y)) return k; return null; }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x; startY = y; isLongPressed = false; isGestureConsumed = false;
                if (y < candidateBarHeight && composingDigits.length() > 0) {
                    for (int i = 0; i < candidateRects.size(); i++) {
                        if (candidateRects.get(i).contains((int) x, (int) y)) { dispatcher.onCommand(new InsertText(candidates.get(i))); composingDigits.setLength(0); candidates.clear(); invalidate(); return true; }
                    }
                }
                activeKey = findKey(x, y);
                if (activeKey != null) { recognizer.onDown(x, y); longPressHandler.postDelayed(longPressRunnable, 450); invalidate(); }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activeKey == null) return true;
                if (isLongPressed && currentPopupItems != null) { int idx = (int) ((x - startX) / 50); if (idx < 0) idx = 0; if (idx >= currentPopupItems.length) idx = currentPopupItems.length - 1; if (idx != longPressSelectedIndex) { longPressSelectedIndex = idx; invalidate(); } return true; }
                if (!isGestureConsumed && (Math.abs(x - startX) > 25 || Math.abs(y - startY) > 25)) longPressHandler.removeCallbacks(longPressRunnable);
                if (!isGestureConsumed) { GestureRecognizer.Gesture g = recognizer.onMove(x, y); if (g != GestureRecognizer.Gesture.NONE) { execGesture(g); isGestureConsumed = true; invalidate(); } }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                longPressHandler.removeCallbacks(longPressRunnable);
                if (isLongPressed && currentPopupItems != null && longPressSelectedIndex >= 0) { dispatcher.onCommand(new InsertText(currentPopupItems[longPressSelectedIndex])); isLongPressed = false; currentPopupItems = null; activeKey = null; invalidate(); return true; }
                if (activeKey != null && !isGestureConsumed) { GestureRecognizer.Gesture fg = recognizer.onUp(); if (fg != GestureRecognizer.Gesture.NONE) execGesture(fg); }
                activeKey = null; isGestureConsumed = false; invalidate();
                return true;
        }
        return true;
    }

    private void execGesture(GestureRecognizer.Gesture g) {
        if (activeKey == null || dispatcher == null) return;
        Command cmd = null;
        switch (g) { case TAP: cmd = activeKey.tap; break; case SWIPE_UP: cmd = activeKey.swipeUp; break; case SWIPE_LEFT: cmd = activeKey.swipeLeft; break; case SWIPE_DOWN: cmd = activeKey.swipeDown; break; case SWIPE_RIGHT: cmd = activeKey.swipeRight; break; }
        if (cmd == null) return;
        String rawText = cmdLabel(activeKey.tap);
        if (g == GestureRecognizer.Gesture.TAP && isNumeric(rawText)) { composingDigits.append(rawText); candidates = T9Engine.getCandidates(composingDigits.toString()); invalidate(); return; }
        dispatcher.onCommand(cmd);
    }

    private boolean isNumeric(String str) { return str != null && str.matches("[0-9]"); }
}

class KeySlot {
    Rect rect = new Rect();
    Command tap, swipeUp, swipeDown, swipeLeft, swipeRight, longPress;
}
