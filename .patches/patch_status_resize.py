import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/STATUS.md")
text = path.read_text(encoding="utf-8")

# 1. 删除"拖拽缩放按键大小"这条遗留待办
old1 = """- [ ] **拖拽缩放按键大小**：目前拖拽只处理位置（`percentX/percentY`），改大小需要新的触摸判定（比如键角落的缩放手柄），比移动复杂一档。用户已确认优先级在多套布局、删除之后。
"""
new1 = ""
assert text.count(old1) == 1, "锚点1未找到或不唯一"
text = text.replace(old1, new1, 1)

# 2. 在"用户反馈的开放问题"这段之前插入新完成的缩放功能说明
old2 = """**用户反馈的开放问题（不算 bug，记录待用户后续决定是否处理）**："""
new2 = """## 2026-07-19 晚间新增：拖拽缩放按键大小

上一轮完成拖拽移动之后，用户确认的下一优先级功能。设计沿用移动模式已有的"对角固定支点"心智模型：

- **热区**：每个键右下角固定 32dp 见方的触摸区域（不用百分比换算，避免小键的热区跟着一起缩小到摸不准）。编辑模式下热区位置画一个小三角图标提示，非可选项——没有视觉提示用户不知道要去摸那个角。
- **判定时机**：手指按下（`ACTION_DOWN`）的瞬间一次性判定落点是否在热区内，决定进入"缩放模式"还是"移动模式"，过程中不切换。
- **缩放锚点**：跟移动模式（右上角跟手指、左下角固定）对称设计——缩放时**左上角固定**，右下角跟手指走，`percentW`/`percentH` 按 `手指坐标 - percentX/percentY` 实时计算。
- **最小尺寸保护**：宽高下限锁定为热区自身尺寸（32dp 换算成百分比），防止键被缩小到比缩放热区还小，自己把自己缩没了导致以后摸不到热区、拖不大回来。
- 已实机验证：放大、缩小、移动三种操作互不误触发，缩到最小不会消失或跳变。

代码位置：`KeyboardView.java` 新增 `resizeKey` 字段、`drawResizeHandles()`、`isInResizeHandle()`；`handleEditTouch()` 在 `ACTION_DOWN` 分支里改为先判定是否命中缩放热区，再决定赋值给 `resizeKey` 还是 `dragKey`。

**用户反馈的开放问题（不算 bug，记录待用户后续决定是否处理）**："""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("STATUS.md 更新完成，2处全部成功")
