import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/STATUS.md")
text = path.read_text(encoding="utf-8")

old = """## 给新会话的 Claude 的行动准则"""
new = """## 2026-07-20 本轮会话总结（交接给下一个新会话，务必先读完这一节再动手）

本轮做了两件事：新增"预置组件库"功能，以及排查一个横跨多轮对话才定位到根因的"粘贴键没反应"的 bug。**排查过程走了不少弯路，记录下来是为了不让下一个会话重复踩坑。**

### 已完成并验证通过的功能

1. **拖拽缩放按键大小**：右下角32dp热区、左上角固定支点、最小尺寸保护。详见上一节"2026-07-19 晚间新增"。已实机验证放大缩小移动互不误触发。
2. **预置组件库**：编辑模式工具栏新增"组件"按钮，点击弹出悬浮面板，可选"方向键组"（↑↓←→，KEY_EVENT类型发送DPAD方向键）或"剪贴板+回车"（两个键：粘贴、回车）。点选后新键以百分比坐标生成在画布中间，可用已有的拖拽/缩放功能挪动。
   - 中途修了两个UI bug：①工具栏按钮太宽导致"组件"按钮和槽位3重叠——收窄了`btnW`/`gap`；②面板点开瞬间又被同一次点击的抬起事件当成"点空白处"误关闭——加了`justOpenedComponentPanel`标记区分"打开面板这次点击的抬起"和"后续真正选择的点击"。这两处已实机确认修好。
3. **删除键选区bug**（这次意外发现的独立老问题，不是这次新引入的）：`InputEngine.deleteCharBeforeCursor()`原来完全不检查有没有选区，永远删光标前一个字符，导致长按框选一段文字后按删除键，删掉的是选区之外的内容。已修复：删除前先判断`ic.getSelectedText(0)`，有选区就删选区（`ic.commitText("", 1)`），没有才走原来的逐字删除逻辑。**已实机确认修复生效。**

### 本轮最核心的发现：组件库生成的剪贴板类型键，保存后会静默失效

**根因（已在代码里找到实锤，不是猜测）**：`RuleLoader.java`的`serializeCommand()`/`parseCommand()`两个方法从一开始就不支持`CLIPBOARD_OPEN_PANEL`/`CLIPBOARD_PASTE_RECENT`这两个Command类型（代码里第284-285行本来就写着注释承认这件事）。组件库新加的"粘贴"键用的正是`Command.clipboardOpenPanel()`，也就是`CLIPBOARD_OPEN_PANEL`类型。这个键第一次生成、还活在内存里的时候点击是正常的（已截图验证过一次，弹出了"剪贴板为空"）。但只要触发过一次保存（点"保存"按钮，或者IME进程被系统重启导致重新从JSON加载），这个键的`tap`字段在序列化时会因为不认识这个类型而被直接丢弃；重新解析JSON时，`parseCommand()`对着缺失的字段返回`null`，这个键的`tap`就静默变成`null`了。点击一个`tap==null`的键，`KeyboardGestureController.execGesture()`最后一行`if (cmd != null) dispatcher.onCommand(cmd)`直接跳过，**连诊断提示都不会弹**——这跟排查过程中后几轮"完全零反应"的现象完全吻合。

**下一个会话要做的第一件事**：给`RuleLoader.parseCommand()`和`serializeCommand()`加上`CLIPBOARD_OPEN_PANEL`/`CLIPBOARD_PASTE_RECENT`这两个类型的支持（比如JSON里写`"type": "clipboard_open_panel"`），让它们能在存读之间正常往返，不再静默丢失。改完要用组件库重新生成一次剪贴板键，**保存、退出应用（或切换槽位再切回来强制重新加载）、再点击**，全程测通了才算真的修好，不能只测"刚生成、没保存过"这一种情况——这正是上一轮排查漏掉的地方。

**排查这个bug时走过的弯路，别重复**：
- 一开始怀疑过是系统剪贴板没监听到外部App复制的内容，改了`SimpleImeService.rebuildKeyboard()`里的dispatcher把`CLIPBOARD_OPEN_PANEL`接到`keyboardView.openClipboardPanel()`——这一步本身是对的、也确实修好了（第一次截图证实了：命令被正确识别、面板真的弹出来了、显示"剪贴板为空"）。但后续测试出现"完全零反应"，一开始又怀疑过是"没点中按键"、装的是旧APK、Termux读不了日志权限（这条走了很久弯路：想用`getExternalFilesDir()`写诊断日志，结果连`layout_slot*.json`这种已知存在的文件用`find /`都搜不到，`logcat`读不到别的App日志因为非root权限限制、`run-as`命令Termux里也没有——最后放弃"读文件/日志"这条路，改用`KeyboardView.showFlash()`直接在屏幕上显示诊断文字，这条路才是真正走通、拿到关键证据的方式）。
- **教训**：以后遇到"看起来毫无反应"的bug，如果怀疑是命令没送达，优先用"屏幕上直接显示诊断文字"这种方式验证，不要指望读取APP私有目录的文件或日志——这台设备上Termux对灵体APP进程的文件系统和logcat都没有直接读取权限，`run-as`命令也不存在。

### 当前代码里遗留的临时诊断代码，必须记得清理

为了排查上述bug，在`SimpleImeService.java`的dispatcher里加了一行：每次任何命令被触发，都会调用`keyboardView.showDiagFlash("cmd=" + 类型)`弹出屏幕提示。**这是排查用的临时代码，现在还留在代码里**，正常使用时每按一个键屏幕上都会弹提示，非常打扰。等剪贴板序列化的bug彻底修好、确认没有问题之后，第一件事就是把这行`showDiagFlash`调用删掉（`KeyboardView.java`里新增的`showDiagFlash()`公开方法可以保留，作为以后调试用的通用接口，只删掉`SimpleImeService`里这次加的调用点）。

### 还没做、已明确但优先级排在后面的事

1. **组件面板改版**：用户明确提出，现在的"图形按钮悬浮面板"应该改成**分类文字列表**——比如"方向键类""数字类""符号类""真键盘类（Tab/Esc等）"这样的分类标题，点进去是这一类里配套使用的键（比如复制+粘贴算一组，数字0-9算一组），一次性整组拖出来，比现在预先画好图形按钮更直观、更符合"降低沟通成本"的核心结论。这是本轮组件库v1之后的下一版方向，尚未开始写代码。
2. **删除按键的表情符号**：用户明确说自己是"复古经典极客风格"，现有的📋剪贴板emoji和这次新加的文字按钮风格不搭，emoji太跳跃，要找机会顺手换掉（换成纯文字或者ASCII风格的符号）。
3. **两套并存的剪贴板逻辑，架构上是矛盾的**：`KeyboardGestureController.execGesture()`里有一段写死的`"📋".equals(activeKey.label)`判断，专门给老的"循环粘贴历史记录"功能用，跟STATUS.md里"真解耦，没有硬编码判断"这条架构原则矛盾，而且跟这次组件库新加的`CLIPBOARD_OPEN_PANEL`面板式粘贴是两条完全独立、没有统一过的逻辑。用户反馈"之前那个emoji粘贴键一直不好用"，大概率说的就是这条老逻辑。等这次的存读bug修完，值得考虑要不要把两套合并成一套。
4. **单独点选删除键**：现在只能拖进底部删除区。用户想要"点一下选中、再点专门的删除按钮"这种模式，需要先设计"编辑模式下有一个键处于被选中状态"这个新概念，改动不小，值得跟上面的组件面板改版一起规划。
5. **`termux-open`安装APK报"安装包解析错误"**：用户目前靠手动去文件管理器里找apk装来绕过，还没有诊断这个问题本身出在哪，优先级低，不影响开发流程。

## 给新会话的 Claude 的行动准则"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)
path.write_text(text, encoding="utf-8")
print("STATUS.md 交接总结写入完成")
