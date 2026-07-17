package com.unbounded.input;

import android.view.inputmethod.InputConnection;

public class InputEngine {
    public static void execute(InputConnection ic, Command cmd) {
        if (ic == null || cmd == null) return;
        switch (cmd.type()) {
            case "insert":
                ic.beginBatchEdit();
                String text = cmd.text();
                if (text.equals("空格")) {
                    ic.commitText(" ", 1);
                } else if (text.equals("Enter")) {
                    ic.commitText("\n", 1);
                } else if (text.equals("Del")) {
                    ic.deleteSurroundingText(1, 0);
                } else if (text.equals("Tab")) {
                    ic.commitText("\t", 1);
                } else if (text.equals("Esc") || text.equals("布局")) {
                    // 暂不处理
                } else if (!text.isEmpty()) {
                    ic.commitText(text, 1);
                }
                ic.endBatchEdit();
                break;
            case "backspace":
                ic.deleteSurroundingText(1, 0);
                break;
            case "commit":
                ic.finishComposingText();
                break;
        }
    }
}
