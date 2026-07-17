package com.unbounded.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleImeService extends InputMethodService {

    private static File logFile;

    public static void log(Context ctx, String msg) {
        try {
            if (logFile == null) {
                File dir = ctx.getExternalFilesDir(null);
                if (dir != null) {
                    logFile = new File(dir, "lingti_debug.log");
                }
            }
            if (logFile != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                FileOutputStream fos = new FileOutputStream(logFile, true);
                fos.write((time + " [INFO] " + msg + "\n").getBytes());
                fos.close();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString();

                    if (logFile == null) {
                        logFile = new File(getExternalFilesDir(null), "lingti_debug.log");
                    }
                    FileOutputStream fos = new FileOutputStream(logFile, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                    fos.write((time + " [CRASH] Thread: " + t.getName() + "\n" + stackTrace + "\n").getBytes());
                    fos.close();
                } catch (Exception ignored) {}

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        });

        log(this, "SimpleImeService onCreate 初始化成功");
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        return true;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        log(this, "onStartInputView 唤起键盘");
    }

    @Override
    public View onCreateInputView() {
        log(this, "onCreateInputView 开始构建 UI");
        FrameLayout container = new FrameLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return true;
            }
        };

        int heightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPx));

        NineKeyKeyboard keyboard = new NineKeyKeyboard(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
        lp.gravity = Gravity.BOTTOM;
        keyboard.setLayoutParams(lp);

        container.addView(keyboard);
        return container;
    }

    private class NineKeyKeyboard extends View {
        private List<KeySlot> keys;
        private Paint bgPaint, textPaint, subPaint;
        private Vibrator vibrator;
        private KeySlot activeKey;
        private boolean isGestureConsumed = false;
        private GestureRecognizer recognizer;

        private static final int ROWS = 4;
        private static final int COLS = 3;
        private static final int KEY_PADDING = 3;

        public NineKeyKeyboard(Context context) {
            super(context);

            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            bgPaint = new Paint(); bgPaint.setAntiAlias(true);
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
            subPaint = new Paint();
            subPaint.setColor(Color.rgb(140, 140, 140));
            subPaint.setTextAlign(Paint.Align.CENTER);
            subPaint.setAntiAlias(true);
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            recognizer = new GestureRecognizer();
            keys = new ArrayList<>();
            initKeys();
        }

        private void initKeys() {
            keys.clear();
            String[][] labels = {
                {"A", "1", "~", "`", "Esc"},
                {"B", "2", "@", "#", "$", "Tab"},
                {"C", "3", "%", "^", "&", "/"},
                {"D", "4", "*", "(", ")", "-"},
                {"E", "5", "_", "+", "=", "{"},
                {"F", "6", "[", "]", "\\", "}"},
                {"G", "7", "<", ">", "|", ":"},
                {"H", "8", "\"", "'", ";", ","},
                {"I", "9", ".", "?", "!", "Enter"},
                {"*", "*", "+", "-", "/", "Del"},
                {"0", "空格", "0", ".", ",", "."},
                {"#", "#", "布局", "", "", "布局"},
            };
            int[][] gridIndex = {{0,1,2},{3,4,5},{6,7,8},{9,10,11}};
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int idx = gridIndex[row][col];
                    KeySlot key = new KeySlot();
                    key.row = row; key.col = col;
                    key.tap = labels[idx][0];
                    key.swipeUp = labels[idx][1];
                    key.swipeDown = labels[idx][2];
                    key.swipeLeft = labels[idx][3];
                    key.longPress = labels[idx][4];
                    keys.add(key);
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (keys.isEmpty()) return;
            float keyW = (float) getWidth() / COLS;
            float keyH = (float) getHeight() / ROWS;
            textPaint.setTextSize(keyH * 0.3f);
            subPaint.setTextSize(keyH * 0.15f);
            for (KeySlot key : keys) {
                float left = key.col * keyW + KEY_PADDING;
                float top = key.row * keyH + KEY_PADDING;
                float right = (key.col + 1) * keyW - KEY_PADDING;
                float bottom = (key.row + 1) * keyH - KEY_PADDING;
                key.rect.set((int) left, (int) top, (int) right, (int) bottom);

                boolean isPressed = (key == activeKey && !isGestureConsumed);
                bgPaint.setColor(isPressed ? Color.rgb(70, 70, 70) : Color.rgb(42, 42, 42));
                canvas.drawRoundRect(left, top, right, bottom, 6, 6, bgPaint);

                float cx = (left + right) / 2f;
                float cy = (top + bottom) / 2f;
                canvas.drawText(key.tap, cx, cy + textPaint.getTextSize() / 3f, textPaint);
                subPaint.setColor(Color.rgb(100, 200, 255));
                if (key.swipeUp != null && !key.swipeUp.isEmpty())
                    canvas.drawText(key.swipeUp, cx, top + keyH * 0.2f, subPaint);
                subPaint.setColor(Color.rgb(140, 140, 140));
                if (key.swipeDown != null && !key.swipeDown.isEmpty())
                    canvas.drawText(key.swipeDown, cx, bottom - keyH * 0.06f, subPaint);
                if (key.swipeLeft != null && !key.swipeLeft.isEmpty())
                    canvas.drawText(key.swipeLeft, left + keyW * 0.16f, cy + subPaint.getTextSize() / 3f, subPaint);
                if (key.longPress != null && !key.longPress.isEmpty()) {
                    subPaint.setColor(Color.rgb(255, 180, 80));
                    canvas.drawText(key.longPress, cx, bottom - keyH * 0.06f, subPaint);
                }
            }
        }

        private KeySlot findKey(float x, float y) {
            KeySlot closest = null;
            float minDistance = Float.MAX_VALUE;
            for (KeySlot key : keys) {
                if (key.rect.contains((int) x, (int) y)) {
                    return key;
                }
                float cx = key.rect.centerX();
                float cy = key.rect.centerY();
                float dist = (cx - x) * (cx - x) + (cy - y) * (cy - y);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = key;
                }
            }
            return closest;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    activeKey = findKey(x, y);
                    isGestureConsumed = false;
                    if (activeKey != null) {
                        recognizer.onDown(x, y);
                        invalidate();
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (activeKey == null) return true;
                    if (isGestureConsumed) return true;

                    GestureRecognizer.Gesture g = recognizer.onMove(x, y);
                    if (g != GestureRecognizer.Gesture.NONE) {
                        SimpleImeService.log(getContext(), "触发手势: " + g);
                        executeGesture(g);
                        isGestureConsumed = true;
                        invalidate();
                    } else if (recognizer.shouldCheckLongPress()) {
                        if (recognizer.checkLongPress() != GestureRecognizer.Gesture.NONE) {
                            SimpleImeService.log(getContext(), "触发长按");
                            executeGesture(GestureRecognizer.Gesture.LONG_PRESS);
                            isGestureConsumed = true;
                            invalidate();
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (activeKey != null && !isGestureConsumed) {
                        GestureRecognizer.Gesture finalGesture = recognizer.onUp();
                        SimpleImeService.log(getContext(), "手指抬起，最终手势: " + finalGesture);
                        if (finalGesture != GestureRecognizer.Gesture.NONE) {
                            executeGesture(finalGesture);
                        }
                    }
                    activeKey = null;
                    isGestureConsumed = false;
                    recognizer.reset();
                    invalidate();
                    return true;
            }
            return true;
        }

        private void executeGesture(GestureRecognizer.Gesture gesture) {
            if (activeKey == null) return;
            String text = null;
            switch (gesture) {
                case TAP: text = activeKey.tap; break;
                case SWIPE_UP: text = activeKey.swipeUp; break;
                case SWIPE_DOWN: text = activeKey.swipeDown; break;
                case SWIPE_LEFT: text = activeKey.swipeLeft; break;
                case LONG_PRESS: text = activeKey.longPress; break;
            }
            if (text == null || text.isEmpty()) return;

            SimpleImeService.log(getContext(), "准备提交字符: [" + text + "]");
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) {
                SimpleImeService.log(getContext(), "警告：InputConnection 为空！");
                return;
            }

            ic.beginBatchEdit();
            switch (text) {
                case "空格": ic.commitText(" ", 1); break;
                case "Enter": ic.commitText("\n", 1); break;
                case "Del": ic.deleteSurroundingText(1, 0); break;
                case "Tab": ic.commitText("\t", 1); break;
                case "Esc": ic.commitText("[Esc]", 1); break;
                case "布局": break;
                default: ic.commitText(text, 1); break;
            }
            ic.endBatchEdit();

            if (vibrator != null)
                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    static class KeySlot {
        int row, col;
        Rect rect = new Rect();
        String tap, swipeUp, swipeDown, swipeLeft, longPress;
    }
}
