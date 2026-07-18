
灵体 (Unbounded)

复古终端风 · 自研交互框架 · 可替换引擎的 Android 输入平台

项目定位

自研交互框架 + 可替换引擎的 Android 输入平台。
交互层已成型，引擎层待接入 Rime。

当前版本

v2.0.0 — 骨架重构完成，3 种输入模式，场景自动切换


核心特性

· 3 种输入模式：中文 T9 查词 / 英文 MultiTap / 终端快捷键
· 场景自动切换：Termux 终端 → 终端模式，代码编辑器 → 英文，其他 → 中文
· 候选词翻页：每页 4 词，候选栏上下滑动翻页，页码指示器
· 手势驱动：五向滑动（上下左右）+ 长按气泡选择 + MultiTap 连击
· 复古终端 UI：纯黑底 + 荧光绿 + 等宽字体 + ThemeTokens 主题系统
· 横竖屏适配：竖屏 3 列，横屏 6 列，字号自动补偿
· 设置界面：默认输入模式切换、键盘高度调节（200-400dp）
· 零权限沙盒日志：崩溃日志写入 getExternalFilesDir


输入模式

模式 触发条件 键位标签 输入逻辑
中文 默认 / 微信等 ABC / DEF / ... 数字累积 → T9 查词
英文 编辑器 / 手动切换 ABC / DEF / ... 数字连击 → MultiTap 选字母
终端 Termux / SSH Esc / Tab / Ctrl / 箭头 直接输出快捷键

英文 MultiTap

· 同键连击循环选字母（2→a→b→c→2...）
· 800ms 超时自动确认当前字母
· 长按候选栏立即确认
· 不同数字键自动确认上一字母

终端模式键位

第1行: Esc / Tab /
第2行: - / : / !
第3行: ( / Up / Home
第4行: Ctrl / 空格 / 退格
Swipe: F1~F4, 方向键, Alt, Del, Enter, PgUp/PgDn


手势定义

手势 动作
TAP 点击
SWIPE_UP 上滑
SWIPE_DOWN 下滑
SWIPE_LEFT 左滑
SWIPE_RIGHT 右滑
LONG_PRESS 长按弹出气泡

架构

Touch → GestureRecognizer → KeyboardGestureController → Command → KeyboardActionDispatcher → InputEngine → InputConnection

五层分离：

· Domain 层：Command（枚举类型化）、KeySlot、Gesture
· Rule 层：RuleLoader + LayoutConfig（JSON schema v2）
· Session 层：NineKeyKeyboard（协调 + 状态管理）
· View 层：KeyboardRenderer（纯绘制）+ ThemeTokens
· Executor 层：InputEngine（Command → InputConnection）


编译与安装

环境要求

· Termux（Android 终端模拟器）
· Android SDK（platforms/android-34）
· ecj（Eclipse Compiler for Java）
· d8 + aapt + apksigner（Android build-tools 34.0.0）

Debug 构建（5-10 秒）

~/storage/shared/lingti/build_simple.sh
termux-open ~/storage/shared/lingti/build/simple/unbounded-mvp.apk

Release 构建

~/storage/shared/lingti/build_release.sh
termux-open ~/storage/shared/lingti/build/release/unbounded-release.apk

首次 release 会自动生成 release.keystore（RSA 2048，有效期 100 年）。

编译链

ecj (Java 8) → .class → d8 → classes.dex
aapt → resources.apk
zip → APK → apksigner → 签名 APK


设置

1. 打开灵体 App → 点击"启用输入法" → 系统设置中启用"灵体"
2. 点击"键盘设置" → 选择默认输入模式 / 调节键盘高度
3. 切换到任意输入框 → 自动弹出键盘

设置存储：SharedPreferences lingti_prefs

GitHub

仓库：https://github.com/aote6/lingti

提交命令：
cd ~/storage/shared/lingti
git add -A
git commit -m "msg"
git push

已知限制

· T9 词库较小（约 80 条），中文输入覆盖有限
· 终端模式键位发送的是文本（如 "Esc"），非实际控制码
· 未接入 Rime 引擎
· 无英文预测输入
· 无换肤功能（主题写死在 ThemeTokens）

