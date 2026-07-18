
灵体 (Unbounded) — 输入系统架构 v2.0

核心理念

不是输入法，是输入平台。事件驱动、规则驱动、平台无关的人机输入内核。

架构总览

Touch → GestureRecognizer → KeyboardGestureController → Command → KeyboardActionDispatcher → InputEngine → InputConnection

五层分离：

层 文件 职责
Domain Command.java / KeySlot 纯语义：命令类型枚举、键位槽
Rule RuleLoader.java / LayoutConfig JSON 规则加载 + schema 校验
Session NineKeyKeyboard.java 协调层：布局计算、状态管理、翻页
View KeyboardRenderer.java / ThemeTokens 纯绘制：键位、候选栏、气泡、主题色
Executor InputEngine.java Command → InputConnection 执行


设计原则

· 核心模块不知道业务，业务模块不能修改核心
· 一致性 > 灵活性
· 为已知差异抽象，不为假设差异抽象
· 删，不是加
· Command 不使用字符串 type，使用枚举类型化

关键设计决策

1. Command 类型化

Command 从 interface (String type()) 重构为 abstract class + Type 枚举：

· Command.Type: INSERT_TEXT / BACKSPACE / COMMIT / SPACE / ENTER / DEL / TAB / ESC / NOOP
· 工厂方法: Command.insert(text) / Command.backspace() / Command.commit()
· 消除字符串魔法值，编译期类型检查

2. 布局计算时机

键位矩形 (KeySlot.rect) 从 onDraw() 移到 onSizeChanged() → computeKeyRects()

· 解决首帧未绘制时触摸命中异常
· 状态在布局阶段确定，绘制时只读

3. IME 生命周期

补全关键方法：

· onFinishInput() → 清键盘 session
· onFinishInputView() → 释放 UI 状态
· onUpdateSelection() → 光标移动时重置候选
· 移除错误的 restartInput(keyboardView) 调用

4. 三层键盘拆分

NineKeyKeyboard (246行) 拆分为：

· KeyboardRenderer — 纯绘制（130行）
· KeyboardGestureController — 触摸处理（170行）
· NineKeyKeyboard — 协调层（95行）

5. JSON Schema v2

· 新增 version 字段（当前 v2）
· LayoutConfig 承载 version / layout / context / keys
· v1 旧配置自动检测并日志提醒升级
· 三套配置文件：default.json / default_english.json / default_terminal.json


输入模式

模式 触发条件 输入逻辑 键位标签
CHINESE 默认 / 微信等 数字累积 → T9Engine.getCandidates() ABC / DEF / ...
ENGLISH 编辑器 / 手动切换 数字连击 → MultiTapEngine.processDigit() ABC / DEF / ...
TERMINAL Termux / SSH 直接 commit 键位文本 Esc / Tab / Ctrl / ↑

英文 MultiTap 状态机

MultiTapEngine.java：

· 同键连击循环选字母（2 → a → b → c → 2...）
· 800ms 超时自动确认当前字母
· 长按候选栏立即确认全部已拼
· 不同数字键自动确认上一字母并开始新字母

终端模式键位

