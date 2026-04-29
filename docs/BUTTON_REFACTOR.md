# 按钮组件重构文档

## 概述

本次重构将所有浅色模式按钮更新为 iOS 26 风格的液态玻璃效果（Liquid Glass），实现更现代、更精致的视觉体验。

## 设计规范

### 尺寸规格

| 类型 | 宽度 | 高度 | 圆角 |
|------|------|------|------|
| 文字按钮 | 85dp | 48dp | 1000dp（胶囊形） |
| 图标按钮 | 48dp | 48dp | 圆形 |
| 描边按钮 | 自适应 | 48dp | 1000dp（胶囊形） |

### 颜色规范

#### 浅色模式
| 属性 | 值 | 说明 |
|------|-----|------|
| 背景色 | `rgba(255, 255, 255, 0.75)` | 75% 不透明度白色 |
| 色调色 | `#0091FF` | 蓝色渐变叠加 |
| 玻璃效果 | `rgba(0, 0, 0, 0.004)` | 微透明叠加层 |
| 阴影色 | `rgba(0, 0, 0, 0.12)` | 12% 不透明度黑色 |
| 文字色 | `#FFFFFF` | 白色 |

#### 深色模式
| 属性 | 值 | 说明 |
|------|-----|------|
| 背景色 | `rgba(28, 28, 30, 0.75)` | 75% 不透明度深灰 |
| 色调色 | `#0A84FF` | 系统蓝色 |
| 文字色 | `#FFFFFF` | 白色 |

### 视觉效果层级

```
┌─────────────────────────────────┐
│           阴影层 (40dp)          │  ← graphicsLayer.shadowElevation
├─────────────────────────────────┤
│         基础背景层               │  ← backgroundColor (75% 白色)
├─────────────────────────────────┤
│         色调渐变层               │  ← Brush.verticalGradient (蓝色)
├─────────────────────────────────┤
│         玻璃效果层               │  ← GlassEffectOverlay (0.4% 黑色)
└─────────────────────────────────┘
```

## 组件架构

### 核心组件

#### 1. LiquidGlassButton
**位置**: `LiquidGlass.kt:218`

**用途**: 主要操作按钮，带有液态玻璃效果

**参数**:
```kotlin
@Composable
fun LiquidGlassButton(
    text: String,              // 按钮文字
    onClick: () -> Unit,       // 点击回调
    modifier: Modifier = Modifier,
    enabled: Boolean = true,   // 是否启用
    isPrimary: Boolean = true, // 是否主要按钮
    width: Dp = 85.dp          // 按钮宽度
)
```

#### 2. LiquidGlassOutlinedButton
**位置**: `LiquidGlass.kt:299`

**用途**: 次要操作按钮，带描边效果

**参数**:
```kotlin
@Composable
fun LiquidGlassOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false,  // 是否为危险操作（红色）
    width: Dp = 85.dp
)
```

#### 3. GlassIconButton
**位置**: `LiquidGlass.kt:857`

**用途**: 图标按钮，48x48dp 圆形

**参数**:
```kotlin
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit  // 图标内容
)
```

#### 4. PrimaryButton
**位置**: `Components.kt:251`

**用途**: 项目标准主要按钮，封装 LiquidGlass 效果

**参数**:
```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false  // 是否显示加载状态
)
```

#### 5. SecondaryButton
**位置**: `Components.kt:338`

**用途**: 项目标准次要按钮，带描边

**参数**:
```kotlin
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

#### 6. TextButton
**位置**: `Components.kt:412`

**用途**: 文字按钮，无背景

**参数**:
```kotlin
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

#### 7. QuickActionButton
**位置**: `LiquidGlass.kt:487`

**用途**: 快捷操作按钮，带图标和文字

**参数**:
```kotlin
@Composable
fun QuickActionButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

### 私有组件

以下组件仅在特定文件中使用：

| 组件名 | 文件 | 用途 |
|--------|------|------|
| `QuickSelectButton` | WeekPickerBottomSheet.kt:191 | 周数快速选择 |
| `LiquidGlassTextButton` | DisplaySettingsScreen.kt:328 | 显示设置文字按钮 |
| `LiquidGlassButton` | ScheduleSettingsScreen.kt:396 | 课程设置按钮 |
| `LiquidGlassOutlinedButton` | DataManagementScreen.kt:487 | 数据管理描边按钮 |

## 设计令牌 (Design Tokens)

### 新增尺寸定义

**文件**: `DesignTokens.kt`

```kotlin
object ComponentSize {
    // ...existing...
    
    object LiquidGlassButton {
        val TextButtonWidth = 85.dp
        val TextButtonHeight = 48.dp
        val IconButtonSize = 48.dp
        val HorizontalPadding = 20.dp
        val VerticalPadding = 6.dp
        val ContentGap = 4.dp
    }
}
```

### 新增颜色定义

**文件**: `DesignTokens.kt`

```kotlin
object LiquidGlassColors {
    // ...existing...
    
