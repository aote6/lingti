// 删除命令：删除光标前一个字符（含代理对处理）
package com.unbounded.input;

public class Backspace extends Command {
    public Backspace() {
        super(Type.BACKSPACE, "");
    }
}
