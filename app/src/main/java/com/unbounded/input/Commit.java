package com.unbounded.input;

public class Commit implements Command {
    @Override
    public String type() { return "commit"; }

    @Override
    public String text() { return ""; }
}
