// IME服务入口：生命周期管理、键盘构建、剪贴板、日志
package com.unbounded.input;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;

import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.layouts.terminal.UnexpectedTerminalLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class SimpleImeService extends InputMethodService {
    private static File logFile;
    private NineKeyKeyboard keyboardView;
    private final Handler focusHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private FrameLayout inputRoot;
    private static final java.util.List<String> clipboardHistory = new ArrayList<>();
    private static final int MAX_CLIPBOARD_HISTORY = 20;
    private ClipboardManager clipboardManager;

    public static java.util.List<String> getClipboardHistory() { return clipboardHistory; }

    private void pasteRecentClipboard() {
        if (clipboardHistory.isEmpty()) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(clipboardHistory.get(clipboardHistory.size() - 1), 1);
    }

    public void pasteClipboardItem(int index) {
        if (index < 0 || index >= clipboardHistory.size()) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(clipboardHistory.get(index), 1);
    }

    private void initClipboard() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData clip = clipboardManager.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        String text = clip.getItemAt(0).getText().toString();
                        if (text != null && !text.isEmpty() && !text.equals(clipboardHistory.isEmpty() ? "" : clipboardHistory.get(clipboardHistory.size() - 1))) {
                            clipboardHistory.add(text);
                            if (clipboardHistory.size() > MAX_CLIPBOARD_HISTORY) {
                                clipboardHistory.remove(0);
                            }
                        }
                    }
                }
            });
        }
    }

    private int getKeyboardHeight() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        int dp = prefs.getInt("keyboard_height", 280);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void applyThemeFromPrefs() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        String name = prefs.getString("theme", "Default");
        ThemeTokens.getManager().setByName(name);
        ThemeTokens.refresh();
    }

    public static void log(Context ctx, String msg) {
        Log.e("Lingti", msg);
        try {
            if (logFile == null) {
                File dir = ctx != null ? ctx.getFilesDir() : null;
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
        initClipboard();
        log(this, "灵体终端键盘启动");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    if (logFile == null) logFile = new File(getFilesDir(), "lingti_debug.log");
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
        initClipboard();
        if (keyboardView == null) rebuildKeyboard();
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
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        KeyboardLayout layout = new UnexpectedTerminalLayout();
        LayoutProfile profile = layout.build();
        keyboardView = new NineKeyKeyboard(this, dispatcher, profile);
        keyboardView.setInputMode(NineKeyKeyboard.InputMode.TERMINAL);
        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        inputRoot.addView(keyboardView);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.finishComposingText();
        if (keyboardView != null) keyboardView.resetSession();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (keyboardView != null) keyboardView.resetSession();
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

        KeyboardLayout layout = new UnexpectedTerminalLayout();
        LayoutProfile profile = layout.build();
        keyboardView = new NineKeyKeyboard(this, dispatcher, profile);
        keyboardView.setInputMode(NineKeyKeyboard.InputMode.TERMINAL);
        keyboardView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));
        inputRoot.addView(keyboardView);
        return inputRoot;
    }

    @Override
    public void onDestroy() {
        focusHandler.removeCallbacksAndMessages(null);
        keyboardView = null;
        super.onDestroy();
    }

    public static void openSettings(Context ctx) {
        Intent intent = new Intent(ctx, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
