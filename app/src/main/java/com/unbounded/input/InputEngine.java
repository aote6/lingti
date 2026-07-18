package com.unbounded.input;

import android.view.inputmethod.InputConnection;

public class InputEngine {
    public static void execute(InputConnection ic, Command cmd) {
        if (ic == null || cmd == null) return;
        switch (cmd.type) {
            case INSERT_TEXT:
                if (cmd.text.isEmpty()) break;
                ic.beginBatchEdit();
                ic.commitText(cmd.text, 1);
                ic.endBatchEdit();
                break;
            case BACKSPACE:
                ic.deleteSurroundingText(1, 0);
                break;
            case COMMIT:
                ic.finishComposingText();
                break;
            case SPACE:
                ic.commitText(" ", 1);
                break;
            case ENTER:
                ic.commitText("\n", 1);
                break;
            case DEL:
                ic.deleteSurroundingText(1, 0);
                break;
            case TAB:
                ic.commitText("\t", 1);
                break;
            case ESC:
            case NOOP:
                break;
        }
    }
}
