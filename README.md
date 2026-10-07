# TENG计算器 (TENG Calculator)

<p align="center">
  <b>一款运行在 Wear OS 智能手表上的科学计算器</b><br/>
  单应用独立运行 · 纯 Kotlin + Jetpack Compose 打造
</p>

---

## 简介

**TENG计算器** 是一个面向 Wear OS（Android 手表）的独立计算器应用。它不仅提供基础与科学计算，还内置了方程/不等式求解、函数绘图、微积分、回归分析、进制/单位/科学计数法转换、化学工具（元素周期表、化合价、方程式配平）、笔记与公式模板等一整套数学与理科工具，专为小屏圆形/方形手表的交互而设计。

应用通过上下左右滑动在「计算器 ↔ 扩展功能 ↔ 设置」之间切换，所有功能均在手表本地运行，无需联网。

## 功能特性

- **基础 / 科学计算**：四则运算、括号、三角函数、对数、幂、开方、阶乘等
- **历史记录**：自动保存计算会话，可回插结果或表达式
- **笔记**：在手表上随手记录
- **公式模板**：自定义常用公式，支持变量代入与排序
- **函数绘图**：输入表达式绘制函数图像，可调 X 轴范围
- **方程求解**：一元方程求解
- **方程组**：多元线性方程组求解
- **不等式 / 不等式组**：一元与多元不等式求解
- **单调性分析**：判断函数单调区间
- **导数 / 积分**：符号与数值微积分
- **回归分析**：数据拟合
- **科学计数法**：普通数字与科学计数法互转
- **数字转中文**：数字与中文读数互转
- **进制转换**：二进制 / 八进制 / 十进制 / 十六进制互转
- **单位换算**：常见物理量单位转换
- **化学工具**：元素周期表、化合价计算、化学方程式配平
- **个性化设置**：显示缩放、震动反馈、滑动退出开关、扩展功能排序

## 技术栈

| 类别 | 说明 |
| --- | --- |
| 语言 | Kotlin 2.0.21 |
| UI | Jetpack Compose（Wear OS Compose Material 3 1.5.0） |
| 架构 | 单 Activity + ViewModel + Repository |
| 构建 | Gradle Kotlin DSL，AGP 8.6.1 |
| 最低/目标 SDK | minSdk 25 / targetSdk 35（compileSdk 35） |
| 形态 | 独立 Wear OS 应用（`wearable.standalone = true`） |

## 项目结构

```
app/src/main/java/com/tengwear/jisuanqi/
├── MainActivity.kt          # 入口 Activity，负责屏幕路由与转场动画
├── ui/                      # 所有 Compose 界面（各 Screen）
│   ├── theme/               # 颜色、字体、主题
│   ├── CalculatorScreen.kt  # 主计算器
│   ├── ScientificCalcScreen.kt
│   ├── FunctionPlotScreen.kt
│   └── ...（方程 / 不等式 / 导数 / 积分 / 化学 / 笔记 / 模板 等）
├── logic/                   # 计算与求解逻辑（纯 Kotlin，无 Android 依赖）
│   ├── ExpressionEvaluator.kt
│   ├── EquationSolver.kt / SystemSolver.kt
│   ├── DerivativeSolver.kt / IntegralSolver.kt
│   ├── PeriodicElements.kt / ValenceCalculator.kt / ChemistrySolver.kt
│   └── ...
└── data/                    # 数据层（Repository、笔记、模板、设置偏好）
```

计算与求解逻辑（`logic/`）刻意与 Android 框架解耦，便于单元测试与复用。

## 构建与运行

### 环境要求

- **Android SDK**（compileSdk 35，可在 Android Studio 的 SDK Manager 中安装）
- **JDK 17**
- 推荐 **Android Studio**（Hedgehog 或更新版本）

### 步骤

1. 克隆仓库：

   ```bash
   git clone https://github.com/<你的用户名>/jisuanqi2.git
   cd jisuanqi2
   ```

2. 配置本地 SDK 路径。在项目根目录创建 `local.properties`（已被 `.gitignore` 忽略，不会提交）：

   ```properties
   sdk.dir=/你的/Android/Sdk/路径
   ```

3. 用 Android Studio 打开项目，连接 Wear OS 手表或创建手表模拟器（Wear OS 模拟器需单独在 AVD Manager 中下载系统镜像）。

4. 运行 / 打包：

   ```bash
   # 调试构建
   ./gradlew assembleDebug

   # 正式构建（如需发布，请自行配置签名，见下方说明）
   ./gradlew assembleRelease
   ```

> ⚠️ **签名说明**：`assembleRelease` 需要配置签名密钥。本项目未内置任何密钥，请参考 [Android 官方文档](https://developer.android.com/studio/publish/app-signing) 在 `app/build.gradle.kts` 的 `release` 闭包中配置你的 keystore。`.gitignore` 已忽略 `*.keystore` / `*.jks`，切勿将密钥提交到仓库。

## 开源协议

本项目基于 **GNU General Public License v3.0 (GPL-3.0)** 开源，详见 [LICENSE](LICENSE) 文件。

GPL-3.0 是一款强 copyleft 协议：任何分发（含修改后分发）本项目软件的行为，都必须以相同协议开源其完整对应源代码。如需将本项目的衍生作品用于闭源分发，请先获得授权。

## 贡献

欢迎通过 Issue 反馈问题，或通过 Pull Request 提交改进。提交前请确认：

- 代码通过编译（`assembleDebug` 成功）
- 新增功能尽量附带说明或测试

## 免责声明

本应用的计算结果仅供学习与日常参考，不保证在所有边界情形下完全精确，关键场景请以专业工具核实。
