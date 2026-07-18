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

        // 第一行: q w e r t y u i o p
        RowSpec row1 = new RowSpec();
        for (char c : "qwertyuiop".toCharArray()) {
            row1.add(new KeyModel("k" + c, String.valueOf(c), 1f, 1, 1, 1, 1));
        }
        profile.addRow(row1);

        // 第二行: a s d f g h j k l
        RowSpec row2 = new RowSpec();
        row2.add(new KeyModel("pad1", "", 0.5f)); // 错位
        for (char c : "asdfghjkl".toCharArray()) {
            row2.add(new KeyModel("k" + c, String.valueOf(c), 1f, 1, 1, 1, 1));
        }
        row2.add(new KeyModel("pad2", "", 0.5f));
        profile.addRow(row2);

        // 第三行: Shift z x c v b n m Backspace
        RowSpec row3 = new RowSpec();
        KeyModel shift = new KeyModel("shift", "Sh", 1.5f, 1, 1, 1, 1);
        row3.add(shift);
        for (char c : "zxcvbnm".toCharArray()) {
            row3.add(new KeyModel("k" + c, String.valueOf(c), 1f, 1, 1, 1, 1));
        }
        KeyModel bksp = new KeyModel("backspace", "Del", 1.5f, 1, 1, 1, 1);
        bksp.tap = Command.backspace();
        row3.add(bksp);
        profile.addRow(row3);

        // 第四行: 123 Space Enter
        RowSpec row4 = new RowSpec();
        KeyModel num = new KeyModel("num", "123", 1.5f, 1, 1, 1, 1);
        KeyModel space = new KeyModel("space", "Space", 6f, 1, 1, 1, 1);
        space.tap = Command.insert(" ");
        KeyModel enter = new KeyModel("enter", "Enter", 2.5f, 1, 1, 1, 1);
        enter.tap = Command.insert("\n");
        row4.add(num).add(space).add(enter);
        profile.addRow(row4);

        return profile;
    }
}
