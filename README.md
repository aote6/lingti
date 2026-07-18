# 灵体 (Lingti)

可编程的 Android 输入输出框架。

不是输入法。是让你自己造输入法的引擎。

## 当前状态

框架层完成。布局层待重建。

## 架构

Touch → GestureRecognizer → KeyboardGestureController → Command → InputEngine → InputConnection

- Command.java — 命令抽象（INSERT_TEXT/BACKSPACE/KEY_EVENT/KEY_CHORD/CLIPBOARD）
- InputEngine.java — 命令执行，唯一输出到 InputConnection 的地方
- KeyboardGestureController.java — 手势识别+长按连续触发
- KeyboardRenderer.java — Canvas 渲染，逐行绘制按键
- RuleLoader.java — JSON 布局加载器
- ThemeTokens.java — 主题取色

## 构建

    bash build_simple.sh
    termux-open build/simple/unbounded-mvp.apk

纯命令行构建，不需要 Gradle。ecj → d8 → aapt → apksigner。

## 许可证

GPLv3
