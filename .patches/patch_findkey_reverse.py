import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardGestureController.java")
text = path.read_text(encoding="utf-8")

old = """    private KeyModel findKey(float x, float y) {
        for (KeyModel k : keys) if (k.rect.contains((int) x, (int) y)) return k;
        return null;
    }"""
new = """    private KeyModel findKey(float x, float y) {
        // 倒序遍历：列表末尾的键最后绘制、视觉上在最上层，
        // 触摸命中优先级必须和视觉层级一致，重叠区域优先判给最上层的键。
        for (int i = keys.size() - 1; i >= 0; i--) {
            KeyModel k = keys.get(i);
            if (k.rect.contains((int) x, (int) y)) return k;
        }
        return null;
    }"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardGestureController.java 打补丁完成")
