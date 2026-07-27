import pathlib
path = pathlib.Path("STATUS.md")
text = path.read_text(encoding="utf-8")

addition = """

## 2026-07-27 剪贴板类型命令序列化bug最终修复

**背景**：2026-07-20会话发现并确诊了根因（RuleLoader.parseCommand()/serializeCommand()不认识CLIPBOARD_OPEN_PANEL/CLIPBOARD_PASTE_RECENT类型），但当时只写了诊断代码和交接记录，没有真正动手改RuleLoader.java。2026-07-27下午先做了PasteManager限速粘贴（另一个独立问题：PTY大段粘贴丢字符），处理完之后误以为顺手也把这个序列化bug修了，实际上当天全部8条写操作都没碰过RuleLoader.java——通过`rz lingti`操作日志核实确认。当天晚些时候重新定位、真正修复。

**修复内容**：
- `RuleLoader.parseCommand()` 新增 `clipboard_open_panel` / `clipboard_paste_recent` 两个case，分别对应 `Command.clipboardOpenPanel()` / `Command.clipboardPasteRecent()`
- `RuleLoader.serializeCommand()` 新增 `CLIPBOARD_OPEN_PANEL` / `CLIPBOARD_PASTE_RECENT` 两个case，写入对应的 `type` 字符串
- 删除了284-285行那条过时且有误导性的注释（原注释声称"剪贴板键靠label硬匹配触发，不受影响"，实际组件库生成的粘贴键走的正是Command序列化这条路，注释本身是错的）

**验证方式**：编辑模式生成粘贴键 → 保存 → 切换输入法再切回来（强制从JSON重新加载）→ 点击粘贴键。实机截图确认：诊断提示显示`cmd=CLIPBOARD_OPEN_PANEL`，说明命令类型完整存活过一次保存+重新加载。切换输入法前后剪贴板历史记录保留，操作正常。

**诊断代码现状**：`SimpleImeService`里的`showDiagFlash`临时诊断调用**保留不删**——用户认为闪一下就消失，不影响使用，且调试阶段有用，以后有需要再决定是否移除。

**排查过程的教训（写给以后的会话）**：确诊根因和真正落地修复之间隔了一周，中间又被另一个问题（PasteManager）插队，导致"以为修过"和"实际修过"出现偏差。以后如果STATUS.md里记录"已确诊根因"但没有明确的commit记录对应这次修复，新会话开始时应该主动用`git log`或`rz`日志核实一遍，不要假设"记录过就等于做过"。
"""

assert addition not in text, "该记录已存在，避免重复追加"
text = text + addition
path.write_text(text, encoding="utf-8")
print("STATUS.md 更新完成")
