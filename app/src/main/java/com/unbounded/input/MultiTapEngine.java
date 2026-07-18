package com.unbounded.input;

public class MultiTapEngine {
    private static final String[] T9_LETTERS = {
        "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
    };

    private int lastDigit = -1;
    private int tapCount = 0;
    private long lastTapTime = 0;
    private static final long TIMEOUT = 800;
    private String committed = "";

    public void reset() {
        lastDigit = -1;
        tapCount = 0;
        lastTapTime = 0;
        committed = "";
    }

    /**
     * 处理一次数字键点击，返回当前组合状态文本（用于候选栏显示），
     * committed 部分已固定不变，最后一段是当前正在循环的字母序列。
     */
    public String processDigit(int digit) {
        if (digit < 2 || digit > 9) {
            flush();
            return committed;
        }
        long now = System.currentTimeMillis();
        if (digit == lastDigit && (now - lastTapTime) < TIMEOUT) {
            tapCount++;
        } else {
            flush();
            lastDigit = digit;
            tapCount = 1;
        }
        lastTapTime = now;
        String letters = T9_LETTERS[digit];
        int index = (tapCount - 1) % letters.length();
        return committed + letters.charAt(index);
    }

    /**
     * 确认当前字母，固定到 committed，准备下一个字母
     */
    public void commitCurrent() {
        if (lastDigit >= 2 && lastDigit <= 9) {
            String letters = T9_LETTERS[lastDigit];
            int index = (tapCount - 1) % letters.length();
            committed += letters.charAt(index);
            lastDigit = -1;
            tapCount = 0;
        }
    }

    /**
     * 删除最后一个已确认的字符，或取消当前未确认的
     */
    public String deleteLast() {
        if (lastDigit > 0) {
            lastDigit = -1;
            tapCount = 0;
            return committed;
        }
        if (!committed.isEmpty()) {
            committed = committed.substring(0, committed.length() - 1);
        }
        return committed;
    }

    public String getCommitted() { return committed; }
    public boolean hasUncommitted() { return lastDigit > 0; }

    private void flush() {
        if (lastDigit >= 2 && lastDigit <= 9) {
            String letters = T9_LETTERS[lastDigit];
            int index = (tapCount - 1) % letters.length();
            committed += letters.charAt(index);
        }
        lastDigit = -1;
        tapCount = 0;
    }
}
