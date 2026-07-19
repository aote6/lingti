import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardGestureController.java")
text = path.read_text(encoding="utf-8")

old1 = """public class KeyboardGestureController {
    private final List<KeyModel> keys;"""
new1 = """public class KeyboardGestureController {
    private List<KeyModel> keys;"""
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

old2 = """    public KeyModel getActiveKey() { return activeKey; }
    public boolean isLongPressed() { return isLongPressed; }"""
new2 = """    public KeyModel getActiveKey() { return activeKey; }
    public boolean isLongPressed() { return isLongPressed; }

    // 删除/新增按键后调用：刷新命中判定用的按键列表，避免持有已从布局里
    // 摘除的键的引用（"幽灵键"——画面上已经不见了，但原来的区域还能点中它）。
    public void updateKeys(List<KeyModel> newKeys) {
        this.keys = newKeys;
        if (activeKey != null && !newKeys.contains(activeKey)) {
            activeKey = null;
            isGestureConsumed = false;
            isLongPressed = false;
        }
    }"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardGestureController.java 打补丁完成，2处全部成功")
