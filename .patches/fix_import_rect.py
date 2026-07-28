import pathlib
path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardRenderer.java")
text = path.read_text(encoding="utf-8")

# 检查是否已有 Rect 导入
if "import android.graphics.Rect;" not in text:
    # 在 package 声明后、第一个 import 前插入
    lines = text.split('\n')
    new_lines = []
    inserted = False
    for line in lines:
        new_lines.append(line)
        if not inserted and line.startswith("package "):
            new_lines.append("")
            new_lines.append("import android.graphics.Rect;")
            inserted = True
    if not inserted:
        # 如果没找到 package，就在最开头插入
        new_lines = ["import android.graphics.Rect;", ""] + lines
    
    text = '\n'.join(new_lines)
    path.write_text(text, encoding="utf-8")
    print("已添加 import android.graphics.Rect;")
else:
    print("Rect 导入已存在")
