// 限速粘贴管理器：终端场景下分块发送，防止PTY缓冲区溢出丢字符
package com.unbounded.input;

import android.content.SharedPreferences;
import android.os.Handler;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PasteManager {
    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int MIN_CHUNK_SIZE = 64;
    private static final int MAX_CHUNK_SIZE = 2048;
    private static final int DEFAULT_DELAY_MS = 15;
    private static final String PREFS_KEY_TERMINAL_PACKAGES = "terminal_packages";
    private static final String PREFS_KEY_CHUNK_DELAY = "paste_chunk_delay";
    private static final String PREFS_KEY_CHUNK_SIZE = "paste_chunk_size";
    private static final String[] DEFAULT_TERMINAL_PACKAGES = {
        "com.termux"
    };
    private static final Object PASTE_TOKEN = new Object();

    private final Handler mainHandler;
    private final SharedPreferences prefs;
    private volatile boolean cancelled = false;
    private int pendingChunkIndex = -1;

    public PasteManager(Handler mainHandler, SharedPreferences prefs) {
        this.mainHandler = mainHandler;
        this.prefs = prefs;
    }

    /**
     * 判断当前编辑器是否属于终端类应用。
     */
    public boolean shouldThrottle(EditorInfo info) {
        if (info == null || info.packageName == null) return false;
        Set<String> packages = prefs.getStringSet(PREFS_KEY_TERMINAL_PACKAGES, null);
        if (packages == null) {
            packages = new HashSet<>(Arrays.asList(DEFAULT_TERMINAL_PACKAGES));
        }
        return packages.contains(info.packageName);
    }

    /**
     * 执行粘贴。
     * @param ic      发起粘贴时的 InputConnection 快照
     * @param text    要粘贴的文本
     * @param throttle 是否启用限速
     */
    public void paste(InputConnection ic, String text, boolean throttle) {
        if (ic == null || text == null || text.isEmpty()) return;

        if (!throttle) {
            try {
                ic.commitText(text, 1);
            } catch (Throwable ignored) {}
            return;
        }
        throttledPaste(ic, text);
    }

    /**
     * 取消正在进行的限速粘贴。
     */
    public void cancel() {
        cancelled = true;
        pendingChunkIndex = -1;
        mainHandler.removeCallbacksAndMessages(PASTE_TOKEN);
    }

    // --- 内部实现 ---

    private void throttledPaste(final InputConnection ic, final String text) {
        cancelled = false;

        int rawSize = prefs.getInt(PREFS_KEY_CHUNK_SIZE, DEFAULT_CHUNK_SIZE);
        int chunkSize = Math.max(MIN_CHUNK_SIZE, Math.min(rawSize, MAX_CHUNK_SIZE));
        int delay = prefs.getInt(PREFS_KEY_CHUNK_DELAY, DEFAULT_DELAY_MS);

        String[] chunks = smartChunk(text, chunkSize);
        if (chunks.length == 0) return;

        pendingChunkIndex = 0;
        sendNextChunk(ic, chunks, delay);
    }

    private void sendNextChunk(final InputConnection ic, final String[] chunks, final int delay) {
        if (cancelled || pendingChunkIndex < 0 || pendingChunkIndex >= chunks.length) {
            pendingChunkIndex = -1;
            return;
        }

        try {
            ic.commitText(chunks[pendingChunkIndex], 1);
        } catch (Throwable e) {
            cancel();
            return;
        }

        pendingChunkIndex++;
        if (pendingChunkIndex < chunks.length && !cancelled) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendNextChunk(ic, chunks, delay);
                }
            }, PASTE_TOKEN, delay);
        } else {
            pendingChunkIndex = -1;
        }
    }

    /**
     * 智能分块：优先在换行符处切，最大不超过 chunkSize。
     */
    static String[] smartChunk(String text, int maxChunkSize) {
        if (text.length() <= maxChunkSize) {
            return new String[] { text };
        }

        java.util.ArrayList<String> chunks = new java.util.ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());

            if (end < text.length()) {
                int lastNewline = text.lastIndexOf('\n', end);
                if (lastNewline > start) {
                    end = lastNewline + 1;
                }
            }

            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks.toArray(new String[0]);
    }
}
