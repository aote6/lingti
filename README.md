# 灵体 (Unbounded)

复古终端风 · 自研交互框架 · 可替换引擎的 Android 输入平台

## 项目定位

自研交互框架 + 可替换引擎的 Android 输入平台。
V3 Platform 架构，布局/行为/主题三层分离。

## 当前版本

v3.0.0 — 平台化架构，3 种布局 + 3 种行为 + 3 套主题

## 核心特性

- 3 种键盘布局：九宫格 / 26键 QWERTY / 终端键盘
- 3 种输入行为：T9中文 / 直接输入 / 真实终端键码（KeyEvent）
- 场景自动切换：Termux → 终端模式，编辑器 → 26键，其他 → 用户默认
- 真实终端键：Esc/Tab/Ctrl/方向键/Home/End/PgUp/PgDn/Ctrl+C组合键
- 候选词翻页：每页 4 词，候选栏上下滑动翻页，词频排序
- 手势驱动：五向滑动 + 长按气泡 + MultiTap 连击
- 3 套主题：默认荧光绿 / Amber琥珀 / IBM经典
- 横竖屏适配：九宫格竖3横6，26键自动适配
- 设置界面：布局/行为/主题/键盘高度可调
- CandidateProvider 插件化：T9Provider，预留 Rime/Emoji 接口

## 输入模式

| 布局 | 行为 | 适用场景 |
|------|------|---------|
| 九宫格 | T9中文 | 微信/短信等中文输入 |
| 九宫格 | 直接输入 | 英文 MultiTap |
| 26键 QWERTY | 直接输入 | 代码编辑器 |
| 终端键盘 | 终端键码 | Termux/SSH/vim |
| 终端键盘 | 直接输入 | 终端内文本输入 |

## 架构

Touch → GestureRecognizer → KeyboardGestureController → Command
  → KeyboardActionDispatcher → InputEngine → InputConnection

V3 五层分离：
- core/command — Command + KeyEventCommand + KeyChordCommand
- core/layout — KeyModel/RowSpec/LayoutProfile/KeyboardLayout/LayoutManager
- core/candidate — CandidateProvider/T9Provider/CandidateEngine/FrequencyCache
- core/theme — ThemeProfile/ThemeManager
- layouts/ — NineKeyLayout/Qwerty26Layout/UnexpectedTerminalLayout

## 编译与安装

~/storage/shared/lingti/build_simple.sh
termux-open ~/storage/shared/lingti/build/simple/unbounded-mvp.apk

Release:
~/storage/shared/lingti/build_release.sh

## 设置

1. 打开灵体 App → 启用输入法
2. 键盘设置 → 选择布局/行为/主题/高度
3. 切换输入框自动弹出

## GitHub

https://github.com/aote6/lingti

## 更新日志

v3.0.0 — V3 Platform：布局/行为/主题分离，真实终端键码，26键QWERTY，CandidateProvider插件化

v2.0.0 — 骨架重构：Command枚举化，JSON schema v2，ThemeTokens，候选翻页，MultiTap，终端模式

v1.0.0 — MVP：九宫格+T9查词，五向手势，长按气泡
