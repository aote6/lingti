package com.unbounded.input;

public class InsertText implements Command {
    private final String text;

    public InsertText(String text) {
        this.text = text;
    }

    @Override
    public String type() { return "insert"; }

    @Override
    public String text() { return text; }
}
