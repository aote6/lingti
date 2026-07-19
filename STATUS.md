# 灵体 (Lingti) — 项目状态文档

> 本文档是跨会话的唯一权威状态记录。每次新开对话，先读这份文档，不要要求用户重新解释项目背景。

## 核心结论（最重要，新会话必读，决定了下一步该做什么）

**2026-07-18 之前的实验已经证明：这个框架的执行能力不是瓶颈。** 用户用"多个 AI 集体穷举功能"的方式，把能想到的交互全部实现过一遍——单键上下左右四向滑动、长按、长按后七选一弹出菜单、词库导入——**全都做出来了，而且都能跑**。

**真正的瓶颈是：设计沟通成本。** 做出来之后发现"所有功能都能用，但都不好用"——不是技术做不到，是"这个按钮多大、放哪、跟旁边的关系是什么"这类布局设计决策，靠语言描述给 AI 听，说不清楚、传不准确。最后用户把这批功能全部删除重来（对应 commit `3f16e3f` 大清扫），只留下框架骨架。

**结论：不要再建议"多加功能"。** 下一步的唯一优先级是降低布局设计的沟通成本本身，具体来说就是让用户能"直接摆"而不是"用语言描述怎么摆"——这也是 2026-07-19 当天先做百分比坐标、再打算做拖拽摆放模式的原因。任何新会话如果发现讨论又滑向"要不要加个新手势/新功能"，应该先反问一句：这个功能的布局位置，用户能不能不靠打字描述就设计出来？如果不能，先别做，先解决"怎么摆"这件事。

## 项目是什么

一个可编程的 Android 输入输出框架，**不是输入法**，是让用户自己设计输入方式和快捷键盘的引擎。

起点是想做九宫格/26键/真键盘输入法，后来发现单人开发不可能做出比现成输入法引擎更好用的东西。真正想要的是：**把几个自己常用的操作组合成自定义按钮和布局**，没有模板可抄，因为这类需求没有现成的参照物。

设计核心矛盾：布局本质是空间信息，但过去一直在用代码/文字去描述它，这天然困难。当前架构方向是把"描述空间关系"这件事从写 Java 代码，逐步转移到写 JSON 配置，未来目标是可视化拖拽定位。

## 开发环境（重要，每次都要遵守）

- 单人开发者，**只用手机（Huawei P20 + Termux）**，没有 PC、没有 IDE、没有 Android Studio。
- 所有代码交互靠复制粘贴终端命令完成。
- **不用 Gradle**，用手写脚本 `build_simple.sh` / `build_release.sh`：`ecj`（Eclipse 编译器）→ `aapt` 打资源 → `d8` 转 dex → `apksigner` 签名。
- 编译命令：`bash build_simple.sh`，产物在 `build/simple/unbounded-mvp.apk`，装机用 `termux-open build/simple/unbounded-mvp.apk`。
- 项目路径：`~/storage/shared/lingti`
- 打补丁的文件放 `~/storage/shared/lingti/.patches/`（这个目录已存在）。

## 改代码的工作流约定（严格遵守，不要自创别的方式）

1. **先看文件再改**：改任何文件前，先 `cat -n` 完整看一遍当前内容，不要凭记忆或凭猜测写 patch。这个项目历史上多次因为不同 AI 工具（Claude/DeepSeek/Gemini）对同一份代码有不同假设而踩坑。
2. **Java 文件用 Python 脚本打补丁**，不要用 `sed` 做多行替换（正则容易匹配失败或误伤），模式固定为：
   ```python
   import pathlib
   path = pathlib.Path("相对路径/File.java")
   text = path.read_text(encoding="utf-8")
   old = """...要被替换的原文，逐字复制，不要有 view 命令的行号前缀..."""
   new = """...替换后的内容..."""
   assert text.count(old) == 1, "锚点未找到或不唯一"
   text = text.replace(old, new, 1)
   path.write_text(text, encoding="utf-8")
   print("XXX.java 打补丁完成")
   ```
   每个文件的每处改动单独一个 assert，失败就说明贴的原文跟实际文件不一致，必须停下来重新核对，不能强行改。
3. **小文件（几十行以内）直接整体用 `cat > file << 'EOF' ... EOF` 重写**，比патч更不容易出错。
4. **每次改完 Java 文件，先做括号计数校验再编译**（能提前抓到明显的语法崩坏）：
   ```bash
   open=$(grep -o "{" 文件路径 | wc -l)
   close=$(grep -o "}" 文件路径 | wc -l)
   echo "brace: { $open } $close $([ "$open" = "$close" ] && echo OK || echo 不匹配)"
   ```
