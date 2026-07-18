package com.unbounded.input;

import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.command.KeyChordCommand;

public class InputEngine {
    private static final String TAG = "InputEngine";

    public static void execute(InputConnection ic, Command cmd) {
        if (ic == null || cmd == null) return;
        switch (cmd.type) {
            case INSERT_TEXT:
                if (cmd.text != null && !cmd.text.isEmpty()) {
                    ic.beginBatchEdit();
                    ic.commitText(cmd.text, 1);
                    ic.endBatchEdit();
                }
                break;
            case BACKSPACE:
                deleteCharBeforeCursor(ic);
                break;
            case COMMIT:
                ic.finishComposingText();
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
            default:
                Log.w(TAG, "Unhandled command type: " + cmd.type);
                break;
        }
    }

    private static void deleteCharBeforeCursor(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(2, 0);
        if (before != null && before.length() > 0) {
            char last = before.charAt(before.length() - 1);
            if (Character.isHighSurrogate(last) && before.length() >= 2) {
                char secondLast = before.charAt(before.length() - 2);
                if (Character.isLowSurrogate(secondLast)) {
                    ic.deleteSurroundingText(2, 0);
                    return;
                }
            }
            if (Character.isLowSurrogate(last) && before.length() >= 2) {
                char secondLast = before.charAt(before.length() - 2);
                if (Character.isHighSurrogate(secondLast)) {
                    ic.deleteSurroundingText(2, 0);
                    return;
                }
            }
        }
        ic.deleteSurroundingText(1, 0);
    }
}
