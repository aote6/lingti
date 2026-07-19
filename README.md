# 灵体 (Lingti)

可编程的 Android 输入面板生成器。

不是输入法。是让你自己拼键盘的工具。

## 核心玩法

- 进入编辑模式，拖拽按钮到任意位置
- 每个按钮可以绑定：插入文本、发送按键、粘贴、组合键
- 3 个独立槽位，各自存盘、独立切换
- 槽位1放Termux快捷键面板，槽位2放微信快捷回复，槽位3放代码片段
- 点保存写入本地文件，点还原恢复出厂布局

## 架构

Touch → GestureRecognizer → KeyboardGestureController → Command → InputEngine → InputConnection

## 构建

bash build_simple.sh
termux-open build/simple/unbounded-mvp.apk

纯命令行构建，不需要Gradle。ecj → d8 → aapt → apksigner。

## 许可证

GPLv3
