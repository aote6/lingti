```
# 灵体 (Unbounded)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green.svg)](https://developer.android.com)

> 复古终端风 · 自研交互框架 · 可替换引擎的 Android 输入平台

**灵体** 是完全从零构建的 Android 输入法平台。布局、行为、主题三层分离，键盘形态可替换，内置终端快捷键。零网络权限，纯命令行构建。

## 为什么写这个

没有一款输入法能在 Termux 里舒服地用 Ctrl+C 中断进程，同时还能在微信里打 T9 中文。灵体让输入法像工具一样可控——布局、行为、主题都可以独立替换，不绑定任何云服务。

## 特性

- 3种布局：九宫格 / 26键 QWERTY / 终端键盘
- 3种行为：T9 中文 / 直接输入 / 真实终端键码
- 场景自动切换：Termux→终端模式，编辑器→26键，其他→用户默认
- 真实终端键：Esc/Tab/Ctrl/方向键/Home/End/PgUp/PgDn/Ctrl+C组合键
- 候选翻页：词频排序+持久化学习
- 五向手势 + 长按气泡 + MultiTap 连击
- 3套主题：默认荧光绿 / Amber 琥珀 / IBM 经典
- 零网络权限，纯命令行构建，5秒编译

## 架构

Touch → GestureRecognizer → KeyboardGestureController → Command → InputEngine → InputConnection

五层分离：core/command/ | core/layout/ | core/candidate/ | core/theme/ | layouts/

## 构建

```bash
bash ~/storage/shared/lingti/build_simple.sh
termux-open ~/storage/shared/lingti/build/simple/unbounded-mvp.apk
```

许可证

GNU General Public License v3.0 — 自由使用、修改、分发，修改后必须同样开源，禁止闭源商用。

```
