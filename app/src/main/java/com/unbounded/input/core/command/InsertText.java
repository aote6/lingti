package com.unbounded.input.core.command;

import com.unbounded.input.Command;

public class InsertText extends Command {
    public InsertText(String text) {
        super(Type.INSERT_TEXT, text != null ? text : "");
    }
}
