// 插入文本命令：commitText到InputConnection
package com.unbounded.input;

public class InsertText extends Command {
    public InsertText(String text) {
        super(Type.INSERT_TEXT, text != null ? text : "");
    }
}
