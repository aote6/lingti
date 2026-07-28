// 命令分发接口：PanelService实现，桥接GestureController和InputEngine
package com.unbounded.input;

public interface KeyboardActionDispatcher {
    void onCommand(Command cmd);
}
