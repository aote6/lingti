// 命令基类：定义Type枚举和工厂方法，所有输入动作的抽象
package com.unbounded.input;

public abstract class Command {
    public enum Type {
        INSERT_TEXT, BACKSPACE, COMMIT,
        KEY_EVENT, KEY_CHORD,
        CLIPBOARD_PASTE_RECENT, CLIPBOARD_OPEN_PANEL
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
    public static Command clipboardPasteRecent() { return new ClipboardCommand(Type.CLIPBOARD_PASTE_RECENT); }
    public static Command clipboardOpenPanel() { return new ClipboardCommand(Type.CLIPBOARD_OPEN_PANEL); }
}
