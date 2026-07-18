package com.unbounded.input;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleImeService extends InputMethodService {
    private static File logFile;
    private NineKeyKeyboard keyboardView;
    private final Handler focusHandler = new Handler(Looper.getMainLooper());
    private String currentContext = "chinese";
    private SharedPreferences prefs;

    private static String detectContext(EditorInfo info) {
        if (info == null) return "chinese";
        String pkg = info.packageName;
        if (pkg == null) return "chinese";
        if (pkg.contains("termux") || pkg.contains("terminal") || pkg.contains("ssh")) {
            return "terminal";
        }
        if (pkg.contains("editor") || pkg.contains("code") || pkg.contains("vscode")) {
            return "english";
        }
        return "chinese";
    }

    private static String configFileForContext(String context) {
        switch (context) {
            case "terminal": return "default_terminal.json";
            case "english": return "default_english.json";
            case "chinese":
            default: return "default.json";
        }
    }

    private int getKeyboardHeight() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        int dp = prefs.getInt("keyboard_height", 280);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

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
        prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
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

        String detected = detectContext(info);
        // 如果用户没设置默认模式，用场景检测；否则非终端/编辑器场景用用户默认
        String defaultMode = prefs.getString("default_mode", "");
        if (!"terminal".equals(detected) && !"english".equals(detected) && !defaultMode.isEmpty()) {
            detected = defaultMode;
        }

        if (!detected.equals(currentContext) || keyboardView == null) {
            currentContext = detected;
            log(this, "场景切换: " + info.packageName + " -> " + currentContext);
            rebuildKeyboard();
        }

        if (keyboardView != null) {
            keyboardView.resetSession();
        }
        focusHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (keyboardView != null) {
                    keyboardView.requestLayout();
                    keyboardView.invalidate();
                }
            }
        }, 100);
    }

    private void rebuildKeyboard() {
        View root = getWindow().getWindow().getDecorView().findViewById(android.R.id.content);
        if (root instanceof FrameLayout) {
            FrameLayout container = (FrameLayout) root;
            container.removeAllViews();

            int h = getKeyboardHeight();

            KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
                @Override
                public void onCommand(Command cmd) {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) InputEngine.execute(ic, cmd);
                }
            };

            String configFile = configFileForContext(currentContext);
            RuleLoader.LayoutConfig layoutConfig = RuleLoader.load(this, configFile);
            keyboardView = new NineKeyKeyboard(this, dispatcher, layoutConfig.keys);

            if ("english".equals(layoutConfig.context)) {
                keyboardView.setInputMode(NineKeyKeyboard.InputMode.ENGLISH);
            } else if ("terminal".equals(layoutConfig.context)) {
                keyboardView.setInputMode(NineKeyKeyboard.InputMode.TERMINAL);
            } else {
                keyboardView.setInputMode(NineKeyKeyboard.InputMode.CHINESE);
            }

            keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
            container.addView(keyboardView);
        }
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        if (keyboardView != null) {
            keyboardView.resetSession();
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (keyboardView != null) {
            keyboardView.resetSession();
        }
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                   int newSelStart, int newSelEnd,
                                   int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                                candidatesStart, candidatesEnd);
        if (keyboardView != null && oldSelStart != newSelStart) {
            keyboardView.resetSession();
        }
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        if (keyboardView != null) {
            keyboardView.requestLayout();
            keyboardView.invalidate();
        }
    }

    @Override
    public View onCreateInputView() {
        FrameLayout container = new FrameLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) { return true; }
        };
        int h = getKeyboardHeight();
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));

        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        RuleLoader.LayoutConfig layoutConfig = RuleLoader.load(this, "default.json");
        keyboardView = new NineKeyKeyboard(this, dispatcher, layoutConfig.keys);
        currentContext = layoutConfig.context;

        if ("english".equals(layoutConfig.context)) {
            keyboardView.setInputMode(NineKeyKeyboard.InputMode.ENGLISH);
        } else if ("terminal".equals(layoutConfig.context)) {
            keyboardView.setInputMode(NineKeyKeyboard.InputMode.TERMINAL);
        } else {
            keyboardView.setInputMode(NineKeyKeyboard.InputMode.CHINESE);
        }

        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        container.addView(keyboardView);
        return container;
    }

    @Override
    public void onDestroy() {
        focusHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // 供 MainActivity 调用：打开设置界面
    public static void openSettings(Context ctx) {
        Intent intent = new Intent(ctx, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
