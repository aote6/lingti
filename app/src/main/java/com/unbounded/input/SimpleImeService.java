package com.unbounded.input;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleImeService extends InputMethodService {
    private static File logFile;
    private NineKeyKeyboard keyboardView;
    private final Handler focusHandler = new Handler(Looper.getMainLooper());

    public static void log(Context ctx, String msg) {
        try {
            if (logFile == null) {
                File dir = ctx.getExternalFilesDir(null);
                if (dir != null) logFile = new File(dir, "lingti_debug.log");
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
                    e.printStackTrace(new PrintWriter(sw));
                    if (logFile == null) logFile = new File(getExternalFilesDir(null), "lingti_debug.log");
                    FileOutputStream fos = new FileOutputStream(logFile, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                    fos.write((time + " [CRASH] " + sw.toString() + "\n").getBytes());
                    fos.close();
                } catch (Exception ignored) {}
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        });
    }

    @Override
    public boolean onEvaluateInputViewShown() { return true; }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        focusHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && keyboardView != null) imm.restartInput(keyboardView);
                if (keyboardView != null) { keyboardView.requestLayout(); keyboardView.invalidate(); }
            }
        }, 100);
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        if (keyboardView != null) { keyboardView.requestLayout(); keyboardView.invalidate(); }
    }

    @Override
    public View onCreateInputView() {
        FrameLayout container = new FrameLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) { return true; }
        };
        int h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));

        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        List<RuleLoader.KeyDef> loadedDefs = RuleLoader.load(this, "default.json");
        keyboardView = new NineKeyKeyboard(this, dispatcher, loadedDefs);
        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        container.addView(keyboardView);
        return container;
    }

    @Override
    public void onDestroy() {
        focusHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
