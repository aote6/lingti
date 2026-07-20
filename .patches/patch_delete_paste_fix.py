import pathlib

# ---------- 1. InputEngine.java：删除时优先处理选区 ----------
p1 = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/InputEngine.java")
t1 = p1.read_text(encoding="utf-8")

old1 = """    private static void deleteCharBeforeCursor(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(2, 0);"""
new1 = """    private static void deleteCharBeforeCursor(InputConnection ic) {
        CharSequence selected = ic.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            // 有选区时，删除应该删掉选中的内容，而不是删光标前一个字符
            ic.commitText("", 1);
            return;
        }
        CharSequence before = ic.getTextBeforeCursor(2, 0);"""
assert t1.count(old1) == 1, "InputEngine 锚点未找到或不唯一"
t1 = t1.replace(old1, new1, 1)
p1.write_text(t1, encoding="utf-8")
print("InputEngine.java 打补丁完成")

# ---------- 2. SimpleImeService.java：接通剪贴板命令 ----------
p2 = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/SimpleImeService.java")
t2 = p2.read_text(encoding="utf-8")

old2 = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };"""
new2 = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                if (cmd != null && cmd.type == Command.Type.CLIPBOARD_OPEN_PANEL) {
                    if (keyboardView != null) keyboardView.openClipboardPanel();
                    return;
                }
                if (cmd != null && cmd.type == Command.Type.CLIPBOARD_PASTE_RECENT) {
                    pasteRecentClipboard();
                    return;
                }
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };"""
assert t2.count(old2) == 1, "SimpleImeService 锚点未找到或不唯一"
t2 = t2.replace(old2, new2, 1)
p2.write_text(t2, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")