    object Button {
        val TintColorLight = Color(0xFF0091FF)
        val TintColorDark = Color(0xFF0A84FF)
        val BackgroundLight = Color(0xBFFFFFFF)  // 75% 白色
        val BackgroundDark = Color(0xBF1C1C1E)
        val GlassEffectOverlay = Color(0x01000000)  // 0.4% 黑色
        val ShadowColor = Color(0x1F000000)  // 12% 黑色
        val TextColorLight = Color.White
        val TextColorDark = Color.White
    }
}
```

### 新增圆角定义

**文件**: `DesignTokens.kt`

```kotlin
object BorderRadius {
    // ...existing...
    val pill = 1000.dp  // 胶囊形圆角
}
```

## 动画效果

### 按压动画

| 组件类型 | 缩放比例 | 动画时长 |
|----------|----------|----------|
| 文字按钮 | 0.96 | 100ms |
| 图标按钮 | 0.92 | 100ms |
| 描边按钮 | 0.96 | 100ms |

**实现代码**:
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.96f else 1f,
    animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
    label = "button_scale"
)
```

## 文件更改清单

### 主题文件

| 文件 | 更改类型 | 说明 |
|------|----------|------|
| DesignTokens.kt | 新增 | 添加按钮尺寸、颜色、圆角定义 |
| LiquidGlass.kt | 重构 | 重构所有液态玻璃按钮组件 |
| Components.kt | 重构 | 重构标准按钮组件 |

### 界面文件

| 文件 | 更改类型 | 更改数量 |
|------|----------|----------|
| WeekPickerBottomSheet.kt | 重构 | 2 处 |
| PeriodPickerBottomSheet.kt | 重构 | 2 处 |
| CoursePreviewBottomSheet.kt | 重构 | 2 处 |
| DisplaySettingsScreen.kt | 重构 | 1 处 |
| ScheduleSettingsScreen.kt | 重构 | 1 处 |
| DataManagementScreen.kt | 重构 | 1 处 |
| NotificationSettingsScreen.kt | 重构 | 5 处 |
| FreeTimeSection.kt | 重构 | 1 处 |
| ImportPreviewScreen.kt | 重构 | 4 处 |
| PeriodTimesSettingsScreen.kt | 重构 | 1 处 |

## 使用指南

### 标准用法

```kotlin
// 主要操作按钮
LiquidGlassButton(
    text = "确定",
    onClick = { /* 操作 */ }
)

// 次要操作按钮
LiquidGlassOutlinedButton(
    text = "取消",
    onClick = { /* 操作 */ }
)

// 危险操作按钮
LiquidGlassOutlinedButton(
    text = "删除",
    onClick = { /* 操作 */ },
    isDestructive = true
)

// 图标按钮
GlassIconButton(
    onClick = { /* 操作 */ }
) {
    Icon(Icons.Default.Add, contentDescription = "添加")
}

// 全宽按钮
LiquidGlassButton(
    text = "保存",
    onClick = { /* 操作 */ },
    modifier = Modifier.fillMaxWidth()
)
```

### 自定义宽度

```kotlin
LiquidGlassButton(
    text = "自定义",
    onClick = { /* 操作 */ },
    width = 120.dp
)
```

### 禁用状态

```kotlin
LiquidGlassButton(
    text = "提交",
    onClick = { /* 操作 */ },
    enabled = false  // 自动降低透明度
)
```

## 兼容性说明

### 保留的 Material 3 按钮

以下场景保留使用 Material 3 原生按钮：

1. **RadioButton** - 单选按钮，保持系统原生样式
2. **IconButton** - 顶部导航栏返回按钮、工具栏按钮
3. **TextButton** - 对话框内的取消/确认按钮（保持简洁风格）

### 迁移注意事项

1. 所有 `Button` 调用应替换为 `LiquidGlassButton`
2. 所有 `OutlinedButton` 调用应替换为 `LiquidGlassOutlinedButton`
3. 图标按钮优先使用 `GlassIconButton`
4. 对话框内按钮可保留 `TextButton` 以保持简洁

## 测试清单

- [ ] 浅色模式下按钮显示正常
- [ ] 深色模式下按钮显示正常
- [ ] 按压动画效果正常
- [ ] 禁用状态显示正确
- [ ] 危险操作按钮颜色正确
- [ ] 全宽按钮布局正确
- [ ] 图标按钮居中显示
- [ ] 阴影效果正常渲染

## 版本历史

| 版本 | 日期 | 更改内容 |
|------|------|----------|
| 1.0 | 2026-02-27 | 初始版本，完成所有按钮组件重构 |

## 参考资源

- [iOS 26 Human Interface Guidelines - Buttons](https://developer.apple.com/design/human-interface-guidelines/buttons)
- [Material Design 3 - Buttons](https://m3.material.io/components/buttons)
- Figma 设计稿：浅色按钮设计规范
