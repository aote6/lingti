package com.unbounded.input;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.command.KeyChordCommand;

public class InputEngine {
    public static void execute(InputConnection ic, Command cmd) {
        if (ic == null || cmd == null) return;
        switch (cmd.type) {
            case INSERT_TEXT:
                if (!cmd.text.isEmpty()) {
                    ic.beginBatchEdit();
                    ic.commitText(cmd.text, 1);
                    ic.endBatchEdit();
                }
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
            case SHIFT_TOGGLE:
            case SYMBOL_TOGGLE:
                break;
            case KEY_EVENT:
                if (cmd instanceof KeyEventCommand) {
                    KeyEventCommand kec = (KeyEventCommand) cmd;
                    long now = android.os.SystemClock.uptimeMillis();
                    ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, kec.keyCode, 0, kec.metaState));
                    ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, kec.keyCode, 0, kec.metaState));
                }
                break;
            case KEY_CHORD:
                if (cmd instanceof KeyChordCommand) {
                    KeyChordCommand kcc = (KeyChordCommand) cmd;
                    long now = android.os.SystemClock.uptimeMillis();
                    for (int code : kcc.keyCodes) {
                        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0, kcc.metaState));
                    }
                    for (int i = kcc.keyCodes.length - 1; i >= 0; i--) {
                        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, kcc.keyCodes[i], 0, kcc.metaState));
                    }
                }
                break;
        }
    }
}
