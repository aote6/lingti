import pathlib
import re

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardGestureController.java")
text = path.read_text(encoding="utf-8")

# 找到第167行附近的内容并替换
old_line = '            java.util.List<String> history = com.unbounded.input.SimpleImeService.getClipboardHistory();'

# 替换成获取 ClipboardEntry 列表，然后转换成 String 列表
new_lines = '''            java.util.List<com.unbounded.input.SimpleImeService.ClipboardEntry> entries = com.unbounded.input.SimpleImeService.getClipboardHistory();
            java.util.List<String> history = new java.util.ArrayList<>();
            for (com.unbounded.input.SimpleImeService.ClipboardEntry entry : entries) {
                history.add(entry.text);
            }'''

text = text.replace(old_line, new_lines)
path.write_text(text, encoding="utf-8")
print("KeyboardGestureController.java 修复完成")
