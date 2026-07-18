package com.unbounded.input.layouts.qwerty;

import com.unbounded.input.Command;
import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class Qwerty26Layout implements KeyboardLayout {
    private boolean shiftActive = false;
    private boolean symbolActive = false;

    public void toggleShift() { shiftActive = !shiftActive; }
    public void toggleSymbol() { symbolActive = !symbolActive; }

    @Override
    public String id() { return "qwerty26"; }

    @Override
    public LayoutProfile build() {
        LayoutProfile profile = new LayoutProfile("qwerty26");

        if (symbolActive) {
            buildSymbolLayer(profile);
        } else {
            buildLetterLayer(profile);
        }
        return profile;
    }

    private void buildLetterLayer(LayoutProfile profile) {
        String topRow = shiftActive ? "QWERTYUIOP" : "qwertyuiop";
        String midRow = shiftActive ? "ASDFGHJKL" : "asdfghjkl";
        String botRow = shiftActive ? "ZXCVBNM" : "zxcvbnm";

        RowSpec row1 = new RowSpec();
        for (char c : topRow.toCharArray()) row1.add(letterKey(c));
        profile.addRow(row1);

        RowSpec row2 = new RowSpec();
        row2.add(spacer(0.5f));
        for (char c : midRow.toCharArray()) row2.add(letterKey(c));
        row2.add(spacer(0.5f));
        profile.addRow(row2);

        RowSpec row3 = new RowSpec();
        KeyModel shift = new KeyModel("shift", shiftActive ? "⇧" : "Sh", 1.5f, 1, 1, 1, 1);
        shift.tap = Command.shiftToggle();
        row3.add(shift);
        for (char c : botRow.toCharArray()) row3.add(letterKey(c));
        KeyModel bksp = new KeyModel("backspace", "Del", 1.5f, 1, 1, 1, 1);
        bksp.tap = Command.backspace();
        row3.add(bksp);
        profile.addRow(row3);

        RowSpec row4 = new RowSpec();
        KeyModel num = new KeyModel("num", "123", 1.5f, 1, 1, 1, 1);
        num.tap = Command.symbolToggle();
        KeyModel space = new KeyModel("space", "Space", 6f, 1, 1, 1, 1);
        space.tap = Command.insert(" ");
        KeyModel enter = new KeyModel("enter", "Enter", 2.5f, 1, 1, 1, 1);
        enter.tap = Command.insert("\n");
        row4.add(num).add(space).add(enter);
        profile.addRow(row4);
    }

    private void buildSymbolLayer(LayoutProfile profile) {
        RowSpec row1 = new RowSpec();
        for (char c : "1234567890".toCharArray()) row1.add(symbolKey(c));
        profile.addRow(row1);

        RowSpec row2 = new RowSpec();
        row2.add(spacer(0.5f));
        for (char c : "@#$%&*-+=".toCharArray()) row2.add(symbolKey(c));
        row2.add(spacer(0.5f));
        profile.addRow(row2);

        RowSpec row3 = new RowSpec();
        KeyModel shift = new KeyModel("shift", "ABC", 1.5f, 1, 1, 1, 1);
        shift.tap = Command.symbolToggle();
        row3.add(shift);
        for (char c : "!?.,:;/()".toCharArray()) row3.add(symbolKey(c));
        KeyModel bksp = new KeyModel("backspace", "Del", 1.5f, 1, 1, 1, 1);
        bksp.tap = Command.backspace();
        row3.add(bksp);
        profile.addRow(row3);

        RowSpec row4 = new RowSpec();
        KeyModel num = new KeyModel("num", "ABC", 1.5f, 1, 1, 1, 1);
        num.tap = Command.symbolToggle();
        KeyModel space = new KeyModel("space", "Space", 6f, 1, 1, 1, 1);
        space.tap = Command.insert(" ");
        KeyModel enter = new KeyModel("enter", "Enter", 2.5f, 1, 1, 1, 1);
        enter.tap = Command.insert("\n");
        row4.add(num).add(space).add(enter);
        profile.addRow(row4);
    }

    private KeyModel letterKey(char c) {
        KeyModel k = new KeyModel("k" + c, String.valueOf(c), 1f, 1, 1, 1, 1);
        k.tap = Command.insert(String.valueOf(c));
        return k;
    }

    private KeyModel symbolKey(char c) {
        KeyModel k = new KeyModel("s" + c, String.valueOf(c), 1f, 1, 1, 1, 1);
        k.tap = Command.insert(String.valueOf(c));
        return k;
    }

    private KeyModel spacer(float span) {
        KeyModel k = new KeyModel("sp", "", span);
        k.enabled = false;
        return k;
    }
}