第1行: Esc / Tab /
第2行: - / : / !
第3行: ( / Up / Home
第4行: Ctrl / 空格 / 退格

Swipe: F1~F4, 方向键, Alt, Del, Enter, PgUp, PgDn


场景自动切换

SimpleImeService.detectContext(EditorInfo) 根据 App 包名返回 context：

· 包含 termux / terminal / ssh → "terminal"
· 包含 editor / code / vscode → "english"
· 其他 → "chinese"

context 映射到配置文件：

· "terminal" → default_terminal.json
· "english" → default_english.json
· "chinese" → default.json

每次 onStartInputView 时检测场景变化，context 变化时触发 rebuildKeyboard()，动态重建键盘 View。

用户可在设置界面设置默认模式，非终端/编辑器场景使用用户默认。

候选词系统

T9Engine.java：

· HashMap 词库（约 80 条，覆盖单字 + 常用词 + 长词组）
· getCandidates(digits) 返回完全匹配 + 前缀匹配去重

翻页机制：

· CANDIDATE_PAGE_SIZE = 4（每页 4 词）
· 候选栏上下滑动翻页（阈值 40px）
· 右上角页码指示器（如 2/3）
· NineKeyKeyboard 维护 candidatePage 状态

手势系统

GestureRecognizer.java：

· 枚举: NONE / TAP / SWIPE_UP / SWIPE_DOWN / SWIPE_LEFT / SWIPE_RIGHT / LONG_PRESS
· SWIPE_THRESHOLD = 18px（滑动触发阈值）
· onDown(x,y) → onMove(x,y) → onUp()
· 长按由 KeyboardGestureController 的 Handler.postDelayed(450ms) 实现

手势到命令的映射由 JSON 配置定义，每个 KeySlot 可配置 6 种手势对应不同的 Command。


主题系统

ThemeTokens.java 集中管理所有视觉常量：

背景层级: BG / SURFACE / SURFACE_RAISED
边框: BORDER / BORDER_ACTIVE
文本: TEXT_PRIMARY / TEXT_SECONDARY / TEXT_ACCENT
按压: PRESS_BG
字体: FONT = Typeface.MONOSPACE

工厂方法: newBgPaint() / newBorderPaint() / newTextPaint()

所有硬编码颜色已移除，KeyboardRenderer 完全通过 ThemeTokens 引用颜色。

横屏适配

NineKeyKeyboard.detectOrientation() 检测屏幕方向：

· 竖屏 (PORTRAIT): cols = 3
· 横屏 (LANDSCAPE): cols = 6

布局计算 Math.ceil(keys.size() / cols) 自动适配行数。
横屏时键位字号放大 1.3 倍补偿。

设置系统

SettingsActivity.java：

· 默认输入模式：中文 / 英文（终端模式仅场景切换，不参与手动选择）
· 键盘高度：SeekBar 调节 200-400dp
· 持久化：SharedPreferences "lingti_prefs"

SimpleImeService.getKeyboardHeight() 读取设置并应用到键盘 View 高度。


编译链

Debug 构建 (build_simple.sh):
ecj (Java 8) → .class → d8 → classes.dex
aapt → resources.apk
zip → APK → apksigner → unbounded-mvp.apk

Release 构建 (build_release.sh):
同流程，使用 release.keystore 签名

工具链：

· ecj (Eclipse Compiler for Java, Termux 包)
· d8 (Android build-tools 34.0.0, 替代已废弃的 dx)
· aapt + apksigner (Android build-tools 34.0.0)
· keytool (JDK, 生成 keystore)

新增 .java 文件需同步更新两个构建脚本的文件列表。

崩溃日志

SimpleImeService 维护静态日志系统：

· 路径: getExternalFilesDir(null) / lingti_debug.log
· 零权限沙盒，跨 App 可写
· Thread.setDefaultUncaughtExceptionHandler 捕获未处理异常
· log() 静态方法供全局调用

项目规模

源文件: 22 个 .java
配置文件: 3 个 assets JSON + 1 个 AndroidManifest.xml
构建脚本: build_simple.sh + build_release.sh
构建状态: 0 错误 0 warning
编译时间: 5-10 秒


已完成路线图

· 单键输入
· 单键多手势
· 九宫格键盘
· GestureRecognizer 手势识别
· Command 模式（枚举类型化）
· RuleLoader + JSON 规则加载
· JSON schema v2 版本化
· 候选词翻页系统
· 英文 MultiTap 引擎
· 终端模式布局
· 场景自动切换（App 包名识别）
· ThemeTokens 主题系统
· 键盘三层拆分（Renderer / GestureController / View）
· IME 生命周期补全
· 布局计算出 onDraw
· 横竖屏适配
· 设置界面 + 键盘高度调节
· Release 打包脚本
· 崩溃日志系统

下一步计划

· 接入 Rime 引擎（librime jni，需 Termux 交叉编译）
· T9 外部码表加载（从文件读取词库）
· 英文单词预测（基于词频 + 上下文）
· 多主题切换（换肤功能）
· 按键震动反馈（HapticFeedback）
· 剪贴板管理
· 单手模式
· 候选词排序优化（用户词频学习）
· 设置界面扩展（颜色选择、字体大小）

产品目标

学习成本最低，熟练后速度最高的输入系统。

复古终端美学 + 工业面板质感 + 机械反馈手感。

