# 灵体 (Unbounded) - 输入系统架构

## 核心理念
不是输入法，是**输入平台**。事件驱动、规则驱动、平台无关的人机输入内核。

## 架构总览
cat > ~/storage/shared/lingti/ARCHITECTURE.md << 'EOF'
# 灵体 - 输入系统架构

## 核心理念
不是输入法，是输入平台。事件驱动、规则驱动、平台无关的人机输入内核。

## 架构总览
Event → GestureRecognizer → RuleEngine → List<Command> → PlatformExecutor

## 核心概念
1. Event - 触摸原始坐标
2. Gesture - Tap/SwipeUp/SwipeDown/SwipeLeft/LongPress
3. Rule - Slot + Gesture + Context → List<Command>
4. Command - InputCommand 或 ControlCommand
5. RuntimeSnapshot - KeyboardState + InputState + ExecutionState
6. PlatformExecutor - 封装平台差异

## 设计原则
- 核心模块不知道业务，业务模块不能修改核心
- 一致性 > 灵活性
- 为已知差异抽象，不为假设差异抽象
- 删，不是加

## MVP 路线图
- [x] 第一步：单键输入 a
- [x] 第二步：单键多手势
- [x] 第三步：九宫格硬编码版
- [ ] 第四步：抽出 GestureRecognizer
- [ ] 第五步：抽出 Rule Engine + YAML
- [ ] 第六步：接入 Rime
- [ ] 第七步：Context 自动切换

## 产品目标
学习成本最低，熟练后速度最高的输入系统。
