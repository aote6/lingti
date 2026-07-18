package com.unbounded.input.layouts.qwerty;

import com.unbounded.input.Command;
import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class Qwerty26Layout implements KeyboardLayout {

    @Override
    public String id() { return "qwerty26"; }

    @Override
    public LayoutProfile build() {
        LayoutProfile profile = new LayoutProfile("qwerty26");

        RowSpec row1 = new RowSpec();
        for (char c : "qwertyuiop".toCharArray()) row1.add(letterKey(c));
        profile.addRow(row1);

        RowSpec row2 = new RowSpec();
        row2.add(spacer(0.5f));
        for (char c : "asdfghjkl".toCharArray()) row2.add(letterKey(c));
        row2.add(spacer(0.5f));
        profile.addRow(row2);

        RowSpec row3 = new RowSpec();
        KeyModel shift = new KeyModel("shift", "Sh", 1.5f, 1, 1, 1, 1);
        shift.tap = Command.insert("");
        row3.add(shift);
        for (char c : "zxcvbnm".toCharArray()) row3.add(letterKey(c));
        KeyModel bksp = new KeyModel("backspace", "Del", 1.5f, 1, 1, 1, 1);
        bksp.tap = Command.backspace();
        row3.add(bksp);
        profile.addRow(row3);

        RowSpec row4 = new RowSpec();
        KeyModel num = new KeyModel("num", "123", 1.5f, 1, 1, 1, 1);
        num.tap = Command.insert("");
        KeyModel space = new KeyModel("space", "Space", 6f, 1, 1, 1, 1);
        space.tap = Command.insert(" ");
        KeyModel enter = new KeyModel("enter", "Enter", 2.5f, 1, 1, 1, 1);
        enter.tap = Command.insert("\n");
        row4.add(num).add(space).add(enter);
        profile.addRow(row4);

        return profile;
    }

    private KeyModel letterKey(char c) {
        KeyModel k = new KeyModel("k" + c, String.valueOf(c), 1f, 1, 1, 1, 1);
        k.tap = Command.insert(String.valueOf(c));
        return k;
    }

    private KeyModel spacer(float span) {
        return new KeyModel("sp", "", span);
    }
}
