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

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.layouts.qwerty.Qwerty26Layout;
import com.unbounded.input.layouts.terminal.UnexpectedTerminalLayout;

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
    private String currentLayout = "ninekey";
    private String currentBehavior = "t9";
    private SharedPreferences prefs;
    private Qwerty26Layout qwertyLayout;
    private FrameLayout inputRoot;

    private static String detectContext(EditorInfo info) {
        if (info == null) return "chinese";
        String pkg = info.packageName;
        if (pkg == null) return "chinese";
        if (pkg.contains("termux") || pkg.contains("terminal") || pkg.contains("ssh")) {
            return "terminal";
        }
        if (pkg.contains("editor") || pkg.contains("code") || pkg.contains("vscode")) {
            return "editor";
        }
        return "chinese";
    }

    private int getKeyboardHeight() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        int dp = prefs.getInt("keyboard_height", 280);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private KeyboardLayout resolveLayout(String layoutId) {
        switch (layoutId) {
            case "qwerty26":
                if (qwertyLayout == null) qwertyLayout = new Qwerty26Layout();
                return qwertyLayout;
            case "unexpected_terminal": return new UnexpectedTerminalLayout();
            default: return null;
        }
    }

    private NineKeyKeyboard.InputMode resolveInputMode(String layoutId, String behavior) {
        if ("unexpected_terminal".equals(layoutId) || "keyevent".equals(behavior)) {
            return NineKeyKeyboard.InputMode.TERMINAL;
        }
        if ("qwerty26".equals(layoutId) || "direct".equals(behavior)) {
            return NineKeyKeyboard.InputMode.ENGLISH;
        }
        return NineKeyKeyboard.InputMode.CHINESE;
    }

    private void applyThemeFromPrefs() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        String name = prefs.getString("theme", "Default");
        ThemeTokens.getManager().setByName(name);
        ThemeTokens.refresh();
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
        applyThemeFromPrefs();
        T9Engine.init(this);
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

        applyThemeFromPrefs();

        String context = detectContext(info);
        String layout = prefs.getString("default_layout", "ninekey");
        String behavior = prefs.getString("default_behavior", "t9");

        if ("terminal".equals(context)) {
            layout = "unexpected_terminal";
            behavior = "keyevent";
        } else if ("editor".equals(context)) {
            layout = "qwerty26";
            behavior = "direct";
        }

        if (!layout.equals(currentLayout) || !behavior.equals(currentBehavior) || keyboardView == null) {
            currentLayout = layout;
            currentBehavior = behavior;
            log(this, "场景: " + context + " -> " + currentLayout + "/" + currentBehavior);
            rebuildKeyboard();
        }

        if (keyboardView != null) keyboardView.resetSession();
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
        if (inputRoot == null) return;
        inputRoot.removeAllViews();
        int h = getKeyboardHeight();

        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                if (cmd.type == Command.Type.SHIFT_TOGGLE) {
                    if (qwertyLayout != null) qwertyLayout.toggleShift();
                    rebuildKeyboard();
                    return;
                }
                if (cmd.type == Command.Type.SYMBOL_TOGGLE) {
                    if (qwertyLayout != null) qwertyLayout.toggleSymbol();
                    rebuildKeyboard();
                    return;
                }
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        KeyboardLayout layout = resolveLayout(currentLayout);
        NineKeyKeyboard.InputMode mode = resolveInputMode(currentLayout, currentBehavior);

        if (layout != null) {
            LayoutProfile profile = layout.build();
            keyboardView = new NineKeyKeyboard(this, dispatcher, profile);
        } else {
            String configFile = "default.json";
            if ("keyevent".equals(currentBehavior)) configFile = "default_ninekey_terminal.json";
            else if ("direct".equals(currentBehavior)) configFile = "default_english.json";
            RuleLoader.LayoutConfig layoutConfig = RuleLoader.load(this, configFile);
            List<KeyModel> keys = layoutConfig.toKeyModels();
            LayoutProfile profile = new LayoutProfile("inline");
            int rows = (int) Math.ceil((float) keys.size() / 3);
            for (int r = 0; r < rows; r++) {
                com.unbounded.input.core.layout.RowSpec row = new com.unbounded.input.core.layout.RowSpec();
                for (int c = 0; c < 3; c++) {
                    int idx = r * 3 + c;
                    if (idx < keys.size()) row.add(keys.get(idx));
                }
                profile.addRow(row);
            }
            keyboardView = new NineKeyKeyboard(this, dispatcher, profile);
        }
        keyboardView.setInputMode(mode);
        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        inputRoot.addView(keyboardView);
    }


    @Override
    public void onFinishInput() {
        super.onFinishInput();
        if (keyboardView != null) keyboardView.resetSession();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (keyboardView != null) keyboardView.resetSession();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                   int newSelStart, int newSelEnd,
                                   int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                                candidatesStart, candidatesEnd);
        if (keyboardView != null && oldSelStart != newSelStart) keyboardView.resetSession();
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        if (keyboardView != null) { keyboardView.requestLayout(); keyboardView.invalidate(); }
    }

    @Override
    public View onCreateInputView() {
        inputRoot = new FrameLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) { return true; }
        };
        int h = getKeyboardHeight();
        inputRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));

        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        RuleLoader.LayoutConfig layoutConfig = RuleLoader.load(this, "default.json");
        List<KeyModel> keys = layoutConfig.toKeyModels();
        LayoutProfile profile = new LayoutProfile("inline");
        int rows = (int) Math.ceil((float) keys.size() / 3);
        for (int r = 0; r < rows; r++) {
            com.unbounded.input.core.layout.RowSpec row = new com.unbounded.input.core.layout.RowSpec();
            for (int c = 0; c < 3; c++) {
                int idx = r * 3 + c;
                if (idx < keys.size()) row.add(keys.get(idx));
            }
            profile.addRow(row);
        }
        keyboardView = new NineKeyKeyboard(this, dispatcher, profile);
        keyboardView.setInputMode(NineKeyKeyboard.InputMode.CHINESE);
        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        inputRoot.addView(keyboardView);
        return inputRoot;
    }

    @Override
    public void onDestroy() {
        focusHandler.removeCallbacksAndMessages(null);
        T9Engine.save();
        super.onDestroy();
    }

    public static void openSettings(Context ctx) {
        Intent intent = new Intent(ctx, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
