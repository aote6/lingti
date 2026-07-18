// 系统按键命令：发送真实KeyEvent（Esc/Tab/Enter/方向键）
package com.unbounded.input.core.command;

import com.unbounded.input.Command;

public class KeyEventCommand extends Command {
    public final int keyCode;
    public final int metaState;

    public KeyEventCommand(int keyCode, int metaState) {
        super(Type.KEY_EVENT, "");
        this.keyCode = keyCode;
        this.metaState = metaState;
    }

    public static KeyEventCommand of(int keyCode) {
        return new KeyEventCommand(keyCode, 0);
    }

    public static KeyEventCommand withMeta(int keyCode, int metaState) {
        return new KeyEventCommand(keyCode, metaState);
    }
}
