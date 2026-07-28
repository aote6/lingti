import pathlib
path = pathlib.Path("STATUS.md")
text = path.read_text(encoding="utf-8")

# 要插入的新章节
new_section = """## 2026-07-27 剪贴板面板 UI 重构

**背景**：剪贴板面板原来的设计是：倒序显示最近若干条，点击任意条目粘贴并自动关闭面板。用户需要更精细的控制：固定常用条目、删除无用条目、连续粘贴多条不关闭面板。

**改动内容**：
- 底层数据结构升级：`clipboardHistory` 从 `List<String>` 改为 `List<ClipboardEntry>`，每个条目包含 `text` 和 `pinned` 字段
- 存储顺序不变（老→新），显示逻辑改变：
  - 固定记录（pinned=true）排在最上面，按原顺序排列
  - 普通记录只显示最近若干条，按复制顺序（老→新）排列
- 面板顶部常驻「退出」按钮，点击关闭面板
- 每行右侧两个按钮：「固定/取消」、「删除」
- 点击文字区域 = 粘贴且不关闭面板，支持连续点击多条

**修改文件**：
- `SimpleImeService.java`：新增 `ClipboardEntry` 内部类，`toggleClipboardPin()` / `deleteClipboardItem()` 静态方法
- `KeyboardRenderer.java`：新增 `ClipboardHit` 内部类，重写 `drawClipboardPopup()` 和 `hitTestClipboard()`
- `KeyboardView.java`：更新触摸事件处理逻辑，支持多种点击动作
- `KeyboardGestureController.java`：适配新的 `getClipboardHistory()` 返回类型

**待优化**：按钮宽度使用固定像素值，未按 dp 缩放。等实机测试后根据用户反馈调整。

"""

# 找到插入位置：在 "## 2026-07-27 剪贴板类型命令序列化bug最终修复" 之前
old_marker = "## 2026-07-27 剪贴板类型命令序列化bug最终修复"
new_text = text.replace(old_marker, new_section + old_marker)

# 再更新"已知遗留"部分：移除 showDiagFlash 那条
old_known = "- **诊断用 `showDiagFlash` 仍留在代码里**：每次按键弹提示，等剪 贴板序列化 bug 修完后一并清理。"
new_known = "- **诊断用 `showDiagFlash` 仍留在代码里**：用户认为闪一下就消失不影响使用，保留不删，以后有需要再决定是否移除。"

new_text = new_text.replace(old_known, new_known)

path.write_text(new_text, encoding="utf-8")
print("STATUS.md 更新完成")
