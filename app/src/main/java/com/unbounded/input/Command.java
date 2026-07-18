package com.unbounded.input;

public abstract class Command {
    public enum Type {
        INSERT_TEXT, BACKSPACE, COMMIT,
        KEY_EVENT, KEY_CHORD,
        SPACE, ENTER, DEL, TAB, ESC, NOOP,
        SHIFT_TOGGLE, SYMBOL_TOGGLE
    }

    public final Type type;
    public final String text;

    protected Command(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public static Command insert(String text) { return new InsertText(text); }
    public static Command backspace() { return new Backspace(); }
    public static Command commit() { return new Commit(); }
    public static Command space() { return new InsertText(" "); }
    public static Command enter() { return new InsertText("\n"); }
    public static Command del() { return new Backspace(); }
    public static Command tab() { return new InsertText("\t"); }
    public static Command noop() { return new InsertText(""); }
    public static Command shiftToggle() { return new ShiftToggle(); }
    public static Command symbolToggle() { return new SymbolToggle(); }
}
