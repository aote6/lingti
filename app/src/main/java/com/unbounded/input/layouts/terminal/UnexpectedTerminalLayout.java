package com.unbounded.input.layouts.terminal;

import android.view.KeyEvent;

import com.unbounded.input.Command;
import com.unbounded.input.core.command.KeyChordCommand;
import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class UnexpectedTerminalLayout implements KeyboardLayout {

    @Override
    public String id() { return "unexpected_terminal"; }

    @Override
    public LayoutProfile build() {
        LayoutProfile profile = new LayoutProfile("unexpected_terminal");

        // 第一行: Esc Tab /
        RowSpec row1 = new RowSpec();
        row1.add(keyEvent("esc", "Esc", KeyEvent.KEYCODE_ESCAPE, 2f));
        row1.add(keyEvent("tab", "Tab", KeyEvent.KEYCODE_TAB, 2f));
        row1.add(keyText("slash", "/", "/", 2f));
        profile.addRow(row1);

        // 第二行: - : !
        RowSpec row2 = new RowSpec();
        row2.add(keyText("minus", "-", "-", 2f));
        row2.add(keyText("colon", ":", ":", 2f));
        row2.add(keyText("bang", "!", "!", 2f));
        profile.addRow(row2);

        // 第三行: ( Up Home
        RowSpec row3 = new RowSpec();
        row3.add(keyText("lparen", "(", "(", 2f));
        row3.add(keyEvent("up", "\u2191", KeyEvent.KEYCODE_DPAD_UP, 2f));
        row3.add(keyEvent("home", "Home", KeyEvent.KEYCODE_MOVE_HOME, 2f));
        profile.addRow(row3);

        // 第四行: Ctrl 空格 Backspace
        RowSpec row4 = new RowSpec();
        KeyModel ctrl = new KeyModel("ctrl", "Ctrl", 2f, 1, 1, 1, 1);
        ctrl.tap = Command.noop();
        KeyModel space = new KeyModel("space", "Space", 4f, 1, 1, 1, 1);
        space.tap = Command.insert(" ");
        KeyModel bksp = new KeyModel("backspace", "Del", 2f, 1, 1, 1, 1);
        bksp.tap = Command.backspace();
        row4.add(ctrl).add(space).add(bksp);
        profile.addRow(row4);

        // 第五行: Ctrl+C Ctrl+D Ctrl+Z ← ↓ → PgUp PgDn
        RowSpec row5 = new RowSpec();
        row5.add(chord("ctrl_c", "C-c", new int[]{KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_C}, 1.5f));
        row5.add(chord("ctrl_d", "C-d", new int[]{KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_D}, 1.5f));
        row5.add(chord("ctrl_z", "C-z", new int[]{KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_Z}, 1.5f));
        row5.add(keyEvent("left", "\u2190", KeyEvent.KEYCODE_DPAD_LEFT, 1.5f));
        row5.add(keyEvent("down", "\u2193", KeyEvent.KEYCODE_DPAD_DOWN, 1.5f));
        row5.add(keyEvent("right", "\u2192", KeyEvent.KEYCODE_DPAD_RIGHT, 1.5f));
        row5.add(keyEvent("pgup", "PgU", KeyEvent.KEYCODE_PAGE_UP, 1.5f));
        row5.add(keyEvent("pgdn", "PgD", KeyEvent.KEYCODE_PAGE_DOWN, 1.5f));
        profile.addRow(row5);

        return profile;
    }

    private KeyModel keyEvent(String id, String label, int keyCode, float span) {
        KeyModel k = new KeyModel(id, label, span, 1, 1, 1, 1);
        k.tap = KeyEventCommand.of(keyCode);
        return k;
    }

    private KeyModel keyText(String id, String label, String text, float span) {
        KeyModel k = new KeyModel(id, label, span, 1, 1, 1, 1);
        k.tap = Command.insert(text);
        return k;
    }

    private KeyModel chord(String id, String label, int[] keyCodes, float span) {
        KeyModel k = new KeyModel(id, label, span, 1, 1, 1, 1);
        k.tap = KeyChordCommand.of(keyCodes);
        return k;
    }
}