5. **真正的验证是编译**：`bash build_simple.sh 2>&1 | tail -60`，看 ecj 报错的文件名+行号定位问题。历史遗留的几条 warning（未使用的变量/方法）是无害的，不用管，除非新增了同名 warning 才需要关注是不是新引入的問题。
6. **JSON 配置改动，用 Python 的 `json` 模块读写并格式化**，不要手改容易漏逗号；改完用 `python3 -m json.tool` 校验合法性。
7. 装机测试：`termux-open build/simple/unbounded-mvp.apk`，用户实机截图反馈问题，Claude 根据截图现象反查代码定位，不要凭空猜测。

## 架构（当前，2026-07-19 后）

```
Touch → GestureRecognizer → KeyboardGestureController → Command → InputEngine → InputConnection
```

分层职责（这次会话重点确认过的，不是猜测）：

- **布局数据层**（`core/layout/`）
  - `KeyModel.java` — 单个按键：id/label/span/padding/rect + 6个Command槽位（tap/四向swipe/longPress）。**现已支持百分比坐标**（`hasPercentRect` + `percentX/Y/W/H`，0-100），用于独立于 span 网格的浮动按键。
  - `RowSpec.java` — 一行按键的列表，提供 `totalSpan()`；新增 `isPercentRow()` / `isMixedRow()` 判断整行坐标模式。
  - `LayoutManager.java` — **唯一权威**的 rect 坐标计算入口。`computeRects()` 区分"纯 span 行"（网格流式布局）和"纯百分比行"（绝对定位浮动键），同一行内混用会被记录警告并退化为 span 处理。
  - `LayoutProfile.java` / `KeyboardLayout.java` — 布局的容器和接口，具体值不在本文档展开，改动前先 `cat` 一遍。
  - `RuleLoader.java` — JSON 加载器，`load(context, fileName)` 从 assets 读取，`parseKeyDef()` 支持 `span` 和 `x/y/w/h`（四者都存在才生效），`parseCommand()` 支持 `insert`/`backspace`/`commit`/`key_event`/`key_chord`。**`key_event` 可以配置任意 Android keyCode（包括 Enter=66、方向键等），命令键完全可以通过 JSON 配置，不需要侧载 Java。**
- **渲染层**
  - `KeyboardRenderer.java` — **只负责画，不计算坐标**。之前有一版 bug：`drawKeyboard()` 内部重复计算了一套基于 span 的坐标并覆盖 `key.rect`，导致 `LayoutManager` 算好的百分比坐标被吃掉。已修复：渲染器现在直接信任并使用 `key.rect`（由 `LayoutManager` 唯一写入）。
- **手势与命中判定层**
  - `GestureRecognizer.java` — 纯数学：根据坐标位移判断 TAP/四向 SWIPE/LONG_PRESS，不知道 UI 或按键的存在。
  - `KeyboardGestureController.java` — 持有 `GestureRecognizer`，处理触摸事件流、长按计时（450ms）、连续触发（`ContinuousDeleteHelper`）。`findKey(x, y)` 做命中判定，**已修复为倒序遍历**：列表末尾的键最后绘制、视觉在最上层，触摸命中必须优先判给最上层的键，重叠区域（比如浮动键盖住网格键）才不会出现"看着点的是A，实际触发B"的错位。
- **命令与执行层**
  - `Command.java` — 抽象基类 + Type 枚举（`INSERT_TEXT/BACKSPACE/COMMIT/KEY_EVENT/KEY_CHORD/CLIPBOARD_*`）+ 静态工厂方法。各类型有独立子类（`InsertText`/`Backspace`/`Commit`/`ClipboardCommand`，`KeyEventCommand`/`KeyChordCommand` 在 `core/command/` 下），**是真解耦**，`KeyboardActionDispatcher` 里没有 `if (id.equals(...))` 硬编码判断。
  - `InputEngine.java` — 唯一执行点，把 Command 派发到 `InputConnection`。**重要认知**：`COMMIT` 类型对应 `ic.finishComposingText()`，这**不等于**发送回车/换行，它只在有"正在拼写中的组合文本"时才有效果。当前所有文本输入都走 `commitText()` 直接提交，从不进入 composing 状态，所以 `COMMIT` 命令在当前架构下是空操作。**需要真正的 Enter 效果时，用 `key_event` 配置 `keyCode:66`（KEYCODE_ENTER）**，不要用 `commit` 类型。
- **服务入口**
  - `SimpleImeService.java` — `InputMethodService` 子类。`onCreateInputView()` 建 `inputRoot` 容器后调用 `rebuildKeyboard()`。`rebuildKeyboard()` 是布局层的唯一装配点：`RuleLoader.load(this, "default.json").buildProfile()` → `new KeyboardView(this, dispatcher, profile)` → `inputRoot.addView(...)`。**这条链路 2026-07-19 之前是空的（`// TODO: JSON 布局加载`），当天首次接通**。
  - `KeyboardView.java` — 被动接收已构造好的 `LayoutProfile`，自己不决定布局内容，持有 `KeyboardRenderer` + `KeyboardGestureController` + `LayoutManager`。

