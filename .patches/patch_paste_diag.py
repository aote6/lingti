import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
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
new = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                diagLog("onCommand type=" + (cmd == null ? "null" : cmd.type));
                if (cmd != null && cmd.type == Command.Type.CLIPBOARD_OPEN_PANEL) {
                    diagLog("hit CLIPBOARD_OPEN_PANEL branch, keyboardView=" + (keyboardView != null));
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
assert text.count(old) == 1, "锚点1未找到或不唯一"
text = text.replace(old, new, 1)

old2 = """    public static java.util.List<String> getClipboardHistory() { return clipboardHistory; }"""
new2 = """    public static java.util.List<String> getClipboardHistory() { return clipboardHistory; }

    // 临时诊断用：写到 getExternalFilesDir()，Termux 可读，排查完粘贴问题后应删除
    private void diagLog(String msg) {
        try {
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) return;
            java.io.File f = new java.io.File(dir, "diag.log");
            String time = new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(new java.util.Date());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f, true);
            fos.write((time + " " + msg + "\\n").getBytes());
            fos.close();
        } catch (Exception ignored) {}
    }"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("诊断日志补丁完成，2处全部成功")
