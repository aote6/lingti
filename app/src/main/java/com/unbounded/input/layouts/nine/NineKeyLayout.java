package com.unbounded.input.layouts.nine;

import com.unbounded.input.Command;
import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.layout.KeyboardLayout;
import com.unbounded.input.core.layout.LayoutProfile;
import com.unbounded.input.core.layout.RowSpec;

public class NineKeyLayout implements KeyboardLayout {
    private final Command[][] commands;

    public NineKeyLayout(Command[][] commands) {
        this.commands = commands;
    }

    @Override
    public String id() { return "ninekey"; }

    @Override
    public LayoutProfile build() {
        LayoutProfile profile = new LayoutProfile("ninekey");
        if (commands == null) return profile;

        String[][] labels = {
            {"1","2","3"},
            {"4","5","6"},
            {"7","8","9"},
            {"*"," ","Del"}
        };
        String[][] t9Labels = {
            {"1","ABC","DEF"},
            {"GHI","JKL","MNO"},
            {"PQRS","TUV","WXYZ"},
            {"*","空格","Del"}
        };

        for (int r = 0; r < commands.length && r < labels.length; r++) {
            RowSpec row = new RowSpec();
            for (int c = 0; c < commands[r].length && c < 3; c++) {
                String label = (r < t9Labels.length && c < t9Labels[r].length) ? t9Labels[r][c] : labels[r][c];
                KeyModel key = new KeyModel("k" + r + c, label, 1f, 2, 2, 2, 2);
                if (commands[r][c] != null) {
                    key.tap = commands[r][c];
                }
                row.add(key);
            }
            profile.addRow(row);
        }
        return profile;
    }
}
