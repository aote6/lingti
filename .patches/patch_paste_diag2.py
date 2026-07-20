import pathlib

# 撤销上一版写文件的诊断代码，改用 showFlash 屏幕上直接可见
p1 = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/SimpleImeService.java")
t1 = p1.read_text(encoding="utf-8")

old1 = """            public void onCommand(Command cmd) {
                diagLog("onCommand type=" + (cmd == null ? "null" : cmd.type));
                if (cmd != null && cmd.type == Command.Type.CLIPBOARD_OPEN_PANEL) {
                    diagLog("hit CLIPBOARD_OPEN_PANEL branch, keyboardView=" + (keyboardView != null));
                    if (keyboardView != null) keyboardView.openClipboardPanel();
                    return;
                }"""
new1 = """            public void onCommand(Command cmd) {
                if (keyboardView != null) {
                    keyboardView.showDiagFlash("cmd=" + (cmd == null ? "null" : cmd.type));
                }
                if (cmd != null && cmd.type == Command.Type.CLIPBOARD_OPEN_PANEL) {
                    if (keyboardView != null) keyboardView.openClipboardPanel();
                    return;
                }"""
assert t1.count(old1) == 1, "SimpleImeService 锚点1未找到或不唯一"
t1 = t1.replace(old1, new1, 1)

old1b = """    // 临时诊断用：写到 getExternalFilesDir()，Termux 可读，排查完粘贴问题后应删除
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
new1b = ""
assert t1.count(old1b) == 1, "SimpleImeService 锚点2未找到或不唯一"
t1 = t1.replace(old1b, new1b, 1)
p1.write_text(t1, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")

# KeyboardView 加一个公开方法，直接复用现有 showFlash 机制（原本是private）
p2 = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/KeyboardView.java")
t2 = p2.read_text(encoding="utf-8")

old2 = """    private void showFlash(String message) {"""
new2 = """    public void showDiagFlash(String message) {
        showFlash(message);
    }

    private void showFlash(String message) {"""
assert t2.count(old2) == 1, "KeyboardView 锚点未找到或不唯一"
t2 = t2.replace(old2, new2, 1)
p2.write_text(t2, encoding="utf-8")
print("KeyboardView.java 打补丁完成")
