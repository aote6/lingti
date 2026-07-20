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
  - `SimpleImeService.java` — `InputMethodService` 子类。`onCreateInputView()` 建 `inputRoot` 容器后调用 `rebuildKeyboard()`。`rebuildKeyboard()` 是布局层的唯一装配点，**现在还负责槽位管理**：`getActiveSlot()`/`setActiveSlot()` 读写 `SharedPreferences("lingti_prefs")` 里的 `active_slot`（默认 1）；根据槽位号拼出文件名 `layout_slot{N}.json`，该槽位文件在 `getExternalFilesDir()` 不存在时回退加载内置的 `default.json`；组装出 `onRestore`（删该槽位文件+清缓存+重建）和 `onSlotSwitch`（写入新槽位号+重建）两个回调，一起传给 `KeyboardView`。**这条链路 2026-07-19 上午之前是空的（`// TODO: JSON 布局加载`），当天首次接通，同一天下午加上了多槽位**。
  - `KeyboardView.java` — 被动接收已构造好的 `LayoutProfile`，自己不决定布局内容，持有 `KeyboardRenderer` + `KeyboardGestureController` + `LayoutManager`。**构造函数当前签名**：`KeyboardView(context, dispatcher, profile, layoutFileName, layoutStateName, onRestore, activeSlot, slotSwitchListener)`——这个签名已经变过几次，改动前务必先 `cat` 确认当前实际参数，不要凭本文档假设。
    - 顶部常驻一条 `controlBarHeight`（36dp）工具栏，**独立于键盘按键区域**，键盘整体通过 `candidateBarHeight` 让出这条空间，工具栏和字母键不再互相遮挡（历史上出现过遮挡bug，已修复）。
    - 工具栏左侧：3个槽位按钮（1/2/3），常驻显示，不需要进编辑模式即可点击切换，当前槽位高亮。
    - 工具栏右侧：编辑/退出 开关（常驻），保存、还原（仅编辑模式下显示）。
    - **编辑模式**：`layoutManager.convertAllToPercent()` 把所有键（不论原是 span 还是百分比）统一转成百分比坐标，此后拖拽只操作百分比字段。拖拽锚点是**键的右上角对准手指**（不是左上角、也不是保持初始按压偏移）——这是用户明确要求的设计，因为右手拇指从左上角会挡住被拖动的键。
    - **删除功能**：编辑模式下屏幕最下方一条 `trashZoneHeight`（40dp）红色提示带，把键拖进这条区域松手即删除（`LayoutProfile.removeKey()` 摘除数据 + `KeyboardGestureController.updateKeys()` 刷新命中列表）。**这里有个必须记住的坑**：`KeyboardGestureController` 的按键列表原本是构造时的一份固定快照，删除/新增键之后如果不调用 `updateKeys()` 刷新，会出现"画面上键已经消失、但原来的位置摸上去还能触发它"的幽灵键 bug——已修复，但以后任何会改变按键数量的新功能（比如以后的组件库拖入新键）都要记得同样调用 `updateKeys()`。
    - **保存反馈**：不用系统 `Toast`（输入法进程里 Toast 经常被系统静默拦截，不可靠，已实测确认无效），改成 `showFlash()` 在画布上自己画一条临时提示框，1.2秒后自动消失。

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

## 2026-07-19 当天下午/晚上完成的工作（同一天第二轮会话）

上午重建布局层之后，用户实机体验并明确提出四个新需求：删除不想要的按键、拖拽缩放键的大小、多套可切换的布局（不同 App 用不同设计）、预置组件库（方向键组/剪贴板组合等模板拖入画布）。经确认优先级：**多套布局用手动 1/2/3 切换（不做自动识别 App），先做多套布局，再做删除，缩放和组件库押后**。

完成的功能（均已实机验证）：

1. **拖拽摆放模式**（当天最核心的里程碑，直接命中"设计沟通成本太高"这个真实瓶颈）：编辑模式开关 + 拖拽移动 + 保存 + 还原。`KeyModel` 的百分比坐标字段从 `final` 改为可变，加 `setPercentRect()`/`setPercentPosition()`。`LayoutManager.convertAllToPercent()` 把所有键统一转百分比。`RuleLoader.save()` 把当前 `LayoutProfile` 序列化回 JSON 写入 `getExternalFilesDir()`，`serializeCommand()` 与 `parseCommand()` 严格对称，保证读写互相兼容。
2. **多套布局槽位（1/2/3，手动切换）**：`SimpleImeService` 管理 `layout_slot{1,2,3}.json` 三个独立文件，当前槽位号存 `SharedPreferences`，重启不丢。每个槽位各自独立保存/还原，互不影响。
3. **删除按键**：拖进屏幕底部的删除区松手即删除。同时发现并修复了一个潜在的幽灵键 bug（见架构一节 `KeyboardGestureController.updateKeys()` 说明）——这是本轮最容易被忽略但影响很大的一处修复。
4. **保存操作的用户反馈**：发现输入法进程里系统 `Toast` 不出现（已实机验证确认不可靠），改用画布内自绘的临时提示 `showFlash()`。
5. **工具栏与键盘按键区域分离**：早期版本编辑/保存/还原按钮是浮在键盘右上角，会遮挡第一排字母键（Q/W 等），改成独立一条常驻工具栏（`controlBarHeight`），键盘整体让出这条空间。

