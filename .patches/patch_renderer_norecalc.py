import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardRenderer.java")
text = path.read_text(encoding="utf-8")

old = """        // 按 LayoutProfile 逐行渲染
        if (profile == null || profile.rows.isEmpty()) return;

        List<RowSpec> rows = profile.rows;
        float maxSpan = 0f;
        for (RowSpec row : rows) {
            float s = row.totalSpan();
            if (s > maxSpan) maxSpan = s;
        }
        if (maxSpan == 0f) maxSpan = 10f;

        float remainingHeight = h - barHeight;
        float rowH = remainingHeight / rows.size();
        float unit = w / maxSpan;
        float y = barHeight;

        for (RowSpec row : rows) {
            float rowWidth = row.totalSpan() * unit;
            float x = (w - rowWidth) / 2f;
            for (KeyModel key : row.keys) {
                float kw = unit * key.span;
                float left = x + key.padLeft;
                float top = y + key.padTop;
                float right = x + kw - key.padRight;
                float bottom = y + rowH - key.padBottom;

                key.rect.set((int) left, (int) top, (int) right, (int) bottom);

                // 按键背景"""
new = """        // 按 LayoutProfile 逐行渲染
        // 坐标不在此处计算：key.rect 由 LayoutManager.computeRects() 唯一权威计算
        // （包括百分比坐标分支），渲染器只负责绘制，不重新计算位置。
        if (profile == null || profile.rows.isEmpty()) return;

        List<RowSpec> rows = profile.rows;

        for (RowSpec row : rows) {
            for (KeyModel key : row.keys) {
                // 按键背景"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

# 去掉循环末尾的 x += kw; 和 y += rowH; （不再需要，因为不再手动推进坐标）
old2 = """                x += kw;
            }
            y += rowH;
        }
    }"""
new2 = """            }
        }
    }"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardRenderer.java 打补丁完成")
