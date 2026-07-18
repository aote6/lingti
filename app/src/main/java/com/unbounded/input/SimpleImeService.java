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

    // 场景识别：根据 App 包名返回 context
    private static String detectContext(EditorInfo info) {
        if (info == null) return "chinese";
        String pkg = info.packageName;
        if (pkg == null) return "chinese";
        // 终端类 App
        if (pkg.contains("termux") || pkg.contains("terminal") || pkg.contains("ssh")) {
            return "english";
        }
        // 代码编辑器
        if (pkg.contains("editor") || pkg.contains("code") || pkg.contains("vscode")) {
            return "english";
        }
        return "chinese";
    }

    // 根据 context 选择配置文件
    private static String configFileForContext(String context) {
        switch (context) {
            case "english": return "default_english.json";
            case "chinese": 
            default: return "default.json";
        }
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
        // 重新加载配置并重建键盘 View
        View root = getWindow().getWindow().getDecorView().findViewById(android.R.id.content);
        if (root instanceof FrameLayout) {
            FrameLayout container = (FrameLayout) root;
            container.removeAllViews();

            int h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());

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
        int h = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
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
}
