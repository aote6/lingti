package com.unbounded.input;

public class InsertText extends Command {
    public InsertText(String text) {
        super(Type.INSERT_TEXT, text != null ? text : "");
    }
}