## 存储路径规则（踩过坑，记住）

- `~/storage/shared/lingti` 是 Termux 的共享存储路径，APP **不能**直接写这里。
- APP 运行时可写：`getFilesDir()`（`/data/data/com.unbounded.input/files/`，**非 root 的 Termux 访问不了**）或 `getExternalFilesDir()`（`/sdcard/Android/data/com.unbounded.input/files/`，配合 `termux-setup-storage` 后 Termux 可以直接访问）。
- **未来做"拖拽摆放模式写回 JSON"时，必须写到 `getExternalFilesDir()`**，否则 Termux 端读不到、改不了，自己把自己锁死。`RuleLoader` 加载时应优先从这个可写目录找配置，找不到再退回 assets 里的默认配置。

## 2026-07-19 当天完成的工作（本轮会话）

背景：前一天做了"大清扫"（commit `3f16e3f`/`49634e4`），删除了九宫格/26键/T9词库/候选引擎/旧JSON残留，只保留框架骨架，README 状态写的是"框架层完成，布局层待重建"。

当天从零重建布局层，完成：

1. **百分比坐标能力**：`KeyModel`/`RowSpec`/`LayoutManager`/`RuleLoader` 四个文件加上 x/y/w/h 支持，允许配置独立于 span 网格、可以自由悬浮、甚至故意叠放的按键。
2. **JSON 布局装配链路接通**：`SimpleImeService.rebuildKeyboard()` 里的 `// TODO: JSON 布局加载` 改成真正调用 `RuleLoader.load()` → `buildProfile()` → `new KeyboardView()` → `addView()`。同时清掉了 `onCreateInputView()` 里重复声明但从未使用的死代码 dispatcher。
3. **修复渲染层坐标覆盖 bug**：`KeyboardRenderer.drawKeyboard()` 原来会重新计算一遍 span 坐标并覆盖 `key.rect`，导致百分比坐标看似配置了但从不生效。已改为渲染器只画不算，`LayoutManager` 是唯一坐标权威。
4. **修复触摸命中层级 bug**：`findKey()` 原来正向遍历（先创建的键优先命中），跟渲染的视觉层级（后画的在最上层）方向相反，导致浮动键和被它盖住的网格键点击判定错乱。已改为倒序遍历，命中优先级与视觉层级一致。
5. 新建测试用 `app/src/main/assets/default.json`：QWERTY 三排字母 + 空格 + 回车（`key_event keyCode:66`）+ 一个百分比坐标悬浮测试键 "Fn"。

以上 4 处代码修复均已过 `ecj` 编译验证，并在真机上通过实际点击测试确认修复生效（浮动键不再被 span 布局吃掉位置，回车键有实际换行效果，重叠区域点击命中正确的可视层）。

## 已知遗留 / 未完成

- [ ] **拖拽摆放模式**：长按按钮拖拽定位、松手写回 JSON。依赖百分比坐标（已完成），需要新写触摸交互 + 文件写入逻辑（写到 `getExternalFilesDir()`，见上）。这是下一个自然的里程碑。
- [ ] `KeyboardRenderer.drawKeyboard()` 里 `w`/`h` 两个局部变量现在未使用（是这次去掉重复坐标计算后的副作用），无害但可以顺手清理。
- [ ] `SimpleImeService.pasteRecentClipboard()` 未被调用，历史遗留死代码。
- [ ] `LayoutManager.currentLayout` 字段赋值后未被读取，历史遗留。
- [ ] `default.json` 目前只是一份手写的测试布局（QWERTY + 一个悬浮键），不是最终产品布局，后续会被用户自定义的按钮组合替代。
- [ ] 同一行内百分比键和 span 键混用时，目前会整行退化为 span 处理并打印警告，但没有在 `RuleLoader` 加载阶段做 assert 硬拦截——这个需要谨慎，因为 `RuleLoader.load()` 现在真正跑在生产链路上了，之前建议的"加载时 assert 报错"要重新评估是直接崩溃更好还是保留静默降级更好。

## 给新会话的 Claude 的行动准则

1. 改代码前必须先 `cat -n` 实际文件内容，不要用本文档里贴出的代码片段直接当作当前源码去写 patch —— 本文档描述的是逻辑和结构，具体行号和上下文可能已随后续改动漂移。
2. 遵守"工作流约定"一节的每一条，尤其是 Python assert 补丁模式和编译验证步骤。
3. 不要把这个项目和用户的另一个项目"无界（Unbounded，Python 路格来克游戏）"的架构、约定、文件混淆——两者是完全独立的项目，各自的 STATUS.md 只覆盖自己的范围。
4. 用户的开发环境高度受限（纯手机 Termux），任何建议必须在这个约束下可执行，不要建议需要 PC/IDE/Gradle/Android Studio 的方案。