**中途踩过的坑，写下来避免重复**：
- 还原按钮第一版只删了外部 JSON 文件，忘记清 `RuleLoader` 的静态内存缓存（`cache` 这个 `Map`），导致"删了文件但读到的还是缓存里旧数据"，表现为"还原了但好像没还原"。加了 `RuleLoader.clearCache(fileName)`，还原前必须先调用它。
- 打补丁时如果 Python 脚本因为其他原因没真正执行（比如命令行输出被截断、误跳过了某一步），编译时会报"方法未定义"之类的 `ERROR`（不是 warning）。`build_simple.sh` 里有 `set -e`，编译失败时脚本会在 `ecj` 那一步直接中断，**不会生成新 APK**，此时如果紧接着跑 `termux-open`，打开的其实是上一次编译成功的旧 APK——测试结果会显示"一切正常"，但那是假象，测的是旧版本。**每次编译后必须确认输出里真的出现"构建完成"字样，并且没有 `ERROR`（只有 `WARNING` 可以忽略），才能信任接下来的装机测试结果。**

## 2026-07-19 晚间新增：拖拽缩放按键大小

上一轮完成拖拽移动之后，用户确认的下一优先级功能。设计沿用移动模式已有的"对角固定支点"心智模型：

- **热区**：每个键右下角固定 32dp 见方的触摸区域（不用百分比换算，避免小键的热区跟着一起缩小到摸不准）。编辑模式下热区位置画一个小三角图标提示，非可选项——没有视觉提示用户不知道要去摸那个角。
- **判定时机**：手指按下（`ACTION_DOWN`）的瞬间一次性判定落点是否在热区内，决定进入"缩放模式"还是"移动模式"，过程中不切换。
- **缩放锚点**：跟移动模式（右上角跟手指、左下角固定）对称设计——缩放时**左上角固定**，右下角跟手指走，`percentW`/`percentH` 按 `手指坐标 - percentX/percentY` 实时计算。
- **最小尺寸保护**：宽高下限锁定为热区自身尺寸（32dp 换算成百分比），防止键被缩小到比缩放热区还小，自己把自己缩没了导致以后摸不到热区、拖不大回来。
- 已实机验证：放大、缩小、移动三种操作互不误触发，缩到最小不会消失或跳变。

代码位置：`KeyboardView.java` 新增 `resizeKey` 字段、`drawResizeHandles()`、`isInResizeHandle()`；`handleEditTouch()` 在 `ACTION_DOWN` 分支里改为先判定是否命中缩放热区，再决定赋值给 `resizeKey` 还是 `dragKey`。

**用户反馈的开放问题（不算 bug，记录待用户后续决定是否处理）**：
- 空格键、回车键因为 `span` 明显大于普通字母键、且贴在屏幕最下方，拖拽手感比小键别扭（不是判定逻辑错，是几何形状导致操作体验差）。以后如果需要改善，可以加"长按弹出删除确认"作为拖拽删除之外的备选方案。
- 用户提到"几小时前保存的布局后来好像变回了原始样子"，但不确定是否真的丢失、无法稳定复现。**Claude 提出的假设、用户选择暂不处理**：怀疑是华为 EMUI 系统对长时间后台的 App 有激进的进程清理/省电策略，可能清空过一次应用状态；也可能是当时切到了一个从未存过内容、自动回退显示出厂样式的槽位，误以为是"丢失"。**这件事目前没有定论，如果未来又复现，应引导用户去手机设置里把灵体加入"受保护应用/启动管理"白名单，排除系统杀后台的可能性，而不是默认怀疑代码逻辑。**

## 已知遗留 / 未完成

- [ ] **预置组件库**：编辑模式里加一个"素材面板"，放几个预先搭好的模板按钮（方向键组、剪贴板+回车组合等），拖一个进画布就新建一个键，不用手动配置 command。纯加法，不影响已有功能，优先级最低，可随时插入。
- [ ] `KeyboardRenderer.drawKeyboard()` 里 `w`/`h` 两个局部变量未使用（去掉重复坐标计算后的历史遗留），无害。
- [ ] `SimpleImeService.pasteRecentClipboard()` 未被调用，历史遗留死代码。
- [ ] `LayoutManager.currentLayout` 字段赋值后未被读取，历史遗留。
- [ ] `default.json`（出厂默认布局）目前只是一份手写的 QWERTY 测试布局，不是最终产品布局。
- [ ] 同一行内百分比键和 span 键混用时，目前会整行退化为 span 处理并打印警告，没有在 `RuleLoader` 加载阶段做 assert 硬拦截。
- [ ] 空格/回车键拖拽删除手感别扭（见上，用户认为问题不大，暂不处理）。
- [ ] "布局好像自动还原过一次"的现象未确诊（见上，用户选择暂不深究，以后复现了再排查，优先怀疑 EMUI 后台清理而非代码逻辑）。

## 2026-07-20 本轮会话总结（交接给下一个新会话，务必先读完这一节再动手）

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

## 给新会话的 Claude 的行动准则

1. 改代码前必须先 `cat -n` 实际文件内容，不要用本文档里贴出的代码片段直接当作当前源码去写 patch —— 本文档描述的是逻辑和结构，具体行号和上下文可能已随后续改动漂移。
2. 遵守"工作流约定"一节的每一条，尤其是 Python assert 补丁模式和编译验证步骤。
3. 不要把这个项目和用户的另一个项目"无界（Unbounded，Python 路格来克游戏）"的架构、约定、文件混淆——两者是完全独立的项目，各自的 STATUS.md 只覆盖自己的范围。
4. 用户的开发环境高度受限（纯手机 Termux），任何建议必须在这个约束下可执行，不要建议需要 PC/IDE/Gradle/Android Studio 的方案。
