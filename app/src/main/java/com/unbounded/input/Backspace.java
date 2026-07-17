package com.unbounded.input;

public class Backspace implements Command {
    @Override
    public String type() { return "backspace"; }

    @Override
    public String text() { return ""; }
}
