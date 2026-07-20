import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

# 1. 缩小右侧控制按钮宽度和间距，让出空间避免跟槽位按钮重叠
old1 = """        int btnH = Math.round(controlBarHeight - 6 * dpScale);
        int btnW = Math.round(56 * dpScale);
        int gap = Math.round(6 * dpScale);"""
new1 = """        int btnH = Math.round(controlBarHeight - 6 * dpScale);
        int btnW = Math.round(48 * dpScale);
        int gap = Math.round(4 * dpScale);"""
assert text.count(old1) == 1, "锚点1未找到或不唯一"
text = text.replace(old1, new1, 1)

# 2. 新增字段：标记"刚打开面板"这次点击的抬起不应立即触发关闭
old2 = """    private boolean componentPanelOpen = false;"""
new2 = """    private boolean componentPanelOpen = false;
    private boolean justOpenedComponentPanel = false;"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

# 3. 打开面板时置位标记
old3 = """            if (editMode && componentButtonRect.contains(x, y)) {
                componentPanelOpen = true;
                invalidate();
                return true;
            }"""
new3 = """            if (editMode && componentButtonRect.contains(x, y)) {
                componentPanelOpen = true;
                justOpenedComponentPanel = true;
                invalidate();
                return true;
            }"""
assert text.count(old3) == 1, "锚点3未找到或不唯一"
text = text.replace(old3, new3, 1)

# 4. 面板打开时的触摸处理：吞掉"刚打开"那次的抬起，之后才是真正的选择/关闭
old4 = """        if (componentPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                for (int i = 0; i < templateItemRects.length; i++) {
                    if (templateItemRects[i].contains(x, y)) {
                        instantiateTemplate(i);
                        break;
                    }
                }
                componentPanelOpen = false;
                invalidate();
            }
            return true;
        }"""
new4 = """        if (componentPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (justOpenedComponentPanel) {
                    justOpenedComponentPanel = false;
                } else {
                    for (int i = 0; i < templateItemRects.length; i++) {
                        if (templateItemRects[i].contains(x, y)) {
                            instantiateTemplate(i);
                            break;
                        }
                    }
                    componentPanelOpen = false;
                }
                invalidate();
            }
            return true;
        }"""
assert text.count(old4) == 1, "锚点4未找到或不唯一"
text = text.replace(old4, new4, 1)

path.write_text(text, encoding="utf-8")
print("修复补丁完成，4处全部成功")
