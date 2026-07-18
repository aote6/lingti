// 提交命令：finishComposingText()，结束输入法组合状态
package com.unbounded.input;

public class Commit extends Command {
    public Commit() {
        super(Type.COMMIT, "");
    }
}
