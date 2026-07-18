// 组合键命令：同时按下多个键（Ctrl+C等）
package com.unbounded.input.core.command;

import com.unbounded.input.Command;

public class KeyChordCommand extends Command {
    public final int[] keyCodes;
    public final int metaState;

    public KeyChordCommand(int[] keyCodes, int metaState) {
        super(Type.KEY_CHORD, "");
        this.keyCodes = keyCodes != null ? keyCodes : new int[0];
        this.metaState = metaState;
    }

    public static KeyChordCommand of(int... keyCodes) {
        return new KeyChordCommand(keyCodes, 0);
    }
}
