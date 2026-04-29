# 产品变更记录

本文档记录产品需求文档的所有变更历史，遵循语义化版本规范。

---

## [2.88.0] - 2026-03-17

### 变更类型：功能优化

### 状态：已实现

### 变更内容
- 功能 91：课表页面标题行布局优化

### 功能详情

#### 功能 91：课表页面标题行布局优化

**需求描述**：
优化"我的课表"和"Ta的课表"页面的标题行布局，将周次选择器移至与标题同行显示，移除日期范围显示，使界面更加紧凑简洁。

**变更内容**：
1. 将周次选择器从独立行移至标题行，与标题文字同行显示
2. 移除日期范围显示（如"3/3 - 3/9"）
3. 删除不再使用的 `WeekSelectorSection` 组件

**修改文件**：
- `app/src/main/java/com/duoschedule/ui/schedule/ScheduleScreen.kt`
- `Product-Spec.md`

---

## [2.87.0] - 2026-03-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容
- 功能 91：主页今日课程列表底部被底栏遮挡

### 功能详情

#### 功能 91：主页今日课程列表底部被底栏遮挡

**问题描述**：
主页今日课程很多时，最后一个课程会被底部导航栏遮挡，用户无法看到完整内容。

**问题原因**：
`MainScreen.kt` 中的 `Column` 使用了 `verticalScroll` 但没有为底部导航栏预留空间。底栏高度为 64dp 加上系统导航栏高度，内容滚动到底部时会被遮挡。

**解决方案**：
在 `MainScreen.kt` 的滚动 `Column` 中添加 `padding(bottom = 80.dp)`，为底栏预留足够空间，确保最后一个课程可以完整显示。

**用户要求**：
用户明确要求"不要垫高"，因此采用滚动内容底部内边距的方式，而非增加固定占位元素。

**修改文件**：
- `app/src/main/java/com/duoschedule/ui/main/MainScreen.kt`

---

## [2.86.0] - 2026-03-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容
- 功能 90：液态玻璃高光效果毛刺修复

### 功能详情

#### 功能 90：液态玻璃高光效果毛刺修复

**问题描述**：
液态玻璃组件（按钮、开关、底部导航栏等）的高光边缘存在明显毛刺，不够平滑，影响整体视觉质感。

**问题原因**：
1. `blurRadius` 参数过小（默认 `width / 2f` ≈ 0.25dp），模糊半径太小无法有效平滑边缘
2. `falloff` 参数为 1f，衰减较快，导致高光边缘过渡不够柔和

**解决方案**：
1. 将 `blurRadius` 从 `width / 2f` 改为 `width * 1.5f`（约 0.75dp）
2. 将 `falloff` 从 `1f` 改为 `0.6f`，降低衰减速度使边缘更柔和

**修改文件**：
- `AndroidLiquidGlass-kmp/backdrop/src/commonMain/kotlin/com/kyant/backdrop/highlight/Highlight.kt`
- `AndroidLiquidGlass-kmp/backdrop/src/commonMain/kotlin/com/kyant/backdrop/highlight/HighlightStyle.kt`

---

## [2.85.0] - 2026-03-17

### 变更类型：新增功能

### 状态：待实现

### 新增功能
- 功能 89：双人课表分享与导入优化

### 功能详情

#### 功能 89：双人课表分享与导入优化

**变更原因**：
用户反馈导出双人课表分享给对方后，对方导入时可能把"我"和"Ta"的课表搞反。

**核心问题**：
- 导出文件中用"我"/"Ta"标识，对方无法识别是谁的课表
- 导入时没有明确的身份确认机制
- 容易把两份课表导入反了

**解决方案**：

**1. 导出优化**
- 导出文件中使用真实姓名（如"张三"、"李四"），而非"我"/"Ta"
- 分别保存两个人的课表设置（开学时间、总周数等），因为两人可能在不同学校
- 导出文件名包含两个人的姓名：`duoschedule_export_双人_张三李四_日期时间.csv`

**2. 导入优化**
- 显示身份识别预览界面，用颜色区分两份课表（蓝色/黄色）
- 每份课表显示：人员姓名、课程数量、第一节课程名称
- 用户手动选择哪份课表分配给"我"或"Ta"
- 两份课表必须分配给不同的人
- 分配完成后显示确认对话框，用户确认后才执行导入

**3. 冲突处理**
- 如果目标设备已有课表数据，让用户选择覆盖还是合并
- 合并模式下检测时间冲突，冲突课程以红色高亮显示

**4. 设置导入选项**
- 开关：同时导入课表设置（默认开启）
- 如果两份设置不同，让用户选择使用哪套设置

**影响文件**：
- `CsvExporter.kt` - 导出逻辑修改
- `CsvImporter.kt` - 导入逻辑修改
- `ImportPreviewScreen.kt` - 新增身份确认界面
- 导出文件格式变更

---

## [2.84.0] - 2026-03-17

### 变更类型：Bug 修复

### 状态：已实现

### 修复内容
- 功能 89：深色模式底栏图标颜色修复
- 功能 90：底栏宽度优化

### 功能详情

#### 功能 89：深色模式底栏图标颜色修复

**问题描述**：
深色模式下底部导航栏图标看不清。

**问题原因**：
- 教程项目中使用 `ColorFilter.tint(contentColor)` 明确设置图标颜色（深色模式用白色）
- 原代码没有传递正确的图标颜色，导致图标使用默认颜色在深色背景下不可见

**解决方案**：
1. 在 `LiquidBottomTabsSpec` 中添加 `ContentColorLight` 和 `ContentColorDark` 颜色定义
2. 创建 `LocalLiquidBottomTabContentColor` CompositionLocal 来传递内容颜色
3. 在 `LiquidBottomTabs` 组件中根据主题设置正确的 `contentColor`
4. 在 `MainActivity` 中使用 `LocalLiquidBottomTabContentColor.current` 获取颜色并应用到 Icon 和 Text

**修改文件**：
- `app/src/main/java/com/duoschedule/ui/theme/LiquidBottomTabs.kt`
- `app/src/main/java/com/duoschedule/ui/theme/LiquidBottomTab.kt`
- `app/src/main/java/com/duoschedule/MainActivity.kt`

#### 功能 90：底栏宽度优化

**问题描述**：
底部导航栏太宽，视觉效果不佳。

**解决方案**：
参考教程项目 `BottomTabsContent.kt` 中的实现，添加水平 padding：
```kotlin
Modifier.padding(horizontal = 36.dp)
```

---

## [2.83.0] - 2026-03-16

### 变更类型：功能优化

### 状态：已实现

### 新增功能
- 功能 88：底部导航栏液态玻璃效果重构

### 功能详情

#### 功能 88：底部导航栏液态玻璃效果重构

**变更原因**：
参考 AndroidLiquidGlass-master 和 AndroidLiquidGlass-kmp 项目，重构底部导航栏实现液态玻璃效果。

**参考项目**：
- `d:\双人课程表\AndroidLiquidGlass-master` - Android 原生版本
- `d:\双人课程表\AndroidLiquidGlass-kmp` - Kotlin Multiplatform 版本

**优化内容**：

**1. 选中指示器玻璃效果触发时机（与参考项目一致）**
- 未点击/未按压状态：选中指示器无玻璃效果，只显示基础背景色
- 点击/按压状态：选中指示器显示完整玻璃效果（vibrancy + blur + lens + chromaticAberration）
- 切换过程中：选中指示器保持玻璃效果
- 切换完成后：玻璃效果消失，恢复普通状态

**2. 拖拽切换功能**
- 支持拖拽选中指示器来切换 Tab
- 使用 `DampedDragAnimation` 实现阻尼动画效果
- 快速滑动时有弹性变形效果

**3. 按压动画效果（与参考项目一致）**
- 容器缩放动画：按压时缩小到 0.85 倍
- 选中指示器缩放动画：按压时放大到 1.39 倍
- 速度弹性动画：快速滑动时指示器有弹性变形

**4. 交互高亮效果**
- 按压位置显示渐变高亮光斑
- 光斑跟随手指移动
- 使用 `InteractiveHighlight` 组件实现

**技术实现**：
- 新增 `LiquidBottomTabs.kt` - 主组件
- 新增 `LiquidBottomTab.kt` - 单个 Tab 项组件
- 新增 `DampedDragAnimation.kt` - 阻尼拖拽动画工具类
- 新增 `InteractiveHighlight.kt` - 交互高亮工具类
- 重构现有 `glassbottombar.kt`

---

## [2.82.0] - 2026-03-11

### 变更类型：功能优化

### 状态：已实现

### 新增功能
- 功能 84：底部导航栏指示器样式和动画优化

### 功能详情

#### 功能 84：底部导航栏指示器样式和动画优化

**变更原因**：
参考 Pronto 应用的底部导航栏指示器设计，重构双人课程表底栏。

**优化内容**：

**1. 样式重构（与 Pronto 一致）**
- 移除独立的指示器滑块
- 选中项自身显示 Capsule（胶囊）背景
- 背景使用主题色（iOS 蓝色）
- 毛玻璃效果（blur + lens）

**2. 动画效果（与 Pronto 一致）**
- 动画时长：400ms
- 缓动曲线：CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
- 选中项 scale 动画（按压时缩小到 0.85）

**技术实现**：
- 重构 `GlassBottomBar.kt`
- 移除独立的指示器滑块逻辑
- 在 `GlassBottomBarItem` 中添加选中状态背景
- 使用 Capsule 形状作为选中背景
- 修改 MainActivity.kt 中的调用方式

---

## [2.82.0] - 2026-03-11

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 87：当前周次不会自动切换

### 功能详情

#### 功能 87：当前周次不会自动切换

**变更原因**：
用户反馈"我的课表界面和ta的课表界面和当前周次不会自动切换"，以及"设置里的也不会变"。

**问题描述**：
- 课表页面显示的周次不会根据当前日期自动更新
- 设置页面修改开学时间后，当前周次不会自动重新计算
- 用户需要手动修改当前周次，影响使用体验

**问题原因分析**：
- `calculateCurrentWeek()` 方法已存在，但从未被调用
- `currentWeek` 值存储在 DataStore 中，只在用户手动设置时更新
- 应用启动和进入前台时，没有自动计算并更新当前周次
- 设置页面修改开学时间或学期总周数时，没有触发当前周次重新计算

**解决方案**：

**1. 新增 `updateCurrentWeekIfNeeded()` 方法**
- 读取两人各自的开学时间和学期总周数
- 使用 `calculateCurrentWeek()` 计算当前应该是第几周
- 如果计算值与存储值不同，自动更新存储值

**2. 应用启动时自动更新**
- 在 `DuoScheduleApp.onCreate()` 中调用 `updateCurrentWeekIfNeeded()`
- 确保应用启动时周次是最新的

**3. 应用进入前台时自动更新**
- 在 `AppLifecycleObserver.onStart()` 中调用 `updateCurrentWeekIfNeeded()`
- 确保用户从后台切回应用时周次是最新的

**4. 设置页面修改时自动更新**
- 修改开学时间时，自动重新计算当前周次
- 修改学期总周数时，自动重新计算当前周次

**技术实现**：
- `DuoScheduleApp.kt` - 新增 `updateCurrentWeekIfNeeded()` 方法
- `DuoScheduleApp.kt` - 在应用启动和进入前台时调用该方法
- `SettingsViewModel.kt` - 修改 `setPersonSemesterStart()` 和 `setPersonTotalWeeks()` 方法，在设置后自动计算当前周次

**影响范围**：
- `DuoScheduleApp.kt` - 新增自动更新周次逻辑
- `SettingsViewModel.kt` - 设置页面修改时自动更新周次

---

## [2.81.0] - 2026-03-09

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 86：主页今日课程最后一个卡片阴影被截断

### 功能详情

#### 功能 86：主页今日课程最后一个卡片阴影被截断

**变更原因**：
用户反馈"主页-今日课程最后一个卡片下面好像被截断了一样，突然阴影就没了"。

**问题描述**：
- 主页今日课程列表中，最后一个卡片的毛玻璃效果（阴影/折射）在底部被截断
- 视觉上看起来像是卡片突然消失或被切断

**问题原因分析**：
- `ScheduleList` 组件设置了 `heightIn(max = 400.dp)` 并使用 `verticalScroll`
- 外层 `MainScreen` 也有 `verticalScroll`，造成嵌套滚动
- 内部滚动容器有高度限制，导致内容被裁剪
- 卡片使用 `drawBackdrop` + `lens` 毛玻璃效果，边缘视觉效果被容器边界裁剪

**解决方案**：
- 移除 `ScheduleList` 内部的 `heightIn(max = 400.dp)` 和 `verticalScroll`
- 让整个页面统一滚动，避免嵌套滚动问题
- 卡片可以完整显示到底部，不再被截断

**修改文件**：
- `TodayScheduleTimeline.kt`：移除内部高度限制和滚动，简化为普通 Column

---

## [2.80.0] - 2026-03-09

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 85：后台通知优化

### 功能详情

#### 功能 85：后台通知优化

**变更原因**：
用户反馈"app在后台，没有清理通知也不及时，只有切换到前台才有通知"，应用在后台时通知无法正常触发。

**问题描述**：
- 应用在后台时，课前通知和上课中通知无法正常触发
- 必须切换到前台才能看到通知，影响用户体验
- Android 后台执行限制导致 BroadcastReceiver 在后台可能无法触发
- AlarmManager 在 Doze 模式下会被延迟

**问题原因分析**：

**1. Android 后台执行限制**
- Android 8.0+ 对后台执行有严格限制
- BroadcastReceiver 在后台可能无法触发
- AlarmManager 在 Doze 模式下会被延迟

**2. 通知调度时机问题**
- `scheduleReminderNotifications()` 只在应用前台时被调用
- 后台时没有重新调度机制
- 闹钟可能在应用进入后台后失效

**3. 前台服务启动问题**
- `LiveUpdateService` 依赖 `OngoingCourseReceiver` 触发
- 该 Receiver 在后台可能无法正常工作
- 导致上课中通知无法显示

**解决方案**：

**1. 使用 WorkManager 定期重新调度**
- 新增 `NotificationRescheduleWorker` 类
- 每 15 分钟自动重新调度所有通知
- 使用 PeriodicWorkRequest 确保在后台也能执行
- 不受 Doze 模式影响

**2. 添加应用生命周期监听**
- 监听应用进入后台和前台事件
- 进入前台时立即重新调度通知
- 进入后台时确保后台任务已正确设置

**3. 优化前台服务启动**
- 在 `OngoingCourseReceiver` 中直接启动前台服务
- 使用 `startForegroundService()` 确保服务在后台也能运行
- 添加 `android:stopWithTask="false"` 确保服务不会被意外终止

**技术实现**：
- 新增 `NotificationRescheduleWorker.kt` - 定期重新调度通知的 Worker
- 修改 `DuoScheduleApp.kt` - 添加生命周期监听和定期任务启动
- 修改 `OngoingCourseReceiver.kt` - 确保前台服务正确启动
- 修改 `AndroidManifest.xml` - 添加 `android:stopWithTask="false"` 属性

**影响范围**：
- 新增 `NotificationRescheduleWorker.kt` - 定期重新调度通知
- `DuoScheduleApp.kt` - 添加生命周期监听
- `OngoingCourseReceiver.kt` - 优化前台服务启动
- `AndroidManifest.xml` - 优化服务配置

---

## [2.79.0] - 2026-03-08

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 84：上课自动静音恢复修复

### 功能详情

#### 功能 84：上课自动静音恢复修复

**变更原因**：
用户反馈"课程结束，没有取消自动静音"，课程结束后手机仍保持静音/振动状态。

**问题描述**：
- 课程结束后手机仍保持静音/振动状态，没有恢复到原有铃声模式
- 静音状态跟踪不完整，只保存了"原始铃声模式"，没有保存"当前是否处于自动静音状态"
- 应用重启后状态丢失，闹钟可能被系统延迟或取消

**解决方案**：

**1. 增强静音状态持久化**
- 新增 `KEY_IS_AUTO_SILENT_ACTIVE`：记录当前是否处于自动静音状态
- 新增 `KEY_AUTO_SILENT_END_TIME`：记录静音应该结束的时间戳
- 新增 `KEY_AUTO_SILENT_COURSE_ID`：记录当前静音对应的课程 ID

**2. 应用启动时检查并恢复**
- 在 `DuoScheduleApp.onCreate()` 中添加静音状态检查
- 如果检测到静音已过期（当前时间 > 结束时间），立即恢复铃声
- 应用进入前台时也执行检查

**3. 增强闹钟调度**
- 在调度静音开始闹钟时同时传递结束时间戳
- 设置静音时记录状态和结束时间
- 恢复铃声时清除状态

**技术实现**：
- `RingerModeManager.kt` - 新增 `setAutoSilentActive()`、`isAutoSilentActive()`、`getAutoSilentEndTime()`、`clearAutoSilentState()` 方法
- `SilentModeReceiver.kt` - 设置静音时记录状态，恢复铃声时清除状态
- `CourseNotificationManager.kt` - 调度静音闹钟时传递结束时间
- `DuoScheduleApp.kt` - 添加 `checkAndRestoreRingerMode()` 方法

**影响范围**：
- `RingerModeManager.kt` - 增强状态持久化
- `SilentModeReceiver.kt` - 增强状态管理
- `CourseNotificationManager.kt` - 增强调度逻辑
- `DuoScheduleApp.kt` - 添加启动检查

---

## [2.78.0] - 2026-03-08

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 83：首页今日课程跨天更新修复

### 功能详情

#### 功能 83：首页今日课程跨天更新修复

**变更原因**：
用户反馈"上课前看还是昨天的课"，首页今日课程区域在跨天后仍显示昨天的课程数据。

**问题描述**：
- `currentDayOfWeek`（当前星期几）在 ViewModel 初始化时设置，但跨天后不会自动更新
- `MainScreen` 中的定时任务只更新小时和分钟，没有检查日期变化
- 导致跨天后课程列表仍显示昨天的课程

**解决方案**：

**1. 添加日期变化检测**
- 在 `MainViewModel` 中添加 `lastDate` 变量记录上次日期
- 在 `updateTime()` 方法中检测日期变化

**2. 自动刷新星期数据**
- 当检测到日期变化时，调用 `refreshCurrentDay()` 更新 `currentDayOfWeek`
- 触发 `todayCourses` Flow 重新查询当天课程

**技术实现**：
```kotlin
// MainViewModel.kt
private var lastDate: LocalDate = LocalDate.now()

fun updateTime() {
    val now = LocalTime.now()
    _currentHour.value = now.hour
    _currentMinute.value = now.minute
    
    val today = LocalDate.now()
    if (today != lastDate) {
        lastDate = today
        refreshCurrentDay()
    }
}
```

**影响文件**：
- MainViewModel.kt - 添加日期变化检测和自动刷新逻辑

---

## [2.77.0] - 2026-03-03

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 82：课表课程卡片内边距优化

### 功能详情

#### 功能 82：课表课程卡片内边距优化

**变更原因**：
用户反馈课表界面课程名称和背景格子左右和上下距离过大，导致内容显示不够紧凑，空间利用率不高。

**解决方案**：

**1. 内边距调整**
- 原内边距：水平 4dp，垂直 3dp
- 新内边距：水平 2dp，垂直 2dp
- 大幅减少课程名称/地点与卡片边缘的距离

**2. 视觉效果**
- 课程卡片内容更紧凑
- 文字与背景色块边缘距离更合理
- 课程名称和地点能利用更多卡片空间

**技术实现**：
- 修改 `ScheduleScreen.kt` 中 `CourseOverlayCard` 组件的 padding 参数
- 从 `padding(horizontal = 4.dp, vertical = 3.dp)` 改为 `padding(horizontal = 2.dp, vertical = 2.dp)`

---

## [2.76.0] - 2026-03-03

### 变更类型：新增功能

### 状态：已实现

### 新增功能
- 功能 81：课程字体大小自定义设置

### 功能详情

#### 功能 81：课程字体大小自定义设置

**变更原因**：
用户反馈课表中课程名称和地点的字体大小不够灵活，部分用户希望字体更大以便于阅读，部分用户希望字体更小以显示更多内容。

**解决方案**：

**1. 课程名称字号设置**
- 设置位置：设置 → 显示设置 → 课表字体
- 可选字号：10sp、11sp、12sp（默认）、13sp、14sp、15sp、16sp
- 影响范围：课表格子中的课程名称文字

**2. 上课地点字号设置**
- 设置位置：设置 → 显示设置 → 课表字体
- 可选字号：9sp、10sp、11sp（默认）、12sp、13sp、14sp
- 影响范围：课表格子中的上课地点文字

**技术实现**：
- SettingsDataStore.kt：新增 `courseNameFontSize` 和 `courseLocationFontSize` 配置项
- CourseRepository.kt：新增对应的 Flow 和设置方法
- SettingsViewModel.kt：新增字体大小 StateFlow 和设置方法
- ScheduleViewModel.kt：新增字体大小 StateFlow
- DisplaySettingsScreen.kt：新增字体大小设置 UI
- ScheduleScreen.kt：CourseOverlayCard 使用自定义字体大小

---

## [2.73.0] - 2026-03-02

### 变更类型：重构

### 状态：待开发

### 重构功能
- 功能 5：桌面小组件 - 全面重构

### 功能详情

#### 功能 5：桌面小组件（重构）

**变更原因**：
用户反馈现有小组件存在多个问题：视觉样式不好看、刷新/性能问题、负一屏找不到组件、功能不够用。需要全面重构小组件功能。

**问题描述**：
- 视觉样式不符合用户期望
- 刷新机制不够智能
- 在小米负一屏（桌面助手）中找不到小组件
- 只有一种尺寸和显示内容，功能单一

**解决方案**：

**1. 多种尺寸规格**

| 尺寸 | 名称 | 显示内容 |
|------|------|----------|
| 2×2 | 空闲时间小组件 | 显示两人当前共同的空闲时间 |
| 4×2 | 我的今日课程小组件 | 仅显示"我"的今日课程列表 |
| 4×2 | Ta的今日课程小组件 | 仅显示"Ta"的今日课程列表 |

**2. 个人独立小组件**
- 为两个人分别创建独立的今日课程小组件
- "我的今日课程小组件"：显示"我"（PersonB）的课程，使用黄色主题色
- "Ta的今日课程小组件"：显示"Ta"（PersonA）的课程， 使用蓝色主题色
- 点击各自小组件跳转到对应的课表页面

**3. iOS 风格设计**
- 采用 iOS 风格视觉设计
- 圆角卡片样式（16dp 圆角）
- 浅色/深色模式自适应
- 毛玻璃背景效果

**4. 智能刷新机制**
- 课程状态变化时自动刷新
- 屏幕亮起时刷新
- 最小刷新间隔：1分钟

**5. MIUI/HyperOS负一屏支持**
- 确保在负一屏小组件列表中可见所有小组件
- 适配 MIUI 小组件规范

**技术实现要点**：
- 创建多个小组件 Receiver 和配置文件
- 实现智能刷新机制
- 优化负一屏可见性
- iOS 风格 UI 设计

**影响文件**：
- `widget/ScheduleWidgetReceiverMIUI.kt` - 重构
- `widget/FreeTimeWidget.kt` - 新增空闲时间小组件
- `widget/MyCourseWidget.kt` - 新增我的课程小组件
- `widget/TaCourseWidget.kt` - 新增Ta的课程小组件
- `res/xml/widget_info_*.xml` - 多个小组件配置文件
- `res/layout/widget_*.xml` - 多个小组件布局文件
- `AndroidManifest.xml` - 注册多个小组件

---

## [2.72.0] - 2026-03-02

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 5：桌面小组件 - MIUI/HyperOS适配

### 功能详情

#### 功能 5：桌面小组件 MIUI/HyperOS适配（优化）

**变更原因**：
用户反馈在MIUI/HyperOS设备的负一屏（桌面助手）添加小组件时，小组件刷新不及时或无法正常显示，需要适配MIUI/HyperOS专用的小组件规范。

**问题描述**：
- 原有小组件未针对MIUI/HyperOS进行专门适配
- 在小米负一屏添加时刷新机制不完善
- 未使用独立进程运行小组件，可能导致性能问题

**解决方案**：
- 移除标准Android小组件，只保留MIUI专用小组件
- 创建MIUI专用小组件配置文件 `schedule_widget_info_miui.xml`
- 创建MIUI专用小组件Receiver `ScheduleWidgetReceiverMIUI`
- 使用独立进程（:widgetProvider）运行小组件
- 支持MIUI小组件exposure刷新模式
- 同时适配vivo小组件规范

**技术实现**：

**1. MIUI小组件Receiver**
```kotlin
class ScheduleWidgetReceiverMIUI : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MIUI_APPWIDGET_UPDATE,
            ACTION_MIUI_EXPOSURE -> {
                // 处理MIUI专用刷新
            }
        }
    }
}
```

**2. AndroidManifest.xml配置**
```xml
<receiver
    android:name=".widget.ScheduleWidgetReceiverMIUI"
    android:label="@string/widget_name"
    android:exported="true"
    android:process=":widgetProvider">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        <action android:name="miui.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data android:name="miuiWidget" android:value="true" />
    <meta-data android:name="miuiWidgetRefresh" android:value="exposure" />
    <meta-data android:name="miuiWidgetRefreshMinInterval" android:value="10000" />
    <meta-data android:name="vivo_widget" android:value="true" />
    <meta-data android:name="vivoWidgetVersion" android:value="1" />
</receiver>
```

**影响文件**：
- `res/xml/schedule_widget_info_miui.xml` - MIUI小组件配置（唯一）
- `widget/ScheduleWidgetReceiverMIUI.kt` - MIUI小组件Receiver（唯一）
- `AndroidManifest.xml` - 更新小组件声明
- `res/values/strings.xml` - 移除多余字符串
- 删除 `schedule_widget_info.xml` - 不再需要标准配置
- 删除 `ScheduleWidgetReceiver.kt` - 不再需要标准Receiver

---

## [2.71.0] - 2026-03-02

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 79：空闲时段显示优化

### 功能详情

#### 功能 79：空闲时段显示优化（新增）

**变更原因**：
用户反馈主页空闲时段卡片显示的是最晚的时段，而不是离现在最近的时段，不符合用户预期。

**问题描述**：
- 空闲时段卡片右侧显示的是最后一个（最晚的）空闲时段
- 用户期望看到离现在最近的空闲时段，方便安排接下来的活动

**解决方案**：
- 修改 `FreeTimeSection.kt` 中的显示逻辑
- 将 `freeTimeSlots.last()` 改为 `freeTimeSlots.first()`
- 变量名从 `latestSlot` 改为 `nearestSlot`

**技术实现**：
```kotlin
// 修改前
val latestSlot = freeTimeSlots.last()

// 修改后
val nearestSlot = freeTimeSlots.first()
```

**影响文件**：
- `FreeTimeSection.kt` - 修改显示逻辑

---

## [2.70.0] - 2026-03-01

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 77：启动阶段页面切换卡顿优化

### 功能详情

#### 功能 77：启动阶段页面切换卡顿优化（新增）

**变更原因**：
用户反馈应用启动后前几秒钟切换页面时出现明显卡顿和掉帧现象，影响使用体验。

**问题描述**：
- 应用启动后的前几秒钟内，使用底部导航栏切换页面时出现卡顿
- 第一次切换页面必卡，后续切换相对流畅
- 掉帧明显，影响用户体验和应用流畅度感知

**问题原因分析**：

**1. ViewModel 的 Flow 订阅策略问题**
- `ScheduleViewModel` 使用 `SharingStarted.Lazily`，导致首次访问时才初始化
- 首次切换页面时，ViewModel 和 Flow 订阅同时初始化，造成卡顿

**2. Compose 首次组合开销大**
- 页面组件（`MainScreen`、`ScheduleScreen`）结构复杂
- 首次切换到新页面时，Compose 需要进行首次组合（Composition）
- 创建所有 Composable 函数、解析类型、创建布局节点等开销大

**3. 预加载不够充分**
- `ComposeWarmup` 只预热了基础组件，没有预热实际页面组件
- 没有在应用启动时预初始化 ViewModel 和数据

**解决方案**：

**1. 优化 Flow 订阅策略**
- 将 `ScheduleViewModel` 中所有 Flow 的 `SharingStarted.Lazily` 改为 `SharingStarted.Eagerly`
- 确保应用启动时立即订阅所有关键数据流
- 与 `MainViewModel` 保持一致的订阅策略

**2. 增强组件预热**
- 扩展 `ComposeWarmup`，添加实际页面组件的预热（`WarmupScreenComponents`）
- 预热主页卡片列表、课表网格头等复杂组件
- 预热 `LazyColumn` 和课程卡片组件

**3. 增强数据预加载**
- 在 `DuoScheduleApp` 中预加载 Repository 的关键数据
- 预取课程数据、人员名称、周次信息、节次信息等
- 使用协程并发预取所有关键数据

**4. 优化动画配置**
- 减少页面切换动画时长：300ms → 250ms
- 减少底部导航栏动画时长：200ms → 150ms
- 移除底栏动画的延迟，让动画更直接响应

**技术实现**：

**1. ScheduleViewModel.kt**
```kotlin
// 所有 Flow 改为 SharingStarted.Eagerly
val personAName: StateFlow<String> = getCachedFlow("personAName") {
    repository.getPersonAName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Ta")
}

private val personACourses = repository.getCoursesByPerson(PersonType.PERSON_A)
    .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)
```

**2. ComposeWarmup.kt**
```kotlin
// 添加页面组件预热
@Composable
private fun WarmupScreenComponents() {
    // 预热主页卡片列表
    LazyColumn { /* ... */ }
    
    // 预热课表网格头
    Box { /* ... */ }
}

// 优化预热时间
delay(500) // 启动延迟从 2000ms 减少到 500ms
delay(500) // 预热时间从 300ms 增加到 500ms
```

**3. DuoScheduleApp.kt**
```kotlin
// 添加 Repository 注入
@Inject
lateinit var repository: CourseRepository

// 预加载关键数据
kotlinx.coroutines.coroutineScope {
    listOf(
        async { repository.getCoursesByPerson(PersonType.PERSON_A).first() },
        async { repository.getPersonAName().first() },
        // ... 其他数据
    ).awaitAll()
}
```

**4. Navigation.kt**
```kotlin
// 优化动画时长
private val iosTransitionSpec = tween<IntOffset>(durationMillis = 250, easing = FastOutSlowInEasing)
private val iosFadeSpec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing)
```

**5. MainActivity.kt**
```kotlin
// 减少底部导航栏动画时长
val scale by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.9f
        isSelected -> 1.05f
        else -> 1f
    },
    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
    label = "nav_item_scale"
)
```

**预期效果**：
- 应用启动时即开始预加载所有关键数据和组件
- 首次切换页面无卡顿，响应流畅
- 整体切换流畅度显著提升
- 用户体验显著改善，应用感知更流畅

**影响文件**：
- `ScheduleViewModel.kt` - Flow 订阅策略优化
- `ComposeWarmup.kt` - 添加页面组件预热
- `DuoScheduleApp.kt` - 添加数据预加载
- `Navigation.kt` - 页面切换动画优化
- `MainActivity.kt` - 底部导航栏动画优化
- `Product-Spec.md` - 新增功能 77
- `Product-Spec-CHANGELOG.md` - 记录本次优化

**优先级**：高

**状态**：已实现

---

## [2.69.0] - 2026-02-28

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 76：去除开屏页面

### 功能详情

#### 功能 76：去除开屏页面（新增）

**变更原因**：
用户希望应用启动时直接进入主界面，无需等待开屏页面。

**问题描述**：
- 应用启动时显示一个带应用图标的开屏页面（Splash Screen）
- 开屏页面持续 200ms，用户需要等待才能看到内容
- 影响用户体验，增加启动时间感知

**解决方案**：
- 移除 AndroidManifest.xml 中 MainActivity 的 Splash 主题引用
- 移除 MainActivity.kt 中的 `installSplashScreen()` 调用
- 应用直接使用主主题启动，无开屏延迟

**技术实现**：
- 修改 `AndroidManifest.xml`：移除 `android:theme="@style/Theme.DuoSchedule.Splash"` 属性
- 修改 `MainActivity.kt`：移除 `installSplashScreen()` 相关代码

**影响文件**：
- AndroidManifest.xml
- MainActivity.kt
- Product-Spec.md - 新增功能 76

---

## [2.68.0] - 2026-02-28

### 变更类型：新增

### 状态：待开发

### 新增功能
- 功能 75：首次页面切换卡顿优化

### 功能详情

#### 功能 75：首次页面切换卡顿优化（新增）

**变更原因**：
用户反馈应用启动后，无论等待多久，第一次使用底部导航栏切换页面时必然出现卡顿，后续切换则流畅。

**问题描述**：
- 应用启动后，第一次用底栏切换页面必卡
- 后续切换流畅，只有第一次卡顿
- 影响用户体验，给人应用性能差的印象

**问题原因分析**：

**1. Compose 首帧渲染开销**
- 第一次切换到新页面时，Compose 需要进行首次组合（Composition）
- 首次组合需要创建所有 Composable 函数、解析类型、创建布局节点等
- 后续切换可以复用已创建的组件，因此更流畅

**2. ViewModel 首次初始化**
- 每个页面的 ViewModel 在首次访问时才初始化
- ViewModel 初始化涉及依赖注入、数据库查询等
- MainViewModel、ScheduleViewModel 等都有多个 Flow 需要首次订阅

**3. Flow 首次订阅延迟**
- 使用 `SharingStarted.WhileSubscribed(5000)` 策略
- Flow 在首次订阅时才开始收集数据
- 数据库查询、DataStore 读取等都在首次切换时触发

**4. ComposeWarmup 预热不充分**
- 当前预热只覆盖了基础组件
- 没有预热应用特定的组件（如课程卡片、课表格子等）
- 没有预热 ViewModel 和 Flow 订阅

**解决方案**：

**1. 增强预加载策略**
- 在 Application 启动时预初始化所有 ViewModel
- 预订阅关键数据 Flow，使用 `SharingStarted.Lazily` 或 `shareIn`
- 预加载所有页面的初始数据

**2. 优化 Flow 订阅策略**
- 将关键数据的 `SharingStarted.WhileSubscribed(5000)` 改为 `SharingStarted.Lazily`
- 使用 `shareIn` 共享数据流，避免重复订阅
- 在 Application 中预热关键数据

**3. 增强组件预热**
- 扩展 ComposeWarmup，预热应用特定组件
- 预热课程卡片、课表格子、设置项等
- 预热动画组件和过渡效果

**4. 页面预加载**
- 在启动时预加载所有底部导航页面
- 使用 `LaunchedEffect` 在后台预组合页面
- 利用 Compose 的组合缓存机制

**技术实现要点**：
- 修改 `DuoScheduleApp.kt` - 增强预加载逻辑
- 修改 `MainViewModel.kt` - 优化 Flow 订阅策略
- 修改 `ScheduleViewModel.kt` - 优化 Flow 订阅策略
- 修改 `ComposeWarmup.kt` - 增强组件预热
- 修改 `CourseRepository.kt` - 优化数据预取

**影响文件**：
- DuoScheduleApp.kt
- MainViewModel.kt
- ScheduleViewModel.kt
- ComposeWarmup.kt
- CourseRepository.kt
- Product-Spec.md - 新增功能 75

---

## [2.67.0] - 2026-02-28

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 34：课前通知标题优化

### 功能详情

#### 功能 34：课前通知标题优化（优化）

**变更原因**：
用户反馈课前通知的主标题显示"Upcoming Class"不够直观，希望直接显示课程名称。

**变更内容**：

**1. 通知标题变更**
- 原标题：固定显示"Upcoming Class"
- 新标题：显示课程名称（如"高等数学"）

**2. 通知内容变更**
- 原内容：课程名称 · 地点 · 提前分钟数
- 新内容：地点 · 开始时间（如"教学楼A101 · 08:00"）

**3. 展开通知内容**
- 原内容：课程名称、地点、开始时间、提前分钟数分行显示
- 新内容：地点、开始时间、提前分钟数分行显示

**技术实现**：
- 修改 `PromotedNotificationBuilder.kt` 中的 `buildReminderNotification` 方法
- 将 `setContentTitle` 从固定字符串改为 `courseName` 参数
- 将 `setContentText` 从课程名称+地点+时间改为地点+时间

**影响文件**：
- PromotedNotificationBuilder.kt - 通知构建逻辑修改
- Product-Spec.md - 功能 34 业务规则更新

---

## [2.66.0] - 2026-02-28

### 变更类型：修复

### 状态：已实现

### 修复问题
- 功能 74：时间选择器 BottomSheet 手势冲突修复

### 功能详情

#### 功能 74：时间选择器 BottomSheet 手势冲突修复（修复）

**变更原因**：
用户反馈在课表时间设置的 BottomSheet 中滑动选择时间时，很容易误触下滑手势导致页面关闭，影响用户体验。

**问题描述**：
- 在课表时间设置的 BottomSheet 中滑动选择时间时，垂直滑动手势被外层的 ModalBottomSheet 捕获
- 用户在滚轮选择器上滑动选择时间时，很容易误触下滑手势导致 BottomSheet 关闭
- 影响用户体验，需要重新打开 BottomSheet 再次设置

**解决方案**：

**1. GlassBottomSheet 组件增强**
- 添加 `enableDismissOnSwipe` 参数，控制是否允许手势关闭
- 默认值为 `true`，保持向后兼容

**2. 手势关闭禁用实现**
- 当 `enableDismissOnSwipe` 为 false 时，创建一个内部的 SheetState
- 通过 `confirmValueChange` 回调阻止状态变为 `SheetValue.Hidden`
- 用户只能通过点击"取消"按钮关闭 BottomSheet

**3. TimeRangeBottomSheet 适配**
- 设置 `enableDismissOnSwipe = false`
- 只允许通过点击"取消"按钮关闭

**技术实现要点**：
- 修改 `GlassBottomSheet.kt`，添加 `enableDismissOnSwipe` 参数
- 使用 `rememberModalBottomSheetState` 创建内部 SheetState
- 设置 `confirmValueChange = { newValue -> newValue != SheetValue.Hidden }`
- 修改 `TimeRangeBottomSheet.kt`，传入 `enableDismissOnSwipe = false`

**影响文件**：
- GlassBottomSheet.kt - 添加手势关闭控制参数
- TimeRangeBottomSheet.kt - 禁用手势关闭
- Product-Spec.md - 新增功能 74

---

## [2.66.0] - 2026-02-28

### 变更类型：优化

### 状态：已实现

### 性能优化
- 功能 75：首次页面切换卡顿优化

### 功能详情

#### 功能 75：首次页面切换卡顿优化（新增）

**变更原因**：
用户反馈应用启动后，第一次使用底部导航栏切换页面时必然出现卡顿，后续切换则流畅。

**问题描述**：
- 应用启动后，第一次用底栏切换页面必卡
- 后续切换流畅，只有第一次卡顿
- 影响用户体验，给人应用性能差的印象

**问题原因分析**：

1. **Compose 首帧渲染开销**
   - 第一次切换到新页面时，Compose 需要进行首次组合（Composition）
   - 首次组合需要创建所有 Composable 函数、解析类型、创建布局节点等
   - 后续切换可以复用已创建的组件，因此更流畅

2. **ViewModel 首次初始化**
   - 每个页面的 ViewModel 在首次访问时才初始化
   - ViewModel 初始化涉及依赖注入、数据库查询等
   - MainViewModel、ScheduleViewModel 等都有多个 Flow 需要首次订阅

3. **Flow 首次订阅延迟**
   - 使用 `SharingStarted.WhileSubscribed(5000)` 策略
   - Flow 在首次订阅时才开始收集数据
   - 数据库查询、DataStore 读取等都在首次切换时触发

4. **ComposeWarmup 预热不充分**
   - 当前预热只覆盖了基础组件
   - 没有预热应用特定的组件（如课程卡片、课表格子等）
   - 没有预热 ViewModel 和 Flow 订阅

**解决方案**：

1. **优化 Flow 订阅策略**
   - 将所有 `SharingStarted.WhileSubscribed(5000)` 改为 `SharingStarted.Lazily`
   - `Lazily` 策略会在 StateFlow 创建时立即开始收集数据，而不是等到第一个订阅者
   - 这样在应用启动时就开始预加载数据，首次切换时无需等待

2. **增强 ComposeWarmup**
   - 添加 `WarmupScheduleComponents()` 预热课表组件
   - 添加 `WarmupCourseComponents()` 预热课程卡片组件
   - 将预热时间从 150ms 增加到 300ms，确保充分预热

3. **Application 预加载**
   - 在 `DuoScheduleApp.onCreate()` 中添加 `preloadViewModels()`
   - 预加载数据库课程数据
   - 预加载所有设置数据
   - 使用 PerformanceMonitor 追踪预加载性能

4. **优化动画配置**
   - 底栏动画从 250ms 减少到 200ms
   - 底栏动画添加 50ms 延迟，让页面先开始切换
   - 简化底部导航切换动画，移除复杂的方向检测逻辑
   - 使用统一的滑动动画，减少计算开销

**技术实现**：

1. MainViewModel.kt - 所有 Flow 改为 `SharingStarted.Lazily`
2. ScheduleViewModel.kt - 所有 Flow 改为 `SharingStarted.Lazily`
3. ComposeWarmup.kt - 添加课程和课表组件预热
4. DuoScheduleApp.kt - 添加 `preloadViewModels()` 方法
5. MainActivity.kt - 底栏动画优化（200ms + 50ms 延迟）
6. Navigation.kt - 简化页面切换动画

**预期效果**：
- 应用启动时即开始预加载数据和组件
- 首次切换页面无卡顿
- 整体切换流畅度提升
- 用户体验显著改善

**影响文件**：
- MainViewModel.kt - Flow 订阅策略优化
- ScheduleViewModel.kt - Flow 订阅策略优化
- ComposeWarmup.kt - 添加课程和课表组件预热
- DuoScheduleApp.kt - 添加 ViewModel 预加载
- MainActivity.kt - 底栏动画优化
- Navigation.kt - 页面切换动画简化

---

## [2.65.0] - 2026-02-28

### 变更类型：新增

### 状态：已实现

### 新增功能
- 功能 73：iOS 风格页面切换动画

### 功能详情

#### 功能 73：iOS 风格页面切换动画（新增）

**变更原因**：
用户反馈当前页面切换动画效果不喜欢、不符合直觉、不够流畅，希望改为 iOS 风格的滑动切换效果。

**问题描述**：
- 当前底部导航栏切换使用简单的淡入淡出动画，缺乏方向感
- 页面内部跳转（如进入设置子页面、课程编辑页面）同样使用淡入淡出，不符合用户直觉
- 动画效果单调，用户体验不佳

**变更内容**：

**1. 底部导航栏切换动画**
- 切换到右侧页面：新页面从右侧滑入，当前页面向左滑出
- 切换到左侧页面：新页面从左侧滑入，当前页面向右滑出
- 动画时长：250ms
- 缓动曲线：FastOutSlowInEasing

**2. 页面内部跳转动画（进入二级页面）**
- 新页面从右侧滑入
- 当前页面向左滑出（位移量为屏幕宽度的 30%）
- 动画时长：300ms
- 缓动曲线：FastOutSlowInEasing

**3. 返回动画（返回上一页）**
- 当前页面向右滑出
- 上一页从左侧滑入（从 -30% 位置滑入到 0）
- 动画时长：300ms
- 缓动曲线：FastOutSlowInEasing

**4. 动画参数**
- 页面滑动位移量：屏幕宽度
- 背景页面位移量：屏幕宽度的 30%
- 动画时长：底部导航切换 250ms，页面跳转 300ms
- 缓动曲线：FastOutSlowInEasing（开始快、结束慢）

**技术实现要点**：
- 修改 Navigation.kt 重构 NavHost 的 enterTransition、exitTransition、popEnterTransition、popExitTransition
- 使用 slideInHorizontally 和 slideOutHorizontally 实现滑动效果
- 底部导航栏切换时检测目标页面在当前页面的左侧还是右侧，根据方向设置滑动方向

**影响文件**：
- Navigation.kt - 重构页面切换动画
- AnimationSpecs.kt - 添加 iOS 风格动画配置
- Product-Spec.md - 新增功能 73

---

## [2.64.0] - 2026-02-28

### 变更类型：修改

### 状态：开发中

### 修改功能
- 功能 72：课表格子长按操作（菜单样式修改）

### 功能详情

#### 功能 72：课表格子长按操作（修改）

**变更原因**：
用户希望菜单在按住的格子上方弹出，并采用横向并排文字的样式，与设置界面风格一致。

**变更内容**：

**1. 菜单位置变更**
- 菜单从手指位置弹出改为在按住的格子上方弹出
- 通过 `onGloballyPositioned` 获取格子位置，定位菜单

**2. 菜单布局变更**
- 从垂直列表改为横向并排布局（Row）
- 菜单项仅显示文字，不显示图标
- 菜单项之间有分隔线

**3. 菜单视觉设计**
- 使用 Liquid Glass 效果（参考设置界面 GlassCard 样式）
- 深色模式配色：
  - 背景：半透明深色 + 模糊效果
  - 文字：浅色文字（LabelsDark.Primary）
  - 分隔线：半透明白色
- 浅色模式配色：
  - 背景：半透明白色 + 模糊效果
  - 文字：深色文字（LabelsLight.Primary）
  - 分隔线：半透明黑色
- 菜单高度：44dp
- 菜单项内边距：水平 16dp，垂直 12dp
- 菜单圆角：16dp（BorderRadius.iOS26.container）
- 破坏性操作（删除）使用红色文字

**影响文件**：
- `CourseContextMenu.kt` - 重构为横向菜单组件

---

## [2.63.0] - 2026-02-28

### 变更类型：新增

### 状态：已实现

### 新增功能
- 功能 72：课表格子长按操作

### 功能详情

#### 功能 72：课表格子长按操作（新增）

**变更原因**：
用户希望通过长按课表格子快速进行复制、粘贴、删除等操作，提升课程管理效率。

**变更内容**：

**1. 长按触发范围**
- 所有格子都支持长按操作（包括有课和无课的格子）
- 长按时间阈值：500ms
- 长按触发时有触觉反馈（震动）

**2. 有课程格子的操作菜单**
- 复制：复制当前课程到剪贴板
- 编辑：进入课程编辑界面
- 删除：删除当前课程

**3. 空白格子的操作菜单**
- 粘贴：粘贴已复制的课程到当前格子（仅在剪贴板有课程时显示）
- 添加课程：进入课程添加界面，自动预选当前格子的星期和节次

**4. 复制功能**
- 复制课程的全部信息：课程名称、地点、老师、周次
- 复制后课程存储在内存剪贴板中（不持久化）
- 复制成功后显示 Toast 提示"已复制"

**5. 粘贴功能**
- 粘贴时自动更新星期和节次为新格子的位置
- 粘贴时自动设置所属人为当前课表的所有者
- 粘贴前检测时间冲突：
  - 如目标格子已有课程，弹出确认对话框，提示用户选择覆盖或取消
  - 如无冲突，直接粘贴保存
- 粘贴成功后显示 Toast 提示"已粘贴"

**6. 删除功能**
- 删除前弹出确认对话框，提示用户确认删除
- 确认后删除课程
- 删除成功后显示 Toast 提示"已删除"

**7. 编辑功能**
- 直接进入课程编辑界面
- 与点击课程格子的行为一致

**8. 跨课表操作**
- 不支持跨课表复制/粘贴
- 复制的课程只能在同一课表内粘贴

**9. 操作菜单样式**
- 采用 ContextMenu 形式
- 在手指位置附近弹出
- 菜单项使用图标 + 文字

**10. 格子选中状态**
- 长按格子触发菜单时，被选中的格子显示蓝色边框高亮
- 边框颜色：主题蓝色（#4789FE）
- 边框宽度：2dp
- 边框圆角：8dp
- 菜单消失后边框自动消失

**11. 剪贴板管理**
- 剪贴板只保存最近一次复制的课程
- 应用重启后剪贴板清空
- 不提供撤销功能

**技术实现要点**：
- 使用 Compose 的 `combinedClickable` 修饰符实现长按检测
- 使用 `DropdownMenu` 组件实现 ContextMenu
- 创建 `CourseClipboard` 单例对象管理剪贴板
- 复用现有的时间冲突检测逻辑

**影响文件**：
- ScheduleScreen.kt - 添加长按手势和 ContextMenu
- ScheduleViewModel.kt - 添加复制、粘贴、删除等操作方法
- 新增 CourseClipboard.kt - 课程剪贴板管理
- Product-Spec.md - 新增功能 72

---

## [2.62.0] - 2026-02-28

### 变更类型：功能优化

### 状态：已实现

### 功能调整
- 功能 16：课表空白格子交互优化（原"点击格子自动选择星期和节次"）

### 功能详情

#### 功能 16：课表空白格子交互优化（调整 + 已实现）

**变更原因**：
用户反馈点击空白格子直接进入添加课程界面容易误触，希望改为两步操作，提供更明确的意图确认。

**变更内容**：

**1. 空白格子交互改为两步操作**
- 第一次点击空白格子：显示灰色半透明遮罩 + 居中加号图标
- 第二次点击同一格子：进入课程添加界面

**2. 遮罩交互逻辑**
- 点击其他格子：原格子遮罩消失，新格子显示遮罩
- 点击格子外区域（时间列、头部区域）：遮罩消失，恢复原状
- 点击有课程的格子：遮罩消失，直接进入编辑/预览界面
- 遮罩不会自动消失

**3. 已有课程格子保持原有行为**
- 点击有课程的格子：直接进入编辑/预览界面
- 不显示遮罩中间状态

**4. 遮罩样式**
- 背景：灰色半透明（深色模式 25% 白色，浅色模式 18% 黑色）
- 图标：白色加号图标，居中显示
- 图标大小：24dp
- 遮罩覆盖整个格子区域
- 无边框

**技术实现**：
- 新增 `EmptySlotPosition` 数据类记录选中格子位置
- 在 `WeeklyScheduleGrid` 中管理 `selectedEmptySlot` 状态
- 修改 `CourseSlot` 组件支持两步点击逻辑和遮罩UI
- 在时间列和头部区域添加点击清除遮罩的逻辑

**影响文件**：
- ScheduleScreen.kt - 空白格子点击交互逻辑重构
- Product-Spec.md - 功能 16 描述更新

---

## [2.61.0] - 2026-02-28

### 变更类型：新增

### 状态：已实现

### 新增功能
- 功能 71：课表周切换滑动动画重构

### 功能详情

#### 功能 71：课表周切换滑动动画重构（新增）

**变更原因**：
用户反馈课表页面左右滑动切换周数时，动画存在闪烁、跟随不流畅、松手动画不自然、边界处理异常等问题，需要重构整个滑动动画逻辑。

**问题描述**：

当前动画实现存在以下问题：
1. **闪烁/跳动**：滑动过程中课表内容出现闪烁或跳动
2. **跟随不流畅**：课表没有完全跟随手指移动，有延迟或卡顿
3. **松手动画不自然**：松手后切换动画不够平滑自然
4. **边界处理有问题**：边界滑动时表现异常或有 bug

**变更内容**：

**1. 滑动方向与切换关系**
- 向左滑动：切换到下一周（类似翻书效果）
- 向右滑动：切换到上一周
- 与用户直觉一致：想看后面的内容就向左滑，想看前面的内容就向右滑

**2. 跟随手指效果**
- 课表内容实时跟随手指位置移动
- 手指到哪，课表就移动到哪
- 无延迟、无卡顿、无闪烁
- 滑动过程中可以看到相邻周的内容从屏幕边缘滑入

**3. 切换判定逻辑**
- **松手即切换**：只要用户松手，就切换到滑动方向的相邻周
- 无论滑动距离多短，只要松手就触发切换
- 切换方向根据滑动方向确定：
  - 向左滑动松手 → 切换到下一周
  - 向右滑动松手 → 切换到上一周

**4. 边界处理**
- **硬边界**：到达边界后课表无法继续滑动，完全停止
- 第一周：无法向右滑动（无法查看上一周）
- 最后一周：无法向左滑动（无法查看下一周）
- 边界时提供触觉反馈（震动）

**5. 动画参数**
- 切换动画时长：200ms（快速响应，干脆利落）
- 缓动曲线：EaseInOutCubic（开始慢、中间快、结束慢，更有弹性感）
- 动画流畅无闪烁

**6. 视觉效果**
- 当前周课表跟随手指移动
- 相邻周课表从屏幕边缘滑入
- 滑动过程中可以看到两周边缘的内容
- 松手后平滑过渡到目标周

**技术实现**：

**1. 动画架构**
- 使用 `Animatable` 管理滑动偏移量
- 使用 `detectHorizontalDragGestures` 检测水平滑动手势
- 使用 `offset` 修饰符实时更新课表位置
- 预加载当前周、上一周、下一周三周的课程数据

**2. 位置计算**
- 当前周：`offset = dragOffset`
- 上一周：`offset = dragOffset - screenWidth`（在当前周左侧）
- 下一周：`offset = dragOffset + screenWidth`（在当前周右侧）

**3. 边界处理**
- 第一周时：限制向右滑动的最大距离为 0
- 最后一周时：限制向左滑动的最大距离为 0
- 边界时触发触觉反馈

**4. 切换逻辑**
- 松手时根据滑动方向确定目标周
- 使用 `animateTo` 平滑过渡到目标位置
- 切换完成后更新 `selectedWeek` 状态

**5. 防闪烁措施**
- 使用 `key` 确保课表组件正确复用
- 避免在滑动过程中重新创建组件
- 使用 `remember` 缓存计算结果
- 确保数据预加载完成后再显示

**影响文件**：
- ScheduleScreen.kt - 重构滑动动画逻辑
- Product-Spec.md - 新增功能 71

---

## [2.60.0] - 2026-02-28

### 变更类型：新增 + 优化

### 状态：待开发

### 新增功能
- 功能 70：数据导出导入功能优化

### 功能详情

#### 功能 70：数据导出导入功能优化（新增 + 优化）

**变更原因**：
用户反馈数据导出导入功能存在多个问题，需要优化。

**问题描述**：

**1. 导出数据错误**
- 人员名称导出错误：导出文件中的人员名称始终为默认值（"Ta"、"我"），而非用户设置的自定义名称
- 总周数导出错误：导出文件中的学期总周数与设置不符

**2. 导出导入逻辑解耦**
- 格式不兼容：导出文件无法直接导入，需要手动调整格式
- 设置信息未导入：导出包含设置信息，但导入时不读取这些设置
- 所属人逻辑不一致：导出包含两个人的课程，但导入模板只记录一个人的课程

**3. 外部应用无法触发导入**
- 通过微信"更多打开方式"选择"双人课程表"打开CSV文件时，应用启动但未进入导入流程

**变更内容**：

**1. 导出数据修复**
- 检查导出调用链，确保用户设置的人员名称正确传递到导出方法
- 检查 `SettingsDataStore` 中总周数的读取逻辑，确保导出时获取正确的值

**2. 导出范围选择**
- 导出时显示选择对话框，提供三个选项：
  - 仅我的课表：只导出 PersonB 的课程和设置
  - 仅Ta的课表：只导出 PersonA 的课程和设置
  - 双人课表：导出两个人的完整数据（默认选项）
- 导出文件名根据选择动态生成

**3. 导入功能优化 - 自动识别文件类型**

**类型1：应用导出CSV（完整格式）**
- 识别标志：文件开头包含 `# 双人课程表导出文件` 或 `# 版本:` 注释行
- 导入流程：自动解析 → 显示完整预览（设置+双人课程）→ 用户确认 → 直接导入
- 无需选择目标人员，按原所属人自动导入

**类型2：模板CSV（简化格式）**
- 识别标志：文件开头为表头行或不包含应用导出标志
- 导入流程：解析课程 → 显示预览 → 用户选择目标人员 → 导入

**导入流程图**
```
导入CSV文件
  ↓
解析文件内容
  ↓
识别文件类型
  ├─ 应用导出CSV → 显示完整预览 → 用户确认 → 导入设置+双人课程
  └─ 模板CSV → 显示课程预览 → 用户选择目标人员 → 导入单人课程
```

**4. 外部应用打开CSV文件导入**
- 在 `MainActivity.onCreate()` 中添加 Intent 处理逻辑
- 支持 `ACTION_VIEW` 和 `ACTION_SEND` 两种 Intent
- 提取文件 URI 并导航到导入预览页面

**技术实现要点**：
- 修改 `CsvExporter.kt` - 修复人员名称和总周数导出问题，添加文件类型识别逻辑
- 修改 `DataManagementScreen.kt` - 添加导出范围选择对话框
- 修改 `ImportPreviewScreen.kt` - 根据文件类型显示不同预览界面
- 修改 `MainActivity.kt` - 添加外部 Intent 处理逻辑

**影响文件**：
- CsvExporter.kt - 导出数据修复，文件类型识别，导入逻辑优化
- DataManagementScreen.kt - 导出范围选择
- ImportPreviewScreen.kt - 根据文件类型显示不同预览
- MainActivity.kt - 外部 Intent 处理
- Product-Spec.md - 新增功能 70

---

## [2.59.0] - 2026-02-28

### 变更类型：优化

### 状态：已实现

### 优化功能
- 功能 24：周数选择器布局优化

### 功能详情

#### 功能 24：周数选择器布局优化

**变更原因**：
用户反馈周数选择器居中显示显得空旷，占用太多垂直空间。

**变更内容**：
- 改为紧凑的行内布局（Row）
- 周数选择器在左侧，日期范围在右侧
- 缩小字体和内边距，减少空间占用
- 保留左右滑动切换周数的手势操作
- 保留点击展开周数选择弹窗功能

**布局对比**：
```
旧布局（垂直居中）：          新布局（行内紧凑）：
┌─────────────────────┐      ┌─────────────────────┐
│                     │      │ [第 X 周 ▼]  2/24-3/2│
│    [第 X 周 ▼]      │      └─────────────────────┘
│      2/24 - 3/2     │
│                     │
└─────────────────────┘
```

**影响文件**：
- 修改 `ScheduleScreen.kt` - 重构 `WeekSelectorSection` 组件为行内布局

---

## [2.58.0] - 2026-02-28

### 变更类型：修复

### 状态：已实现

### 修复问题
- 功能 69：课表周日期计算修复

### 问题详情

#### 功能 69：课表周日期计算修复

**问题描述**：
- 当用户设置开学时间为非周一（如周三）时，课表列头显示的日期与实际星期几不匹配
- 例如：开学日期设为 2026-02-25（周三），第 1 周显示的日期应该是周一 2/23 到周日 3/1，但实际显示错误

**问题原因**：
- `getWeekDates` 方法直接将开学日期作为每周的起始日期
- 没有考虑开学日期可能不是周一的情况
- 导致返回的日期列表第一个元素是开学日期本身，而不是该周的周一

**解决方案**：
- 使用 `TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)` 找到开学日期所在周的周一
- 以该周一作为第 1 周的起始日期
- 后续周数在此基础上正确计算

**影响文件**：
- 修改 `SettingsDataStore.kt` - 修复 `getWeekDates` 方法

---

## [2.57.0] - 2026-02-28

### 变更类型：修复

### 状态：已实现

### 修复问题
- 功能 67：上课自动静音权限问题修复

### 问题详情

#### 功能 67：上课自动静音权限问题修复

**问题描述**：
- 用户在系统设置的"通知使用权限"（勿扰权限）列表中找不到该应用
- 自动静音功能无法正常工作

**问题原因**：
- AndroidManifest.xml 缺少 `ACCESS_NOTIFICATION_POLICY` 权限声明
- 没有这个权限声明，应用不会出现在系统的勿扰模式权限授权列表中
- `notificationManager.isNotificationPolicyAccessGranted` 始终返回 false

**解决方案**：
- 在 AndroidManifest.xml 中添加 `ACCESS_NOTIFICATION_POLICY` 权限声明

**影响文件**：
- 修改 `AndroidManifest.xml` - 添加 ACCESS_NOTIFICATION_POLICY 权限

---

## [2.56.0] - 2026-02-28

### 变更类型：新增

### 状态：已实现

### 新增功能
- 功能 67：上课自动静音

### 功能详情

#### 功能 67：上课自动静音（新增 + 已实现）

**变更原因**：
用户希望在上课时自动静音，避免手机突然响起造成尴尬，课程结束后自动恢复铃声。

**变更内容**：

**1. 静音模式类型（用户可选）**
- 静音模式：完全静音，来电和通知都无声音
- 振动模式：来电/通知时振动提醒，不发声
- 勿扰模式：可设置例外（如允许联系人来电）
- 默认值：振动模式

**2. 触发时机**
- 课程开始时立即触发静音
- 不提前静音，避免过早影响用户

**3. 恢复时机**
- 课程结束时立即恢复铃声
- 连续课程处理：如果下一节课在 10 分钟内开始，保持静音状态直到所有连续课程结束
- 恢复后自动还原为用户原有的铃声模式

**4. 适用对象**
- 仅对"我的课程"生效
- 不处理 Ta 的课程

**5. 权限处理**
- 需要 `ACCESS_NOTIFICATION_POLICY` 权限（勿扰模式权限）
- Android 7.0+ 需要用户手动在系统设置中授权
- 在通知设置页面显示权限状态
- 提供跳转到系统设置的入口
- 权限未授予时显示引导提示

**6. 设置入口**
- 位置：设置 → 通知设置
- 设置项：上课自动静音开关、静音模式类型选择、权限状态显示、跳转系统设置按钮

**7. 默认状态**
- 功能默认开启
- 静音模式默认为振动模式

**技术实现**：

**1. 新增文件**
- `RingerModeManager.kt` - 铃声模式管理工具类
- `AutoSilentWorker.kt` - 自动静音调度任务

**2. 修改文件**
- `SettingsDataStore.kt` - 添加静音配置存储
- `CourseRepository.kt` - 添加静音配置访问方法
- `CourseNotificationManager.kt` - 集成静音功能调度
- `NotificationSettingsScreen.kt` - 添加静音设置 UI
- `SettingsViewModel.kt` - 添加静音设置相关方法

**影响文件**：
- 新增 `RingerModeManager.kt` - 铃声模式管理工具类
- 新增 `AutoSilentWorker.kt` - 自动静音调度任务
- 修改 `NotificationSettingsScreen.kt` - 添加静音设置 UI
- 修改 `SettingsDataStore.kt` - 添加静音配置存储
- 修改 `CourseNotificationManager.kt` - 集成静音功能
- Product-Spec.md - 更新功能 67 状态为已实现

---

## [2.55.0] - 2026-02-28

### 变更类型：新增

### 状态：待开发

### 新增功能
- 功能 67：上课自动静音

### 功能详情

#### 功能 67：上课自动静音（新增）

**变更原因**：
用户希望在上课时自动静音，避免手机突然响起造成尴尬，课程结束后自动恢复铃声。

**变更内容**：

**1. 静音模式类型（用户可选）**
- 静音模式：完全静音，来电和通知都无声音
- 振动模式：来电/通知时振动提醒，不发声
- 勿扰模式：可设置例外（如允许联系人来电）
- 默认值：振动模式

**2. 触发时机**
- 课程开始时立即触发静音
- 不提前静音，避免过早影响用户

**3. 恢复时机**
- 课程结束时立即恢复铃声
- 连续课程处理：如果下一节课在 10 分钟内开始，保持静音状态直到所有连续课程结束
- 恢复后自动还原为用户原有的铃声模式

**4. 适用对象**
- 仅对"我的课程"生效
- 不处理 Ta 的课程

**5. 权限处理**
- 需要 `ACCESS_NOTIFICATION_POLICY` 权限（勿扰模式权限）
- Android 7.0+ 需要用户手动在系统设置中授权
- 在通知设置页面显示权限状态
- 提供跳转到系统设置的入口
- 权限未授予时显示引导提示

**6. 设置入口**
- 位置：设置 → 通知设置
- 设置项：上课自动静音开关、静音模式类型选择、权限状态显示、跳转系统设置按钮

**7. 默认状态**
- 功能默认开启
- 静音模式默认为振动模式

**技术实现要点**：
- 使用 `NotificationManager.isNotificationPolicyAccessGranted()` 检查权限
- 使用 `AudioManager.setRingerMode()` 设置铃声模式
- 使用 `WorkManager` 调度静音和恢复任务
- 静音前保存用户当前的铃声模式，恢复时还原

**影响文件**：
- 新增 `RingerModeManager.kt` - 铃声模式管理工具类
- 新增 `AutoSilentWorker.kt` - 自动静音调度任务
- 修改 `NotificationSettingsScreen.kt` - 添加静音设置 UI
- 修改 `SettingsDataStore.kt` - 添加静音配置存储
- 修改 `CourseNotificationManager.kt` - 集成静音功能
- Product-Spec.md - 新增功能 67

---

## [2.54.0] - 2026-02-28

### 变更类型：功能优化

### 状态：已实现

### 功能优化
- 功能 4：共同空闲时间显示优化

### 功能详情

#### 功能 4：共同空闲时间显示优化（优化）

**变更原因**：
用户反馈空闲时间多于一个时显示不完整，且希望点击空闲时间区域能查看所有空闲时间，同时过去的空闲时间不应该显示。

**变更内容**：

**1. 过滤过去的空闲时间**
- 空闲时间计算时根据当前时间过滤
- 只显示当前时间之后的空闲时段
- 如果当前时间在某空闲时段中间，则从当前时间开始显示剩余部分

**2. 点击查看所有空闲时间**
- 点击空闲时间卡片弹出 BottomSheet 显示所有空闲时间列表
- 列表中每个时段可点击查看详情
- 列表显示时段时间和时长

**3. UI 优化**
- 卡片提示文字从"点击查看详情"改为"点击查看全部"
- 新增 AllFreeTimeSlotsSheet 组件显示所有空闲时间
- 新增 FreeTimeSlotItem 组件显示单个时段列表项

**技术实现**：
- MainViewModel.kt - calculateFreeTimeSlots 方法增加当前时间参数，过滤过去的空闲时间
- FreeTimeSection.kt - 新增 AllFreeTimeSlotsSheet 和 FreeTimeSlotItem 组件
- FreeTimeSection.kt - FreeTimeCard 增加点击查看全部功能
- Product-Spec.md - 更新功能 4 的业务规则

**影响文件**：
- `app/src/main/java/com/duoschedule/ui/main/MainViewModel.kt`
- `app/src/main/java/com/duoschedule/ui/main/components/FreeTimeSection.kt`
- `Product-Spec.md`

**用户体验提升**：
- 空闲时间显示更加准确，不再显示已过去的时间
- 用户可以方便地查看所有空闲时间
- 列表展示更加清晰，信息更完整

---

## [2.53.0] - 2026-02-28

### 变更类型：功能优化

### 状态：已实现

### 功能新增
- 功能 66：课表周切换平移动画优化

### 功能详情

#### 功能 66：课表周切换平移动画优化（新增）

**变更原因**：
用户反馈课表页面左右滑动切换周数时，原有的淡入淡出动画不够直观，希望改为平滑的平移动画，让整个课表跟随滑动方向平移切换。

**变更内容**：

**1. 动画效果变更**
- 原动画：淡入淡出（fadeIn + fadeOut）
- 新动画：水平平移（slideInHorizontally + slideOutHorizontally）
- 动画时长：从 150ms 调整为 300ms
- 缓动曲线：FastOutSlowInEasing

**2. 平移方向逻辑**
- 向左滑动（切换到下一周）：新内容从右侧滑入，旧内容向左滑出
- 向右滑动（切换到上一周）：新内容从左侧滑入，旧内容向右滑出
- 与用户滑动方向一致，符合直觉

**技术实现**：
- ScheduleScreen.kt - 修改 AnimatedContent 的 transitionSpec
- 使用 slideInHorizontally 和 slideOutHorizontally 替代 fadeIn 和 fadeOut
- 根据 lastDragDirection 动态设置平移方向
- Product-Spec.md - 新增功能 66

**影响文件**：
- `app/src/main/java/com/duoschedule/ui/schedule/ScheduleScreen.kt`
- `Product-Spec.md`

**用户体验提升**：
- 切换周数时动画更加流畅自然
- 平移方向与滑动方向一致，符合用户直觉
- 视觉连贯性更好，用户能清晰感知周数的变化

---

## [2.52.0] - 2026-02-28

### 变更类型：重构

### 状态：已实现

### 功能新增
- 功能 65：时间选择器 BottomSheet 重构

### 功能详情

#### 功能 65：时间选择器 BottomSheet 重构（新增）

**变更原因**：
用户反馈时间设置里的时间选择器需要改为 BottomSheet 形式，取消选中区域的灰色背景框，选中项改为蓝色，未选中项改为灰色，标题需要动态显示课节和时长。

**变更内容**：

**1. BottomSheet 形式**
- 将原有的 Dialog 改为 BottomSheet 形式
- 使用 GlassBottomSheet 组件，保持与项目整体风格一致
- 从底部滑出，提供更好的操作体验

**2. 滚轮选择器样式优化**
- 取消选中区域的灰色背景框
- 选中项文字颜色：蓝色（iOS 26 TintBlue #0A84FF）
- 未选中项文字颜色：灰色（LabelsVibrantSecondary）
- 选中项字号放大（20sp），未选中项字号较小（16sp）
- 添加平滑的颜色和字号动画过渡

**3. 标题格式优化**
- 标题改为动态格式："请调节第n节课的时间"
- 副标题显示时长："本节x分钟"，根据选择动态调整

**4. 时间显示格式**
- 开始时间和结束时间中间用横杠（-）连接
- 时间格式：HH:mm - HH:mm

**技术实现**：
- TimeRangeBottomSheet.kt - 新增 BottomSheet 组件
- PeriodTimesSettingsScreen.kt - 更新使用新的 BottomSheet
- Product-Spec.md - 新增功能 65

**影响文件**：
- app/src/main/java/com/duoschedule/ui/settings/components/TimeRangeBottomSheet.kt（新增）
- app/src/main/java/com/duoschedule/ui/settings/PeriodTimesSettingsScreen.kt（修改）
- Product-Spec.md（更新）

---

## [2.51.0] - 2026-02-28

### 变更类型：重构

### 状态：已实现

### 功能新增
- 功能 64：时间选择器重构

### 功能详情

#### 功能 64：时间选择器重构（新增）

**变更原因**：
用户反馈时间设置里的时间选择器太难用，滚轮滚动不够流畅，开始时间和结束时间分开显示不够直观，整体体验不佳。

**变更内容**：

**1. 滚轮选择器重构**
- 采用与节次选择器相同的滚轮实现方式
- 使用 LazyColumn + snapFlingBehavior 实现流畅的 snap 滚动效果
- 选中项高亮显示，使用主题色
- 选中项字号放大（20sp），未选中项字号较小（16sp）
- 添加平滑的颜色和字号动画过渡（150ms，FastOutSlowInEasing）
- 选中区域使用浅色背景高亮（深色模式 20% 白色，浅色模式 15% 黑色）

**2. 时间范围选择布局优化**
- 开始时间和结束时间并排显示，方便对比
- 开始时间使用蓝色（Ta 的主题色），结束时间使用黄色（我的主题色）
- 自动计算并显示时长（如"45分钟"、"1小时30分钟"）
- 弹窗宽度扩大到屏幕的 92%，提供更大的操作空间

**3. 组件架构优化**
- `WheelPicker` - 通用滚轮选择器组件
- `TimeWheelPicker` - 单时间选择器（小时 + 分钟）
- `TimeRangeWheelPicker` - 时间范围选择器（开始 + 结束）
- 重构 `TimeRangeAlert` 使用新的时间选择器组件

**技术实现**：
- WheelTimePicker.kt - 完全重构滚轮选择器
- InputDialogs.kt - 重构 TimeRangeAlert 组件
- Product-Spec.md - 新增功能 64

**影响文件**：
- WheelTimePicker.kt - 滚轮选择器重构
- InputDialogs.kt - 时间选择弹窗重构
- Product-Spec.md - 文档更新

---

## [2.50.0] - 2026-02-28

### 变更类型：优化

### 状态：已实现

### 功能新增
- 功能 63：新增课程界面布局优化

### 功能详情

#### 功能 63：新增课程界面布局优化（新增）

**变更原因**：
用户反馈新增课程界面中，教室地点和上课老师输入区域占用过多空间，标题和输入框垂直排列导致不必要的留白。

**变更内容**：

**1. 教室地点布局优化**
- 从垂直布局（图标 + 标题在上，输入框在下）改为水平布局
- 新布局：图标 + 标题 + 输入框 在同一行
- 标题固定宽度 72dp，输入框自适应剩余空间
- 输入框单行显示，placeholder 简化为"点击输入"

**2. 上课老师布局优化**
- 从垂直布局改为水平布局
- 新布局：图标 + 标题 + 输入框 在同一行
- 标题固定宽度 72dp，输入框自适应剩余空间
- 输入框单行显示，placeholder 简化为"点击输入（可选）"

**3. 历史建议样式优化**
- 上课老师的历史建议从垂直列表改为水平排列的 Chip 样式
- 更紧凑的显示，不占用过多垂直空间

**技术实现**：
- CourseEditScreen.kt - 修改 `LocationInputRow` 和 `TeacherInputRow` 为水平布局
- CourseEditScreen.kt - 新增 `SuggestionChip` 组件
- Product-Spec.md - 新增功能 63

**影响文件**：
- CourseEditScreen.kt - 布局优化
- Product-Spec.md - 文档更新

---

## [2.49.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 功能调整
- 功能 61：课表虚线边框开关 → 课表网格线分隔设置

### 功能详情

#### 功能 61：课表网格线分隔设置（调整）

**变更原因**：
用户希望将课表格子的虚线边框改为更简洁的横线和竖线分隔，并将设置项从"课表设置"移动到"显示设置"中。

**变更内容**：

**1. 分隔线样式调整**
- 从虚线边框改为横线和竖线分隔
- 移除虚线效果（PathEffect.dashPathEffect）
- 使用简单的实线分隔各格子
- 线宽：1dp
- 颜色：浅色模式 `#20000000`，深色模式 `#40FFFFFF`

**2. 设置位置调整**
- 从"设置 → 课表设置 → 课表外观设置"移动到"设置 → 显示设置 → 课表外观"
- 设置项名称从"显示虚线边框"改为"显示网格线分隔"
- 移除"Ta的虚线边框"设置项（合并为单一设置）

**3. 设置表述优化**
- 设置项名称：显示网格线分隔
- 副标题：在课表格子之间显示横线和竖线分隔

**技术实现**：
- ScheduleScreen.kt - 将虚线边框改为横线和竖线分隔
- DisplaySettingsScreen.kt - 添加网格线分隔设置开关
- ScheduleSettingsScreen.kt - 移除原有的虚线边框设置
- SettingsDataStore.kt - 保持 `showDashedBorder` 配置项（语义上改为网格线分隔）
- Product-Spec.md - 更新功能 61 描述

**影响文件**：
- ScheduleScreen.kt - 分隔线样式调整
- DisplaySettingsScreen.kt - 添加设置开关
- ScheduleSettingsScreen.kt - 移除设置项
- Product-Spec.md - 功能描述更新

---

## [2.48.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 61：课表虚线边框开关

### 功能详情

#### 功能 61：课表虚线边框开关（新增）

**问题描述**：
- 之前虚线只显示在空格子上，用户希望虚线作为格子分隔线显示在所有格子上
- 用户希望可以控制虚线的显示/隐藏

**解决方案**：

**1. 虚线样式调整**
- 虚线应用于所有格子（有课和无课的格子）
- 作为格子分隔线使用
- 虚线参数：线段长度 6dp，间隔 4dp，线宽 1dp

**2. 添加虚线开关**
- 开关位置：设置 → 课表设置 → 课表外观设置
- 默认状态：开启（显示虚线边框）
- 用户可自由控制虚线显示

**技术实现**：
- SettingsDataStore.kt：添加 `showDashedBorder` 配置项
- CourseRepository.kt：添加 `getShowDashedBorder()` 和 `setShowDashedBorder()` 方法
- ScheduleViewModel.kt：添加 `showDashedBorder` StateFlow
- ScheduleScreen.kt：根据配置显示/隐藏虚线边框
- ScheduleSettingsScreen.kt：添加开关 UI 组件

**影响文件**：
- ScheduleScreen.kt - 虚线样式调整和开关支持
- ScheduleViewModel.kt - 添加虚线开关读取
- SettingsDataStore.kt - 添加虚线开关存储
- CourseRepository.kt - 添加虚线开关访问方法
- ScheduleSettingsScreen.kt - 添加开关 UI
- Product-Spec.md - 新增功能 61 描述

---

## [2.47.0] - 2026-02-27

### 变更类型：优化 + Bug修复

### 状态：已实现

### 新增功能
- 功能 60：课表界面优化

### 功能详情

#### 功能 60：课表界面优化（新增）

**问题描述**：
- 课表中没有课程的空格子显示为灰色块，视觉上显得杂乱
- 首次打开课表页面时，格子数量会先多后少，出现闪烁现象
- 左右滑动切换周数时，滑动阈值过高，感觉滑不动

**解决方案**：

**1. 空格子改为虚线边框样式**
- 移除空格子的灰色背景填充
- 改为虚线边框样式，视觉更轻量
- 虚线参数：线段长度 6dp，间隔 4dp，线宽 1.5dp
- 边框颜色：浅色模式 `#20000000`，深色模式 `#40FFFFFF`

**2. 修复首次加载闪烁问题**
- 添加数据加载状态检查
- 在数据完全加载前显示加载指示器
- 修复 ViewModel 中 totalPeriods 默认值从 12 改为 5

**3. 优化滑动切换灵敏度**
- 将滑动阈值从 100dp 降低到 60dp
- 更容易触发周数切换

**影响文件**：
- ScheduleScreen.kt - 空格子样式、加载状态、滑动阈值
- ScheduleViewModel.kt - totalPeriods 默认值
- Product-Spec.md - 新增功能 60 描述

---

## [2.46.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 59：返回按钮大小优化

### 功能详情

#### 功能 59：返回按钮大小优化（新增）

**问题描述**：
- 当前返回按钮使用 `GlassSymbolIconButton` 组件，默认大小为 48dp
- 按钮在页面顶部显得过大，与其他元素不协调
- 用户反馈按钮太大，影响视觉美观

**解决方案**：
- 将 `ComponentSize.LiquidGlassButton.IconButtonSize` 从 48dp 调整为 40dp
- 按钮更加紧凑，与页面其他元素更协调

**技术实现**：
- 修改 `DesignTokens.kt` 中的 `IconButtonSize` 常量
- 从 48.dp 改为 40.dp

**影响文件**：
- DesignTokens.kt - IconButtonSize 常量调整
- Product-Spec.md - 新增功能 59 描述

---

## [2.45.0] - 2026-02-27

### 变更类型：Bug修复

### 状态：已实现

### 修复问题
- 功能 58：深色/浅色模式切换后部分页面背景显示异常

### 功能详情

#### 功能 58：深色/浅色模式切换后部分页面背景显示异常（修复）

**问题描述**：
- 手机系统为深色模式时，手动在应用内切换到浅色模式后，部分页面背景显示异常
- 背景色没有正确跟随主题变化

**问题原因**：

**1. MainActivity.kt 中的 backdrop 缓存问题**
- `rememberLayerBackdrop` 创建的 backdrop 在初始化时捕获了 `backgroundColor`
- 当主题切换时，`backgroundColor` 变化了，但 backdrop 不会自动更新
- 因为 `remember` 会缓存初始值

**2. 多个页面使用 `Color.Transparent` 作为容器背景**
- 以下页面使用了 `containerColor = Color.Transparent`：
  - DisplaySettingsScreen.kt
  - UserSettingsScreen.kt
  - ScheduleSettingsScreen.kt
  - DataManagementScreen.kt
  - CourseEditScreen.kt
- 这会导致在某些情况下背景显示异常

**解决方案**：

**1. 修复 MainActivity.kt 中的 backdrop**
- 为 `rememberLayerBackdrop` 添加 `key` 参数，使用 `darkTheme` 作为 key
- 当 `darkTheme` 变化时，backdrop 会重新创建，从而获取新的背景色

**2. 修复所有页面的容器背景色**
- 将所有 `containerColor = Color.Transparent` 改为 `containerColor = MaterialTheme.colorScheme.background`
- 确保背景色跟随主题变化

**影响文件**：
- MainActivity.kt - 添加 backdrop 的 key 参数
- DisplaySettingsScreen.kt - 修复容器背景色
- UserSettingsScreen.kt - 修复容器背景色
- ScheduleSettingsScreen.kt - 修复容器背景色
- DataManagementScreen.kt - 修复容器背景色
- CourseEditScreen.kt - 修复容器背景色

---

## [2.44.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 57：设置项点击交互优化

### 功能详情

#### 功能 57：设置项点击交互优化（新增）

**变更原因**：
显示设置页面中"主题设置"和"今日课程设置"需要先点击横条查看信息，再点击下方按钮才能弹出选择弹窗，操作步骤繁琐。需要简化交互流程，改为点击横条直接弹出弹窗。

**变更内容**：

**1. 交互方式变更**
- 主题设置：点击"深色模式"横条直接弹出主题模式选择弹窗
- 今日课程设置：点击"今日课程显示模式"横条直接弹出显示模式选择弹窗
- 移除原有的"选择主题模式"和"选择显示模式"按钮

**2. 横条样式优化**
- 横条整体可点击，添加点击涟漪效果
- 右侧显示当前选中值（如"跟随系统"、"都显示"）
- 右侧添加箭头图标，提示可点击

**3. 视觉反馈**
- 点击横条时有涟漪动画效果
- 横条可点击区域覆盖整行

**技术实现要点**：
- 将 Row 组件添加 `clickable` 修饰符
- 移除 ButtonPrimary 按钮
- 添加右侧箭头图标（Icons.Default.ChevronRight 或类似）

**影响文件**：
- Product-Spec.md - 新增功能 57 描述
- DisplaySettingsScreen.kt - 交互方式重构

---

## [2.43.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 功能调整
- 统一文档中"我"和"Ta"的顺序，将所有"我"放在"Ta"前面

### 功能详情

#### 文档顺序统一（优化）

**变更原因**：
产品文档中部分描述将"Ta"放在"我"前面，与用户视角不一致。需要统一为"我"在前、"Ta"在后的顺序，更符合用户习惯。

**变更内容**：

**1. 功能编号调整**
- 功能 8：原"Ta的课表页面"调整为"我的课表页面"
- 功能 9：原"我的课表页面"调整为"Ta的课表页面"

**2. 功能描述顺序调整**
- 用户设置二级页面：从"Ta的名称、我的名称"调整为"我的名称、Ta的名称"
- 导出范围：从"仅Ta的课表、仅我的课表"调整为"仅我的课表、仅Ta的课表"

**3. 术语表更新**
- 移除术语表中的"人员A"和"人员B"标注，简化描述

**影响文件**：
- Product-Spec.md - 功能编号互换，描述顺序统一

---

## [2.42.0] - 2026-02-27

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 56：底部导航栏 iOS 26 风格优化

### 功能详情

#### 功能 56：底部导航栏 iOS 26 风格优化（新增）

**变更原因**：
根据 Figma iOS 26 设计规范（非最小化版本），重构底部导航栏，优化选中状态的视觉效果，采用圆角矩形背景高亮设计，提升视觉一致性。

**变更内容**：

**1. 选中状态设计优化**
- 选中项背景：圆角矩形高亮背景（药丸形状）
  - 浅色模式：`#EDEDED`（浅灰色）
  - 深色模式：`#121212`（深灰色）
- 背景圆角：100dp
- 背景尺寸：72dp × 54dp（非最小化版本）

**2. 图标和文字颜色调整**
- 选中状态：
  - 浅色模式：`#0088FF`（iOS 蓝色）
  - 深色模式：`#0091FF`（iOS 蓝色）
- 未选中状态：
  - 浅色模式：`#1A1A1A`（深灰色）
  - 深色模式：`#F5F5F5`（浅灰色）

**3. 布局结构优化**
- 图标和文字包裹在选中背景内
- 图标大小：18dp
- 文字大小：10sp
- 图标与文字间距：1dp
- 按钮间距：-8dp（负间距，让按钮视觉上更紧凑）

**技术实现要点**：
- 更新 `DesignTokens.kt` 添加 `IOS26Colors.TabBar` 颜色定义
- 重构 `MainActivity.kt` 中的 `IOSStyleNavItem` 组件
- 调整 `IOSStyleBottomNavBar` 中的 Row 间距为负值

---

## [2.41.0] - 2026-02-26

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 55：时间设置滚轮选择器

### 功能详情

#### 功能 55：时间设置滚轮选择器（新增）

**变更原因**：
原有的时间设置使用文本输入框，用户需要手动输入 "HH:mm-HH:mm" 格式的时间，操作繁琐且容易出错。需要改为更直观的滚轮选择器，提升用户体验。

**变更内容**：

**1. 新增滚轮选择器组件**
- 创建 `WheelPicker` 通用滚轮选择器组件
- 创建 `WheelTimePicker` 单时间选择组件
- 创建 `WheelTimeRangePicker` 时间范围选择组件

**2. 重构时间设置对话框**
- 将 `TimeRangeInputDialog` 从文本输入改为滚轮选择
- 分别选择开始时间和结束时间
- 自动验证结束时间必须晚于开始时间

**3. 视觉效果优化**
- 选中项使用较大字号和主题色高亮
- 未选中项使用较小字号和灰色
- 上下渐变遮罩，突出选中区域
- 选中区域使用浅色背景高亮

**技术实现要点**：
- 新建 `WheelTimePicker.kt` 文件
- 修改 `InputDialogs.kt` 文件中的 `TimeRangeInputDialog` 函数

---

## [2.40.0] - 2026-02-26

### 变更类型：优化

### 状态：已实现

### 新增功能
- 功能 54：通知渠道优化

### 功能详情

#### 功能 54：通知渠道优化（新增）

**变更原因**：
应用存在 4 个通知渠道，用户在系统设置中看到过多渠道，造成困惑。需要合并重复渠道，简化用户体验。

**变更内容**：

**1. 渠道合并方案**
- 合并前：4 个渠道
  - `course_reminder_channel` - 课前提醒
  - `course_ongoing_channel` - 上课中
  - `course_ongoing_live_channel` - 上课实况通知
  - `live_update_service_channel` - 实时更新服务
- 合并后：2 个渠道
  - `course_reminder_channel` - 课程提醒（保留）
  - `course_ongoing_channel` - 上课状态（合并其他渠道到此）

**2. 代码修改**
- 删除 `CHANNEL_ID_ONGOING_LIVE` 常量
- 删除 `LiveUpdateService.CHANNEL_ID` 常量
- 删除 `createLiveNotificationChannel()` 方法
- 删除 `LiveUpdateService.createNotificationChannel()` 方法
- 统一使用 `CHANNEL_ID_ONGOING` 渠道

**技术实现要点**：
- 修改 `CourseNotificationManager.kt`
- 修改 `LiveUpdateService.kt`

---

## [2.39.0] - 2026-02-26

### 变更类型：新增

### 状态：已实现

### 新增功能
- 功能 53：通知调试区优化

### 功能详情

#### 功能 53：通知调试区优化（新增）

**变更原因**：
用户反馈通知设置页面中的调试区按钮点击无反应，缺乏交互反馈，无法有效调试通知功能。

**变更内容**：

**1. 按钮点击响应优化**
- 所有按钮添加防抖处理，避免快速重复点击
- 按钮点击后立即显示 SnackBar 反馈
- 成功提示：显示绿色 SnackBar，内容为"通知已发送"
- 失败提示：显示红色 SnackBar，内容包含具体错误原因

**2. 权限状态检查**
- 在调试区顶部显示当前权限状态卡片
- 显示内容：通知权限状态、实况通知权限状态、系统版本、是否为小米设备
- 权限状态实时更新
- 点击权限卡片可跳转到系统设置

**3. 日志查看器**
- 在调试区底部添加"查看日志"按钮
- 点击后弹出日志查看对话框
- 日志内容：通知发送时间、类型、结果、关键参数值
- 日志最多保留 50 条
- 支持清空日志和复制日志内容

**4. 仅 Debug 构建显示**
- 调试区仅在 debug 构建中显示
- release 版本自动隐藏调试区

**技术实现要点**：
- NotificationTestHelper 重构，方法返回 Result<Unit> 类型
- 创建 NotificationPermissionStatusCard 组件
- 创建 NotificationDebugLog 数据类和 LogViewerDialog 组件

**状态**：待开发

---

## [2.38.0] - 2026-02-26

### 变更类型：新增

### 新增功能
- 功能 52：Android 15 实况通知 (Promoted Notifications) 适配

### 功能详情

#### 功能 52：Android 15 实况通知适配（新增）

**变更原因**：
根据 Android 15 实况通知适配教程，优化上课中通知在锁屏和状态栏的显示效果，提供更突出的实时状态展示。

**变更内容**：

**1. 版本兼容性**
- 严格版本检查：仅在 Android 15 (API 36) 及以上设备使用实况通知 API
- 低版本设备保持现有通知逻辑，不做任何改变
- 修复现有代码中 `canPostPromotedNotifications()` 低版本返回 true 的问题

**2. 通知渠道管理**
- 新增独立的实况通知渠道：`course_ongoing_live_channel`
- 渠道名称：上课实况通知
- 渠道重要性：IMPORTANCE_HIGH
- 现有渠道保持不变：`course_ongoing_channel`（上课中）

**3. 状态栏简短文本**
- 使用 `setShortCriticalText()` 在状态栏显示剩余时间
- 显示格式：剩余 X 分钟，如"剩余 45 分钟"
- 仅在实况通知启用时显示

**4. 用户控制**
- 在通知设置中添加"实况通知"开关
- 用户可在应用内控制是否启用实况通知
- 默认开启

**5. 权限检查**
- 检查 `canPostPromotedNotifications()` 权限
- 在通知设置中显示实况通知权限状态
- 提供跳转到系统设置的入口

**6. 通知更新节流**
- 添加通知更新节流机制
- 最小更新间隔：500ms
- 避免频繁更新导致性能问题

**技术实现**：

**1. CourseNotificationManager 重构**
- 修复 `canPostPromotedNotifications()` 方法，低版本返回 false
- 新增 `CHANNEL_ID_ONGOING_LIVE` 常量
- 新增 `createLiveNotificationChannel()` 方法
- 重构 `showOngoingNotification()` 方法，根据条件选择渠道
- 添加 `setRequestPromotedOngoing(true)` 请求实况通知
- 添加 `setShortCriticalText()` 设置状态栏文本

**2. LiveUpdateService 重构**
- 重构 `buildNotification()` 方法
- 根据版本和权限选择通知渠道
- 添加实况通知支持

**3. NotificationThrottler 工具类**
- 新增通知更新节流器
- 实现 `shouldNotify()` 方法
- 最小更新间隔 500ms

**4. SettingsDataStore 扩展**
- 新增 `liveNotificationEnabled` 配置项
- 默认值：true

**5. NotificationSettingsScreen 更新**
- 新增"实况通知"开关
- 显示实况通知权限状态
- 提供跳转到系统设置入口

**6. 调试区域重构**
- 删除旧的 `NotificationTestHelper.kt` 中独立的测试逻辑
- 重构 `NotificationTestSection` 组件，直接调用现有通知代码
- 仅在 debug 构建中显示调试区域，release 版本自动隐藏
- 调试功能列表：
  - 测试提醒通知：调用 `CourseNotificationManager.showReminderNotification()`
  - 测试普通上课中通知：调用 `showOngoingNotification()` 使用普通渠道
  - 测试实况通知：调用 `showOngoingNotification()` 使用实况通知渠道（仅 Android 15+）
  - 清除测试通知：调用 `cancelReminderNotifications()` 和 `cancelOngoingNotification()`
- UI 风格：保持现有红色警告色背景，标注"调试用"

**影响文件**：
- Product-Spec.md - 新增功能 52 描述
- CourseNotificationManager.kt - 通知管理重构
- LiveUpdateService.kt - 前台服务通知重构
- SettingsDataStore.kt - 新增配置项
- NotificationSettingsScreen.kt - 新增设置项 UI，重构调试区域
- NotificationTestHelper.kt - 删除独立测试逻辑，改为调用现有通知代码

**状态**：待开发

---

## [2.37.0] - 2026-02-25

### 变更类型：优化

### 新增功能
- 功能 51：Live Updates 显示优化

### 功能详情

#### 功能 51：Live Updates 显示优化（新增 + 已实现）

**变更原因**：
用户反馈通知中的进度条显示不够简洁，希望只显示距离下课还有多少分钟。

**变更内容**：

**1. 移除进度条显示**
- 移除 Android 16 的 `Notification.ProgressStyle` 进度条
- 移除小米动态岛中的进度百分比参数
- 简化通知界面，只显示核心信息

**2. 通知内容格式**
- 课程名称：作为通知标题
- 地点 + 剩余时间：作为通知内容，格式为"地点 · 剩余 X 分钟"
- 保持倒计时计时器显示（setChronometerCountDown）

**3. 代码变更**
- `LiveUpdateService.kt`：移除 progressPercent 相关逻辑，简化 buildNotification 方法
- `MiuiIslandHelper.kt`：移除 progressPercent 参数，简化 buildProgressTickerData 和 createMiuiIslandNotification 方法

**影响文件**：
- Product-Spec.md - 新增功能 51 描述
- LiveUpdateService.kt - 移除进度条逻辑
- MiuiIslandHelper.kt - 移除进度参数

---

## [2.36.0] - 2026-02-25

### 变更类型：新增

### 新增功能
- 功能 50：小米动态岛（Dynamic Island）适配

### 功能详情

#### 功能 50：小米动态岛适配（新增）

**变更原因**：
参考其他应用的动态岛实现日志，发现小米设备使用特定的 JSON 数据格式（tickerData）实现动态岛显示，需要适配以获得更好的用户体验。

**变更内容**：

**1. 新增 MiuiIslandHelper 工具类**
- 创建 `MiuiIslandHelper.kt` 处理小米动态岛逻辑
- 实现 `buildTickerData()` 方法构建 JSON 数据
- 实现 `buildProgressTickerData()` 方法构建进度显示数据
- 实现 `buildReminderTickerData()` 方法构建提醒数据
- 实现 `applyMiuiIslandExtras()` 方法将 JSON 数据应用到通知
- 实现 `createMiuiIslandNotification()` 方法创建动态岛通知
- 实现 `createMiuiReminderNotification()` 方法创建提醒通知
- 实现 `isMiuiDevice()` 方法检测是否为小米设备

**2. JSON 数据结构**
- `islandProperty`: 岛属性值（2 表示应用态）
- `bigIslandArea`: 大岛区域
  - `imageTextInfoLeft`: 左侧图标+文字（课程名称）
  - `imageTextInfoRight`: 右侧文字（地点+剩余时间）
- `smallIslandArea`: 小岛区域（应用图标）

**3. 修改 LiveUpdateService**
- 在 `buildNotification()` 中优先使用小米动态岛通知
- 如果小米动态岛创建成功则直接返回
- 否则降级为 Android 16 ProgressStyle 或普通通知

**4. 修改 CourseNotificationManager**
- 在 `showReminderNotification()` 中优先使用小米动态岛提醒
- 在 `showOngoingNotification()` 中优先使用小米动态岛实时更新
- 自动检测设备类型并选择合适的实现方式

**代码变更**：
- 新增 MiuiIslandHelper.kt - 小米动态岛工具类
- 修改 LiveUpdateService.kt - 集成小米动态岛通知
- 修改 CourseNotificationManager.kt - 集成小米动态岛通知

**影响文件**：
- Product-Spec.md - 新增功能 50 描述
- MiuiIslandHelper.kt - 新增文件
- LiveUpdateService.kt - 通知构建逻辑更新
- CourseNotificationManager.kt - 通知发送逻辑更新

---

## [2.35.0] - 2026-02-25

### 变更类型：修改

### 修改功能
- 功能 34：课前通知（岛通知）- 简化为只通知自己的课程
- 功能 35：上课中岛通知 - 简化为只通知自己的课程
- 功能 36：通知设置 - 移除显示模式设置

### 功能详情

#### 功能 34/35/36：通知功能简化（修改）

**变更原因**：
用户反馈不需要通知Ta的课程，只需要通知自己的课程即可。

**变更内容**：

**1. 功能 34：课前通知**
- 移除"显示模式配置"选项（仅显示自己、仅显示Ta、同时显示两人）
- 简化为只通知自己的课程
- 移除 IslandDisplayMode 相关逻辑

**2. 功能 35：上课中岛通知**
- 移除"用户配置的显示模式"输入
- 简化为只显示自己的课程状态
- 移除 `showDualOngoingNotification()` 方法（不再需要双人同时显示）

**3. 功能 36：通知设置**
- 移除"岛通知显示模式"设置项
- 简化设置项为：课前提醒时间、通知权限状态

**代码变更**：
- CourseNotificationManager.kt - 移除 IslandDisplayMode 逻辑，只处理 PersonType.PERSON_B
- NotificationSettingsScreen.kt - 移除显示模式选择 UI
- SettingsDataStore.kt - 保留 getIslandDisplayMode 方法但不再使用
- IslandDisplayMode.kt - 保留枚举定义但不再用于通知

**影响文件**：
- Product-Spec.md - 功能描述更新
- CourseNotificationManager.kt - 通知逻辑简化
- NotificationSettingsScreen.kt - 设置页面简化
- LiveUpdateService.kt - 前台服务简化

---

## [2.34.0] - 2026-02-25

### 变更类型：新增 + 已实现

### 新增功能
- 功能 49：Android 16 Live Updates 适配

### 功能详情

#### 功能 49：Android 16 Live Updates 适配（新增 + 已实现）

**功能描述**：
适配 Android 16 引入的 Live Updates 功能，优化上课中通知的实时显示体验。Live Updates 是一种新型通知，允许应用向用户展示实时变化的信息，会在状态栏、锁屏和通知中心持续显示，并支持高频更新。

**核心改进**：

**1. 依赖升级**
- 升级 `androidx.core:core-ktx` 从 1.12.0 到 1.15.0
- Live Updates 需要 Jetpack 库 1.15+ 版本支持

**2. 权限声明**
- 在 AndroidManifest.xml 中添加 `POST_LIVE_UPDATES` 权限
- Android 16 开始，发布 Live Updates 需要显式权限

**3. 通知代码重构**
- 使用官方 `setLiveUpdate(true)` API 替代反射调用
- 使用 `setCategory(Notification.CATEGORY_STATUS)` 替代 `CATEGORY_PROGRESS`
- 添加 `setVisibility(Notification.VISIBILITY_PUBLIC)` 确保锁屏可见
- 通知通道配置优化：添加 `setShowBadge(true)` 和 `lockscreenVisibility`

**4. 兼容性处理**
- Android 16+ 设备启用 Live Updates
- Android 16 以下设备自动降级为普通通知
- 使用 `Build.VERSION.SDK_INT >= 35` 判断

**技术实现要点**：
- `showOngoingNotification()` 方法重构
- `showDualOngoingNotification()` 方法重构
- `createNotificationChannels()` 方法优化
- 移除反射调用代码，简化实现

**影响文件**：
- app/build.gradle.kts - 依赖升级
- AndroidManifest.xml - 权限声明
- CourseNotificationManager.kt - 通知代码重构
- Product-Spec.md - 新增功能 49

---

## [2.33.0] - 2026-02-25

### 变更类型：新增 + 已实现

### 新增功能
- 功能 48：课表格子文字样式优化

### 功能详情

#### 功能 48：课表格子文字样式优化（新增 + 已实现）

**问题描述**：
- 两个课表界面的课程名称和地点不太和谐
- 课程名称又大又粗（16sp + Medium），看着有点失衡
- 地点字号太小（10sp），不够清晰

**解决方案**：

**1. 课程名称字号调整**
- 从 16sp 减小到 14sp，更轻盈

**2. 课程名称字重调整**
- 从 Medium 改为 Bold，更醒目

**3. 地点字号调整**
- 从 10sp 增大到 12sp，更清晰

**4. 视觉风格**
- 层次分明但不悬殊
- 课程名称略大于地点，有层次感

**影响文件**：
- ScheduleScreen.kt - CourseCellContent 组件文字样式调整
- Product-Spec.md - 新增功能 48

---

## [2.32.0] - 2026-02-25

### 变更类型：新增 + 已实现

### 新增功能
- 功能 47：底部导航栏标签动态化

### 功能详情

#### 功能 47：底部导航栏标签动态化（新增 + 已实现）

**功能描述**：
底部导航栏的"我的课表"和"Ta的课表"标签根据用户设置的人员名称动态显示，提升个性化体验。

**核心功能**：

**1. 标签动态化规则**
- "我的课表"标签：显示为"{我的名称}的课表"，如"小明的课表"
- "Ta的课表"标签：显示为"{Ta的名称}的课表"，如"小红的课表"
- 未设置名称时使用默认值"我"和"Ta"

**2. 实时更新**
- 名称变更后导航栏标签实时更新
- 与主页今日课程切换按钮保持一致的显示逻辑

**3. 技术实现**
- 在 MainActivity.kt 中读取 personAName 和 personBName
- 使用动态字符串拼接显示标签
- 通过 collectAsStateWithLifecycle 实现响应式更新

### 影响文件
- MainActivity.kt - 底部导航栏标签动态化
- Product-Spec.md - 新增功能 47

---

## [2.31.0] - 2026-02-25

### 变更类型：优化

### 新增功能
- 功能 46：今日课程高度动态适配

### 功能详情

#### 功能 46：今日课程高度动态适配（新增 + 已实现）

**问题描述**：
- 今日课程区域设置了固定 400dp 高度
- 当课程较少时，会有明显的空白边界感
- 视觉上不够美观，用户反馈"界限看着不好看"

**解决方案**：

**1. 移除固定高度限制**
- 将 `height(400.dp)` 改为 `heightIn(max = 400.dp)`
- 高度根据内容动态计算

**2. 只显示有课程的时间段**
- 过滤掉没有课程的时间段
- 减少空白区域

**3. 使用 Column 替代 LazyColumn**
- 支持在 LazyColumn 中嵌套使用
- 保持滚动功能

**影响文件**：
- MainScreen.kt - 移除固定高度
- TodayScheduleTimeline.kt - 改为动态高度，过滤空白时间段

---

## [2.30.0] - 2026-02-25

### 变更类型：优化

### 新增功能
- 功能 45：课表格子配色优化

### 功能详情

#### 功能 45：课表格子配色优化（新增 + 已实现）

**问题描述**：
- 原有课程格子配色使用高饱和度的 Material Design 彩色
- 颜色过于鲜艳，视觉上显得杂乱
- 用户反馈"颜色丑丑的"

**解决方案**：

**1. 配色风格改为清新糖果色系**
- 鲜艳有活力，但不刺眼
- 饱和度和明度控制在舒适范围内
- 18 种清新色调：天蓝、薄荷绿、蜜桃粉、薰衣草紫、奶黄等

**2. 深色模式适配**
- 深色模式下颜色略微提亮（亮度增加 20%）
- 确保在深色背景上的可读性

**3. 配色逻辑保持不变**
- 根据课程名称哈希值从调色板中选择颜色
- 相同课程始终显示相同颜色

**影响文件**：
- Theme.kt - 更新 CourseColorPalette 为清新糖果色系
- Product-Spec.md - 新增功能 45

---

## [2.29.0] - 2026-02-25

### 变更类型：Bug修复 + 状态更新

### 修复问题
- 功能 1：双人课程表管理 - 周数显示逻辑修复

### 功能状态更新
以下功能状态从"待开发"更新为"已实现"：
- 功能 44：今日课程时间轴显示优化

### 功能详情

#### 功能 44：今日课程时间轴显示优化（状态更新）

**状态**：待开发 → 已实现

**实现内容**：

**1. 卡片样式统一**
- 所有课程卡片统一使用 72dp 固定高度（包括单列模式和双列模式）
- 三行布局：课程名称（加粗）+ 地点（灰色）+ 节次（主题色）
- 通过左侧色条颜色区分不同人员

**2. 卡片间距优化**
- 卡片间距统一为 6dp

**3. 无课状态展示**
- 当前时段无课程时，显示空闲时段占位卡片
- 占位卡片使用浅灰色背景 + 虚线边框
- 显示"空闲"文字 + 咖啡杯图标

**4. 切换按钮标签动态化**
- "我"按钮：显示为"{我的名称}"（如"小明"）
- "Ta"按钮：显示为"{Ta的名称}"（如"小红"）
- 未设置名称时使用默认值"我"和"Ta"

**影响文件**：
- TodayScheduleTimeline.kt - 重构卡片布局，统一 72dp 高度和三行布局，新增空闲占位组件
- MainScreen.kt - 切换按钮标签动态化

#### 功能 1：双人课程表管理 - 周数显示逻辑修复

**问题描述**：
- 课程编辑界面选择单独一周时，显示"1周"而非具体的周数
- 用户选择第5周，应该显示"第5周"，而不是"1周"

**解决方案**：
- 重构 `getWeekDisplayText` 函数，新增 `formatWeekRanges` 辅助函数
- 实现智能周数格式化逻辑：
  - 连续周数用横杠连接：如选择1-5周，显示"1-5周"
  - 不连续周数用逗号分隔：如选择1,3,5周，显示"1, 3, 5周"
  - 混合情况：如选择1-5,8,10周，显示"1-5, 8, 10周"
  - 单周显示：如只选择第5周，显示"第5周"

**影响文件**：
- CourseEditScreen.kt - 重构 `getWeekDisplayText` 和新增 `formatWeekRanges` 函数

---

## [2.28.0] - 2026-02-25

### 变更类型：新增 + 状态更新

### 新增功能
- 功能 44：今日课程时间轴显示优化

### 功能状态更新
以下功能状态从"待开发"更新为"已实现"：
- 功能 7：信息岛适配（后续版本）
- 功能 36：通知设置
- 功能 43：全局卡片圆角统一

### 功能详情

#### 功能 44：今日课程时间轴显示优化（新增）

**功能描述**：
优化主页今日课程时间轴的显示效果，统一卡片样式，完善信息展示，提升视觉体验。

**核心改进**：

**1. 卡片样式统一**
- 固定高度：所有课程卡片统一使用 72dp 固定高度（包括单列模式和双列模式）
- 三行布局：第一行显示课程名称，第二行显示上课地点，第三行显示时间节数
- 圆角样式：使用统一的 16dp 圆角
- 颜色区分：通过左侧色条颜色区分不同人员（PersonA 和 PersonB 各自的主题色）

**2. 信息显示完善**
- 课程名称：第一行，最大 1 行，超出截断显示省略号，字体加粗
- 上课地点：第二行，最大 1 行，超出截断显示省略号，灰色字体
- 时间节数：第三行，格式如"第1-2节"，使用主题色字体

**3. 卡片间距优化**
- 卡片间距统一为 6dp
- 列表顶部和底部增加 8dp 内边距

**4. 无课状态展示**
- 当前时段无课程时，显示空闲时段占位卡片
- 占位卡片使用浅灰色背景 + 虚线边框
- 显示"空闲"文字 + 咖啡杯/休息图标
- 高度与课程卡片一致（72dp）

**5. 切换按钮标签动态化**
- 切换按钮标签使用设置中的人员名称
- "我"按钮：显示为"{我的名称}"（如"小明"）
- "Ta"按钮：显示为"{Ta的名称}"（如"小红"）
- "全部"按钮：保持显示"全部"
- 未设置名称时使用默认值"我"和"Ta"

**影响文件**：
- TodayScheduleTimeline.kt - 重构卡片布局，统一 72dp 高度和三行布局，新增空闲占位组件
- MainScreen.kt - 切换按钮标签动态化，使用 personAName/personBName

#### 功能 7：信息岛适配（状态更新）

**状态**：待开发 → 已实现

**实现内容**：
- 适配 Android 原生信息岛功能，在状态栏显示当前课程状态
- 遵循 Android 信息岛 API 规范
- 显示内容精简，不支持时降级为普通小组件

#### 功能 36：通知设置（状态更新）

**状态**：待开发 → 已实现

**实现内容**：
- 设置入口位置：设置主页面，新增"通知设置"分类
- 设置项列表：课前提醒时间、岛通知显示模式、通知权限状态
- 配置存储：使用 DataStore 存储配置

#### 功能 43：全局卡片圆角统一（状态更新）

**状态**：待开发 → 已实现

**实现内容**：
- 圆角规范：主卡片 16dp、按钮 16dp、输入框 16dp、Chip 标签 16dp、底部弹窗 16dp、课程格子 8dp、进度条 2dp
- 底部导航栏特殊设计：整体胶囊形设计，左右两侧为半圆形
- AppShapes 配置更新：统一圆角值

---

## [2.27.0] - 2026-02-25

### 变更类型：新增 + 已实现

### 新增功能
- 功能 41：今日课程显示逻辑优化

### 功能详情

#### 功能 41：今日课程显示逻辑优化（已实现）

**功能描述**：
优化主页今日课程区域的显示逻辑，重新设计课程卡片布局，突出重要信息，提升信息可读性和用户体验。

**核心功能**：

**1. 信息优先级**
- 课程名称：最重要，用户首先需要知道是什么课
- 上课地点：同等重要，用户需要知道去哪里上课
- 上课时间：次要，用于判断课程状态

**2. 今日课程列表项（CourseListItem）布局重构**
- 采用"左侧时间条 + 右侧课程信息"的横向布局
- 左侧时间条：
  - 宽度 52dp
  - 显示开始时间和结束时间（上下分行）
  - 背景色为人员专属颜色（蓝色/Ta，黄色/我）
  - 已结束课程背景色变灰
- 右侧课程信息：
  - 课程名称：bodyLarge 字体，加粗显示，最多2行
  - 上课地点：bodyMedium 字体，最多1行
  - 时间范围：labelSmall 字体，显示在底部

**3. 当前课程卡片（CurrentCourseCard）布局优化**
- 左侧添加人员专属颜色竖条（4dp宽）
- 课程名称：titleMedium 字体，加粗显示，最多2行
- 上课地点：bodyMedium 字体，最多2行
- 剩余时间：使用人员专属颜色显示
- 节次信息：显示在时间旁边

**4. 下节课预告卡片优化**
- 显示"下节"标签
- 课程名称和地点同等重要
- 显示开始时间和节次信息

**5. 颜色方案**
- Ta（人员A）：蓝色系（#4789FE）
- 我（人员B）：黄色系（#FFB74D）
- 已结束课程：灰色（#9E9E9E），透明度 50%

**影响文件**：
- CourseListItem.kt - 重构布局，添加左侧时间条
- CurrentCourseCard.kt - 优化显示，添加左侧颜色条
- CurrentCourseState.kt - 添加 periodText 和 nextCoursePeriodText 字段
- MainViewModel.kt - 添加节次计算逻辑

---

## [2.24.0] - 2026-02-25

### 变更类型：新增 + 已实现

### 新增功能
- 功能 38：今日课程显示模式设置
- 功能 39：今日课程信息显示优化
- 功能 40：默认启动页面设置（暂不开发，需求已记录）

### 功能详情

#### 功能 38：今日课程显示模式设置（已实现）

**功能描述**：
在设置中添加今日课程显示模式选项，允许用户选择主页今日课程区域显示"我的课表"、"Ta的课表"或"都显示"。

**核心功能**：

**1. 显示模式选项**
- 仅显示我的课表：只显示用户自己的今日课程
- 仅显示Ta的课表：只显示Ta的今日课程
- 都显示：同时显示两个人的今日课程（默认值）

**2. 设置入口**
- 设置 → 显示设置
- 新增"今日课程显示模式"设置项

**3. 主页适配**
- 仅显示我的课表：当前课程卡片单列显示，今日课程列表单列显示
- 仅显示Ta的课表：同上，显示Ta的课程
- 都显示：保持现有双列布局

**4. 配置存储**
- 使用 DataStore 存储
- 枚举值：`SELF_ONLY`、`TA_ONLY`、`BOTH`
- 默认值：`BOTH`

**影响文件**：
- 新增 TodayCourseDisplayMode.kt - 显示模式枚举
- DisplaySettingsScreen.kt - 添加显示模式设置项
- SettingsDataStore.kt - 添加配置存储
- CourseRepository.kt - 添加配置访问方法
- SettingsViewModel.kt - 添加配置访问方法
- MainViewModel.kt - 读取显示模式配置
- MainScreen.kt - 根据显示模式调整布局
- CurrentCourseCard.kt - 新增 SingleCurrentCourseSection 组件

#### 功能 39：今日课程信息显示优化（已实现）

**功能描述**：
优化主页今日课程区域的信息显示，解决课程名称和上课地点因卡片太小被截断的问题。

**问题分析**：
- 当前课程卡片采用并排双列布局，宽度约为屏幕宽度的一半
- 课程名称和地点使用单行显示，超出部分显示省略号
- 当课程名称或地点较长时，信息无法完整显示

**优化方案**：

**1. 课程名称显示优化**
- 增加最大行数为2行
- 超出部分显示省略号

**2. 上课地点显示优化**
- 增加最大行数为2行
- 配合功能38的单列模式优化显示

**3. 卡片样式调整**
- 当前课程卡片：课程名称和地点行数从1行改为2行
- 今日课程列表项：课程名称和地点行数从1行改为2行

**4. 与功能38配合**
- 单列显示时，卡片宽度占满整行，更好地展示完整信息
- 双列显示时，通过增加行数确保信息完整

**影响文件**：
- CurrentCourseCard.kt - 当前课程卡片优化
- CourseListItem.kt - 今日课程列表项优化

#### 功能 40：默认启动页面设置（新增，暂不开发）

**功能描述**：
在设置中添加默认启动页面选项，允许用户选择打开应用时默认显示的页面。

**启动页面选项**：
- 主页（今日课程）：默认选项
- 我的课表：直接进入我的课表周视图
- Ta的课表：直接进入Ta的课表周视图

**设置入口**：
- 设置 → 显示设置
- 新增"默认启动页面"设置项

**配置存储**：
- 使用 DataStore 存储
- 枚举值：`HOME`、`MY_SCHEDULE`、`TA_SCHEDULE`
- 默认值：`HOME`

**状态**：待开发（暂不开发，需求已记录）

---

## [2.21.0] - 2026-02-25

### 变更类型：已实现

### 已实现功能
- 功能 37：主页布局优化（今日课程多时一屏显示）
- 功能 34：课前通知（岛通知）
- 功能 35：上课中岛通知
- 功能 36：通知设置
- 功能 28：节次选择框样式优化
- 功能 33：共同空闲时间显示优化

### 功能详情

#### 功能 37：主页布局优化（已实现）

**实现内容**：
1. **当前课程并排布局**：改为 Row 并排布局，两个卡片水平排列，各占一半宽度
2. **今日课程双列显示**：左列"我的课程"，右列"Ta的课程"，各自独立显示
3. **移除下节课预告**：节省空间，确保一屏显示
4. **紧凑样式**：减小内边距和字体大小，优化空间利用

**影响文件**：
- CurrentCourseCard.kt - 重构卡片布局和样式
- MainScreen.kt - 今日课程双列显示

#### 功能 34、35、36：通知功能组（已实现）

**实现内容**：
1. **通知管理模块**：新增 CourseNotificationManager.kt，支持课前提醒和上课中通知
2. **通知设置页面**：新增 NotificationSettingsScreen.kt，支持配置提前时间和显示模式
3. **WorkManager 集成**：新增 ReminderWorker.kt，实现定时通知调度
4. **设置存储**：扩展 SettingsDataStore.kt，添加通知相关配置项

**影响文件**：
- notification/IslandDisplayMode.kt - 新增枚举
- notification/CourseNotificationManager.kt - 新增通知管理
- notification/ReminderWorker.kt - 新增定时任务
- ui/settings/NotificationSettingsScreen.kt - 新增设置页面
- data/local/SettingsDataStore.kt - 扩展配置
- data/repository/CourseRepository.kt - 扩展方法
- ui/settings/SettingsViewModel.kt - 扩展方法
- ui/settings/SettingsScreen.kt - 添加入口
- ui/navigation/Navigation.kt - 添加路由
- DuoScheduleApp.kt - 初始化通知

#### 功能 28：节次选择框样式优化（已实现）

**实现内容**：
1. **选中项样式**：蓝色 (#4789FE)，字体放大到 18sp
2. **未选中项样式**：灰色，字体 14sp
3. **动画过渡**：使用 animateColorAsState 和 animateIntAsState 实现平滑过渡
4. **圆角优化**：22dp 药丸形状

**影响文件**：
- PeriodPickerBottomSheet.kt - 重构选择器样式

#### 功能 33：共同空闲时间显示优化（已实现）

**实现内容**：
1. **移除智能推荐**：所有时段平铺展示，无特殊突出
2. **药丸形状卡片**：圆角 22dp，浅色/深色模式背景色适配
3. **点击弹窗**：显示时段详情和建议文字
4. **空状态优化**：图标 + 文字组合显示

**影响文件**：
- FreeTimeSection.kt - 重构组件布局和样式

---

## [2.20.0] - 2026-02-25

### 变更类型：已实现

### 已实现功能
- 功能 31：当前课程卡片美化优化
- 功能 32：UI Bug 修复

### 功能详情

#### 功能 31：当前课程卡片美化优化（已实现）

**实现内容**：
1. **布局方式**：从并排双卡片改为上下堆叠布局，卡片间距 12dp
2. **卡片内容增强**：
   - 有课状态：显示课程名称、上课地点、剩余时间、进度条
   - 无课状态：显示咖啡杯图标 + "休息中"文字
3. **进度条**：卡片背景进度条，从左到右填充，使用人员专属颜色半透明填充
4. **下节课预告**：独立小卡片，显示下节课名称、时间、地点
5. **配色方案**：Ta 蓝色 (#4789FE)，我 黄色 (#FFB74D)

**影响文件**：
- CurrentCourseState.kt - 扩展数据模型
- CurrentCourseCard.kt - 重构卡片布局
- MainViewModel.kt - 添加进度和下节课计算
- Theme.kt - 更新人员颜色

#### 功能 32：UI Bug 修复（已实现）

**修复内容**：
1. **空闲时间白色方块**：在 GlassmorphismCard 的 Box 中添加 `.height(IntrinsicSize.Min)` 修复高度问题
2. **移除首页 FAB**：删除 MainScreen.kt 中的 floatingActionButton 代码块

**影响文件**：
- Glassmorphism.kt - 修复卡片高度问题
- MainScreen.kt - 移除 FAB

---

## [2.19.0] - 2026-02-25

### 变更类型：Bug修复

### 修复问题
- 功能 32：UI Bug 修复

### 功能详情

#### 功能 32：UI Bug 修复（新增）

**问题描述**：

**1. 空闲时间图层下方的白色方块**
- 问题位置：主页"共同空闲时间"区域
- 问题表现：当显示"今天没有共同空闲时间"的空状态卡片时，卡片下方出现白色方块
- 问题原因：GlassmorphismCard 组件布局问题
- 解决方案：检查并修复空状态卡片布局，确保高度自适应内容

**2. 首页去除新增课程的加号按钮**
- 问题位置：主页右下角的 FloatingActionButton
- 问题表现：首页显示添加课程的 FAB 按钮，用户希望移除
- 解决方案：移除 MainScreen.kt 中的 floatingActionButton 代码块

### 影响文件
- FreeTimeSection.kt - 检查并修复空状态卡片布局
- Glassmorphism.kt - 可能需要调整 GlassmorphismCard 组件
- MainScreen.kt - 移除 FloatingActionButton

---

## [2.18.0] - 2026-02-25

### 变更类型：新增

### 新增功能
- 功能 31：当前课程卡片美化优化

### 功能详情

#### 功能 31：当前课程卡片美化优化（新增）

**功能描述**：
重构主页"当前课程"区域，优化双人课程状态卡片的布局和视觉效果，提升用户体验和信息展示效率。

**核心改进**：

**1. 布局方式**
- 从并排双卡片改为上下堆叠布局
- 每个卡片独占一行，展示空间更大
- 卡片间距：12dp

**2. 卡片内容增强**
- 有课状态：
  - 显示课程名称、上课地点、剩余时间
  - 卡片背景进度条（从左到右填充）
  - 进度颜色使用人员专属颜色半透明填充
- 无课状态：
  - 显示咖啡杯图标（☕）
  - 显示"休息中"或"空闲"文字
  - 保持玻璃拟态样式

**3. 下节课预告**
- 独立小卡片，位于当前课程卡片下方
- 显示下节课名称、开始时间、上课地点
- 紧凑横条样式

**4. 视觉风格**
- 保持玻璃拟态设计（Glassmorphism）
- 深色/浅色模式自适应

**5. 配色方案**
- Ta（人员A）：蓝色系（#4789FE）
- 我（人员B）：黄色系（#FFB74D 或 #FFA726）

**6. 卡片交互**
- 卡片不可点击，仅展示信息
- 无点击反馈效果

**7. 动画效果**
- 课程切换淡入淡出动画
- 进度条平滑填充动画
- 下节课预告卡片出现/消失动画
- 动画时长：200ms，缓动曲线：FastOutSlowIn

**技术实现要点**：
- 扩展 `CurrentCourseState` 数据模型，添加进度和下节课信息
- 重构 `CurrentCourseCard.kt` 组件布局
- 在 `MainViewModel.kt` 中添加下节课数据查询和进度计算
- 在 `Theme.kt` 中添加黄色系人员颜色

### 影响文件
- CurrentCourseCard.kt - 重构卡片布局和样式
- MainScreen.kt - 调整卡片排列方式
- MainViewModel.kt - 添加下节课数据和进度计算
- CurrentCourseState.kt - 扩展数据模型
- Theme.kt - 添加黄色系人员颜色

---

## [2.16.0] - 2026-02-24

### 变更类型：新增 + 已实现

### 新增功能
- 功能 29：外部应用打开CSV文件导入

### 功能详情

#### 功能 29：外部应用打开CSV文件导入（新增）

**功能描述**：
支持通过微信、QQ、系统文件管理器等应用打开CSV文件，快速进入课程导入流程。

**业务规则**：

**1. 支持的打开方式**
- 微信：聊天中接收的CSV文件，点击后选择"其他应用打开"，选择本应用
- QQ：聊天中接收的CSV文件，点击后选择"其他应用打开"，选择本应用
- 系统文件管理器：在文件管理器中点击CSV文件，选择本应用打开
- 其他应用：支持系统标准的文件分享 Intent

**2. 应用启动状态处理**
- 应用在后台运行：直接进入导入流程
- 应用未启动：先启动应用，再进入导入流程

**3. 导入目标选择**
- 打开文件后显示课程预览页面
- 预览页面顶部显示选择对话框：导入到"我的课表"或"Ta的课表"
- 用户选择后开始导入流程

**4. 支持的编码格式**
- UTF-8：最常见的编码格式
- UTF-8 with BOM：Windows 环境常见
- GBK/GB2312：Excel 默认导出的编码格式，中文环境常见
- 自动检测编码格式，优先尝试 UTF-8

**5. 错误处理**
- 文件格式错误时显示详细错误提示
- 错误提示格式："第X行：字段Y格式错误，期望格式为..."
- 例如："第3行：星期格式错误，期望格式为1-7的数字"
- 例如："第5行：开始节次格式错误，期望格式为正整数"

**6. 导入成功后处理**
- 显示导入成功提示（导入课程数量）
- 提供两个选项：
  - "返回主页"：返回应用主页
  - "查看课表"：跳转到导入目标课表页面

**技术实现**：
- 在 AndroidManifest.xml 中注册 Intent Filter，支持 `ACTION_VIEW` 和 `ACTION_SEND`
- 支持的 MIME 类型：`text/csv`、`text/comma-separated-values`、`application/csv`
- 使用 ContentResolver 读取外部文件内容
- 编码检测：使用juniversalchardet或类似库自动检测编码

**异常处理**：
- 文件读取失败：提示"无法读取文件，请重试"
- 编码检测失败：提示"文件编码不支持，请转换为UTF-8格式"
- 文件格式错误：显示详细错误提示
- 应用启动超时：显示加载提示

### 影响文件
- AndroidManifest.xml - 注册 Intent Filter
- MainActivity.kt - 处理外部 Intent
- CsvImporter.kt - 支持多种编码格式
- ImportPreviewScreen.kt - 导入目标选择对话框
- 新增编码检测工具类

---

## [2.15.0] - 2026-02-24

### 变更类型：优化

### 功能调整
- 统一全局蓝色为 #4789FE

### 功能详情

#### 统一全局蓝色（优化）

**问题描述**：
- 应用中存在多种蓝色色调（#2196F3、#5B9DF5、#5C9CFF、#7AB8FF 等）
- 颜色不统一，影响视觉一致性
- 需要统一为标准蓝色 #4789FE

**解决方案**：

**1. Theme.kt 主色调更新**
- PrimaryLight: #2196F3 → #4789FE
- PrimaryDark: #5B9DF5 → #4789FE
- PrimaryContainerLight: #E3F2FD → #E8F1FF
- OnPrimaryContainerLight: #1565C0 → #1A5CB0
- OnPrimaryDark: #000000 → #FFFFFF（确保对比度）
- OnPrimaryContainerDark: #D6E7FF → #E8F1FF

**2. 人员区分色更新**
- PersonAColorLight: #5C9CFF → #4789FE（Ta 的颜色统一为蓝色）
- PersonAColorDark: #7AB8FF → #4789FE

**3. colors.xml 资源更新**
- primary: #FF6B6B → #4789FE
- primary_variant: #E55555 → #3A7AE6
- person_a_color: #FF6B6B → #4789FE
- person_b_color: #4ECDC4 → #FF8A65（与我方颜色保持一致）
- free_time_color: #90EE90 → #81C784（与 Theme.kt 保持一致）

### 影响文件
- Theme.kt - 主色调和人员区分色统一
- colors.xml - 资源文件颜色更新

---

## [2.14.0] - 2026-02-24

### 变更类型：新增

### 新增功能
- 功能 28：节次选择框样式优化

### 功能详情

#### 功能 28：节次选择框样式优化（新增）

**问题描述**：
- 编辑课程界面的节次选择框文字较小，可读性不佳
- 选中项与未选中项区分不够明显
- 缺乏视觉反馈，用户体验有待提升

**解决方案**：

**1. 整体文字放大**
- 选择框内所有文字放大至原来的 120%
- 提升整体可读性

**2. 选中项文字样式**
- 选中项文字颜色：#4789FE（蓝色）
- 选中项文字大小：比未选中项大 4sp
- 无背景变化，仅通过文字颜色和大小区分

**3. 未选中项文字样式**
- 未选中项文字颜色：浅灰色
- 未选中项文字大小：基础大小（放大后的 120%）

**4. 动画过渡**
- 选中状态切换时，文字大小和颜色变化有平滑过渡动画
- 动画时长：150-200ms
- 缓动曲线：FastOutSlowIn

**5. 适用范围**
- 编辑课程界面的节次选择框
- 三列滑动选择器：周几选择、开始节次选择、结束节次选择

**技术实现**：
- 使用 Compose 的 `animateColorAsState` 实现颜色过渡动画
- 使用 `animateDpAsState` 或 `animateFloatAsState` 实现文字大小过渡动画
- 使用 `animateContentSize` 实现布局变化动画

### 影响文件
- CourseEditScreen.kt - 节次选择框样式优化
- WeekPickerBottomSheet.kt - 可能涉及相关组件

---

## [2.13.0] - 2026-02-24

### 变更类型：新增 + 已实现

### 新增功能
- 功能 27：课表周切换滑动动画

### 功能详情

#### 功能 27：课表周切换滑动动画（新增 + 已实现）

**问题描述**：
- 两个课程表界面左右滑动切换周时，没有动画提示
- 用户有时候不知道有没有切换成功
- 缺乏视觉反馈，用户体验不佳

**解决方案**：

**1. 滑动方向与切换关系**
- 向左滑动：显示上一周
- 向右滑动：显示下一周

**2. 动画效果**
- 课表整体跟随手指滑动，呈现平滑的横向移动效果
- 周数显示跟随课表一起滑动，作为整体的一部分
- 动画持续时间：150ms
- 缓动曲线：FastOutSlowIn（开始快结束慢，自然舒适）

**3. 切换触发方式**
- 根据滑动距离判断是否切换周数
- 滑动距离超过 100dp 触发切换

**4. 边界处理**
- 第一周向左滑动：触觉反馈（震动）+ Toast提示"已是第一周"
- 最后一周向右滑动：触觉反馈（震动）+ Toast提示"已是最后一周"

**5. 适用范围**
- "我的课表"页面：支持滑动切换动画
- "Ta的课表"页面：支持滑动切换动画

**技术实现**：
- 使用 Compose 的 `AnimatedContent` 实现滑动切换动画
- 使用 `slideInHorizontally` 和 `slideOutHorizontally` 实现横向滑动效果
- 使用 `performHapticFeedback` 实现触觉反馈
- 使用 `Toast.makeText` 显示边界提示

### 影响文件
- ScheduleScreen.kt - 实现滑动切换动画、边界反馈

---

## [2.12.0] - 2026-02-24

### 变更类型：新增 + 修改

### 新增功能
- 功能 26：CSV 导入模板

### 功能调整
- 功能 6：课程数据导入 - 适配新模板格式

### 功能详情

#### 功能 26：CSV 导入模板（新增）

**1. 模板内容**
- 完整模板，包含 7 个字段
- 字段列表：课程名称、星期、开始节次、结束节次、教室地点、上课老师、周次
- 周次支持多种格式：范围格式（1-16）、单周/双周、列表格式（1,3,5,7）
- **不包含所属人字段**：用户在导入预览页面统一选择导入目标

**2. 模板说明**
- CSV 第一行为表头说明，包含字段名称和填写提示
- 预填充 2-3 条示例数据，帮助用户理解格式

**3. 模板下载入口**
- 入口位置：设置 → 数据管理页面
- 显示「下载 CSV 导入模板」按钮

**4. 下载方式**
- 保存到本地：文件保存到手机本地存储
- 分享到其他应用：调用系统分享功能
- 两种方式同时提供，用户可选择

**5. 错误处理**
- 用户上传格式错误的 CSV 文件时，显示简单提示
- 提示内容：文件格式错误，请对照模板检查

#### 功能 6：课程数据导入（修改）

**适配新模板格式**：
- CSV 字段格式明确：课程名称、星期、开始节次、结束节次、教室地点、上课老师、周次
- 周次支持多种格式：范围格式（1-16）、单周/双周、列表格式（1,3,5,7）
- 用户在预览页面统一选择导入目标（我的课表/Ta的课表），CSV 中不再包含所属人字段
- 错误提示简化：文件格式错误时提示"文件格式错误，请对照模板检查"

### 影响文件
- DataManagementScreen.kt - 添加模板下载入口
- 新增 assets/course_template.csv - 模板文件
- CsvImporter.kt - 适配新模板格式，支持多种周次格式解析
- ImportPreviewScreen.kt - 确保导入目标选择功能正常

---

## [2.10.0] - 2026-02-24

### 变更类型：优化

### 功能调整
- 功能 24：课表周数显示优化

### 功能详情

#### 功能 24：课表周数显示优化（优化）

**1. 周数选择器与标题同行**
- 周数选择器与"我的课表"/"Ta的课表"标题放在同一行
- 标题在左侧，周数选择器在右侧
- 周数显示格式："第 X 周"

**2. 移除周数切换按钮**
- 移除左右箭头按钮
- 仅保留滑动切换方式
- 更简洁的界面布局

### 影响文件
- ScheduleScreen.kt - 周数选择器布局优化

---

## [2.9.0] - 2026-02-24

### 变更类型：新增

### 新增功能
- 功能 23：课表界面交互优化

### 功能详情

#### 功能 23：课表界面交互优化（新增）

**1. 周数选择器位置调整**
- 周数选择器移至页面最顶部（TopAppBar 下方）
- 周数显示更加醒目
- 左右箭头按钮保持原有功能

**2. 滑动切换周数**
- 支持左右滑动切换周数
- 向左滑动：上一周
- 向右滑动：下一周
- 滑动时显示周数变化动画
- 边界处理：第一周时不能向左滑，最后一周时不能向右滑

**3. 左侧节数显示优化**
- 节数数字放大显示（更醒目）
- 时间文字缩小显示（节省空间）
- 减少左侧占用宽度（从 50dp 优化为更紧凑）
- 保持时间信息可读性

### 影响文件
- ScheduleScreen.kt - 周数选择器位置调整、滑动切换、左侧显示优化

---

## [2.8.0] - 2026-02-24

### 变更类型：新增 + 优化

### 新增功能
- 功能 22：设置界面交互优化

### 功能详情

#### 功能 22：设置界面交互优化（新增）

**1. 时间设置三级界面**
- 将课表设置中的时间设置移入独立的三级界面
- 新增 `PeriodTimesSettingsScreen` 页面
- 添加保存按钮，修改后需手动保存
- 点击时间项弹出编辑对话框

**2. 显示设置增强**
- 新增"课表外观设置"分类
- 新增"显示周六"开关（默认开启）
- 新增"显示周日"开关（默认关闭）
- 新增"显示非本周课程"开关
- 新增"课程格子高度"设置（40-100dp，默认60dp）

**3. 输入交互优化**
- 所有可直接输入的框改为点击弹出对话框
- 新增通用输入对话框组件：
  - `TextInputDialog`：文本输入
  - `NumberInputDialog`：数字输入（带范围验证）
  - `TimeRangeInputDialog`：时间范围输入（带格式验证）
  - `SliderInputDialog`：滑块选择输入
- 点击设置项显示当前值，点击后弹出对应的输入对话框

**4. 课表显示适配**
- 根据显示设置动态显示/隐藏周六、周日列
- 根据格子高度设置调整课程格子最小高度

### 影响文件
- 新增 `PeriodTimesSettingsScreen.kt` - 时间设置三级界面
- 新增 `components/InputDialogs.kt` - 通用输入对话框组件
- 修改 `ScheduleSettingsScreen.kt` - 移除时间设置，添加三级导航，输入改为弹窗
- 修改 `DisplaySettingsScreen.kt` - 添加课表外观设置
- 修改 `UserSettingsScreen.kt` - 输入改为弹窗
- 修改 `Navigation.kt` - 添加时间设置三级页面路由
- 修改 `SettingsViewModel.kt` - 添加新设置项支持
- 修改 `ScheduleViewModel.kt` - 添加新设置项读取
- 修改 `ScheduleScreen.kt` - 根据设置显示周六/周日，调整格子高度
- 修改 `SettingsDataStore.kt` - 添加新设置项存储
- 修改 `CourseRepository.kt` - 添加新设置项访问

---

## [2.7.0] - 2026-02-24

### 变更类型：新增 + Bug修复

### 新增功能
- 功能 21：课表显示优化与Bug修复

### 功能详情

#### 功能 21：课表显示优化与Bug修复（新增）

**1. 课程方框显示优化**
- 移除固定宽高比限制，使用固定最小高度
- 课程方框内显示课程名称和地点
- 优化文字大小和布局，确保信息完整显示

**2. 非标准节数适配**
- 使用 LazyColumn 替代 Column 循环，消除大间隙问题
- 无论设置多少节课，课表都能正常显示无过大间隙

**3. 时间冲突检测**
- 实现完整的时间冲突检测逻辑
- 检测同一天、同一人、时间重叠的课程
- 支持周次冲突检测（单周/双周/自定义周次）
- 保存课程时自动检测并提示冲突

**4. 小组件时间监听**
- 在 Application 中注册 TimeChangeReceiver
- 时间变化时自动刷新小组件

**5. 默认节数调整**
- 默认节数从12节调整为5节
- 默认时间配置相应调整

### 影响文件
- ScheduleScreen.kt - 课程方框显示优化，使用 LazyColumn
- CourseRepository.kt - 实现时间冲突检测
- CourseDao.kt - 新增同步查询方法
- CourseEditViewModel.kt - 保存时检测冲突
- DuoScheduleApp.kt - 注册 TimeChangeReceiver
- SettingsDataStore.kt - 默认节数调整

---

## [2.6.0] - 2026-02-24

### 变更类型：状态更新

### 功能状态更新
- 功能 20：全面性能优化 - 从"待开发"更新为"已实现"

### 已实现的优化项

**1. 启动优化**
- ✅ 添加 Splash Screen 启动画面（使用 AndroidX SplashScreen API）
- ✅ 数据库预加载（在 Application 启动时异步初始化）
- ✅ 启动时间监控

**2. 页面切换优化**
- ✅ 统一 Flow 订阅策略（全部使用 SharingStarted.Lazily）
- ✅ 页面切换动画优化

**3. 数据库优化**
- ✅ 添加数据库索引（personType、dayOfWeek、startWeek、endWeek）
- ✅ 数据库版本升级至 v2
- ✅ 配置 fallbackToDestructiveMigration

**4. Compose 编译优化**
- ✅ 添加 Kotlin 编译器优化参数
- ✅ 配置 debug/release 构建类型

**5. 性能监控**
- ✅ 新增 PerformanceMonitor 工具类
- ✅ 启动时间监控
- ✅ 数据库查询监控
- ✅ 帧率监控

### 影响文件
- app/build.gradle.kts - 添加 SplashScreen 依赖和编译优化配置
- app/src/main/res/values/themes.xml - 添加 Splash 主题
- app/src/main/AndroidManifest.xml - 应用 Splash 主题
- MainActivity.kt - 集成 SplashScreen 和性能监控
- DuoScheduleApp.kt - 数据库预加载和性能监控
- MainViewModel.kt - 统一 Flow 订阅策略
- Course.kt - 添加数据库索引
- AppDatabase.kt - 数据库版本升级
- DatabaseModule.kt - 配置迁移策略
- 新增 PerformanceMonitor.kt - 性能监控工具

---

## [2.5.0] - 2026-02-24

### 变更类型：新增

### 新增功能
- 功能 20：全面性能优化

### 功能详情

#### 功能 20：全面性能优化（新增）
**问题描述**：
- 应用启动慢，冷启动时间超过2秒
- 页面切换存在卡顿现象
- 数据库查询效率低
- 缺乏性能监控机制

**解决方案**：

**1. 启动优化**
- 添加启动画面（Splash Screen），使用 Android 12+ SplashScreen API
- Hilt 延迟初始化：非核心依赖使用 `@EntryPoint` 延迟加载
- 数据库预加载：在 Application 启动时异步初始化数据库
- 数据预取：启动时预加载常用数据到内存缓存
- Compose 基线配置：启用 `composeCompilerReports` 和 `composeMetrics`

**2. 页面切换优化**
- 统一 Flow 订阅策略：全部使用 `SharingStarted.Lazily`
- 数据共享：使用 `shareIn` 共享课程数据，避免重复查询
- 页面预加载：在主页时预加载课表页面数据
- 导航动画优化：使用硬件加速动画

**3. 数据库优化**
- 添加索引：为 `dayOfWeek`、`personType`、`startWeek`、`endWeek` 字段添加索引
- 查询优化：使用 `@Query` 的 `LIMIT` 和 `ORDER BY` 优化
- 事务优化：批量操作使用 `@Transaction` 注解
- 连接池配置：配置 Room 数据库连接池大小

**4. 内存优化**
- 图片资源优化：使用 WebP 格式，压缩资源大小
- 对象池：复用常用对象，减少 GC 压力
- 内存泄漏检测：集成 LeakCanary 检测内存泄漏
- 大对象延迟加载：非必要数据延迟加载

**5. 性能监控**
- 启动时间监控：记录冷启动、热启动时间
- 页面渲染监控：监控帧率和掉帧情况
- 数据库查询监控：记录慢查询
- 内存使用监控：监控内存峰值和内存泄漏

**性能目标**：
- 冷启动时间：< 1秒
- 热启动时间：< 500ms
- 页面切换时间：< 100ms
- 数据库查询时间：< 50ms
- 内存占用：< 100MB
- 帧率：稳定 60fps

**影响范围**：
- DuoScheduleApp.kt：添加数据库预加载
- MainActivity.kt：添加 Splash Screen
- MainViewModel.kt：统一 Flow 订阅策略
- ScheduleViewModel.kt：优化数据共享
- Course.kt：添加数据库索引
- CourseDao.kt：优化查询语句
- build.gradle.kts：添加性能相关依赖和配置
- 新增 PerformanceMonitor.kt：性能监控工具类

**技术实现**：
- SplashScreen API：使用 `androidx.core.splashscreen.SplashScreen`
- Compose 编译优化：配置 `kotlinOptions.freeCompilerArgs`
- Room 索引：在 `@Entity` 中添加 `indices`
- 性能监控：使用 `androidx.metrics:metrics-performance`

---

## [2.4.0] - 2026-02-24

### 变更类型：状态更新

### 功能状态更新
以下功能状态从"待开发"更新为"已实现"：

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 功能 3：今日课程列表 | 高 | 显示今日完整课程安排 |
| 功能 5：桌面小组件 | 高 | 桌面显示当前课程状态 |
| 功能 6：课程数据导入 | 高 | 支持 CSV、Wakeup、教务系统导入 |

### 影响范围
- Product-Spec.md 功能状态更新

---

## [2.3.0] - 2026-02-24

### 变更类型：新增 + 优化

### 新增功能
- 功能 19：页面切换性能优化

### 功能详情

#### 功能 19：页面切换性能优化（新增）
**问题描述**：
- 底部导航栏切换页面时存在卡顿现象
- 页面切换缺少过渡动画，体验生硬

**解决方案**：
1. **Flow 订阅策略优化**：
   - 将 `SharingStarted.WhileSubscribed(5000)` 改为 `SharingStarted.Lazily`
   - 避免页面切换时 Flow 停止订阅，减少数据重新加载
   - 课程数据使用 `shareIn` 共享，减少重复查询

2. **导航动画**：
   - 添加页面切换过渡动画
   - 使用 fadeIn/fadeOut + slideInHorizontally/slideOutHorizontally 组合
   - 动画时长 200ms，使用 FastOutSlowInEasing 缓动曲线

3. **列表动画**：
   - 主页列表项添加入场动画
   - 使用 AnimatedVisibility 包裹列表项
   - 添加淡入 + 滑动效果

**影响范围**：
- MainViewModel.kt
- ScheduleViewModel.kt
- SettingsViewModel.kt
- Navigation.kt
- MainScreen.kt
- 新增 AnimationSpecs.kt

---

## [2.2.0] - 2026-02-24

### 变更类型：状态更新

### 功能状态更新
以下功能状态从"待开发"更新为"已实现"：

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 功能 1：双人课程表管理 | 高 | 核心功能，支持课程增删改查 |
| 功能 2：当前课程状态显示 | 高 | 实时显示两人当前课程 |
| 功能 3：今日课程列表 | 高 | 显示今日完整课程安排 |
| 功能 4：共同空闲时间 | 中 | 计算共同空闲时段 |
| 功能 5：桌面小组件 | 高 | 桌面显示当前课程状态 |
| 功能 6：课程数据导入 | 高 | 支持 CSV、Wakeup 导入 |
| 功能 8：Ta的课表页面 | 高 | 独立课表页面 |
| 功能 9：我的课表页面 | 高 | 独立课表页面 |
| 功能 10：课表参数设置 | 高 | 开学时间、周数、节数配置 |
| 功能 11：非本周课程显示开关 | 中 | 显示设置开关 |
| 功能 12：课表导航顺序调整 | 低 | 导航顺序优化 |
| 功能 13：课表周数日期显示 | 中 | 列头显示日期 |
| 功能 14：课表左侧节数与时间显示 | 高 | 左侧显示节数和时间 |
| 功能 15：设置界面二级导航重构 | 高 | 二级导航结构 |
| 功能 16：点击格子自动选择星期和节次 | 中 | 自动预选功能 |
| 功能 17：课程数据导出 | 高 | CSV 导出功能 |
| 功能 18：小米文件选择控件接入 | 高 | 使用 Android 原生文件选择器 |

### 待开发功能
- 功能 7：信息岛适配（后续版本）

### 影响范围
- Product-Spec.md 功能状态更新

---

## [2.1.0] - 2026-02-24

### 变更类型：新增

### 新增功能
- 功能 18：小米文件选择控件接入

### 功能详情

#### 功能 18：小米文件选择控件接入（新增）
**接入范围**：
- CSV文件导入：从CSV文件导入课程数据
- Wakeup文件导入：从Wakeup课程表备份文件导入
- CSV文件导出：导出课程数据为CSV文件

**接入优势**：
- 应用不再需要申请完整的存储权限
- 解决用户拒绝授权导致应用功能不可用问题
- 应用只能读取用户选择的文件数据，满足用户最小化授权的要求
- 提供系统级的一致用户体验

**技术实现**：
- 使用小米文件选择控件SDK（HyperOS 3及以上）
- 兼容处理：在不支持小米文件选择控件的系统上，降级使用Android原生ActivityResultContracts
- 导入使用文件选择模式（单选/多选）
- 导出使用文件创建模式

**兼容性要求**：
- HyperOS 3（基于Android 16）及以上：使用小米文件选择控件
- 其他系统：使用Android原生文件选择器（ActivityResultContracts）

**参考资料**：
- 小米澎湃OS开发者平台：https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2220

### 影响范围
- DataManagementScreen.kt 文件选择逻辑需要重构
- 需要添加小米文件选择控件SDK依赖
- 需要实现兼容性检测和降级逻辑

### 兼容性说明
- 向后兼容，在不支持小米文件选择控件的设备上自动降级

---

## [2.0.0] - 2026-02-24

### 变更类型：重大更新

### 功能调整
- 功能 6：课程数据导入 - 完善需求细节，新增多种导入方式和预览功能
- 功能 17：课程数据导出 - 新增功能

### 功能详情

#### 功能 6：课程数据导入（完善）
**新增导入来源**：
- 本应用导出的CSV文件
- Wakeup课程表备份文件
- 凌展教务系统（账号密码登录，手动输入验证码）

**新增导入预览功能**：
- 显示解析出的课程列表
- 用户可勾选要导入的课程
- 冲突课程红色高亮标记
- 用户可编辑课程信息后再导入
- 用户选择导入到"我的课表"还是"Ta的课表"

**新增异常处理**：
- 文件格式错误提示
- 部分失败提示
- 重试机制
- 导入前自动备份

**优先级调整**：中 → 高

#### 功能 17：课程数据导出（新增）
**导出格式**：
- CSV完整格式，包含所有字段

**导出范围**：
- 仅Ta的课表
- 仅我的课表
- 双人课表一起
- 用户可选择

**导出内容**：
- 课程数据
- 课表参数设置
- 不包含敏感信息

**文件操作**：
- 选择保存位置
- 分享功能
- 导出成功提示

### 影响范围
- 数据管理页面需要重构，增加导入导出功能
- 需要新增CSV解析器
- 需要新增Wakeup数据格式解析器
- 需要新增凌展教务系统对接模块
- 需要新增导入预览页面
- 需要新增文件选择和分享功能

### 兼容性说明
- 向后兼容，现有数据保持不变

---

## [1.9.0] - 2026-02-24

### 变更类型：新增

### 新增功能
- 功能 16：点击格子自动选择星期和节次

### 功能详情
- 在课表页面点击空白格子添加课程时，自动根据点击位置预选星期和节次
- 点击课表网格的空白格子时，获取该格子对应的星期（1-7）和节次（1-N）
- 导航到课程编辑页面时，传递星期和节次参数
- 课程编辑页面接收参数后，自动选中对应的星期和节次
- 如果从顶部添加按钮进入，不预选任何星期和节次

### 影响范围
- 课程编辑页面需要支持接收初始星期和节次参数
- 导航路由需要支持传递星期和节次参数
- 课表页面需要传递点击位置信息

### 兼容性说明
- 向后兼容，不影响现有功能

---

## [1.8.0] - 2026-02-24

### 变更类型：修改

### 功能调整
- 功能 15：设置界面二级导航重构

### 功能详情
- 设置主页面改为分类入口列表，支持二级导航
- 用户设置排在课表设置前面，因为用户名称是基础配置
- 设置分类顺序调整为：
  1. 用户设置（优先显示）
  2. 课表设置
  3. 显示设置
  4. 数据管理
- 用户设置二级页面：Ta的名称、我的名称
- 课表设置二级页面：两人的开学时间、周数、节数、时间配置

### 影响范围
- 设置页面UI需要重构为二级导航结构
- 需要新增导航路由配置
- 需要拆分设置组件为多个二级页面

### 兼容性说明
- 向后兼容，现有设置数据保持不变

---

## [1.7.0] - 2026-02-23

### 变更类型：修改

### 功能调整
- 功能 1：双人课程表管理 - 上课时间改为课节选择

### 功能详情
- 编辑课程界面的上课时间从时间段选择改为课节选择
- 支持选择单节或多节课，如"第1-2节"
- 根据课表节数设置动态显示可选课节

### 影响范围
- 课程编辑页面时间选择UI需要重构
- 需要根据总节数设置动态显示课节数

### 兼容性说明
- 向后兼容，现有数据将自动转换为课节数

---

## [1.6.0] - 2026-02-23

### 变更类型：修改

### 功能调整
- 功能 1：双人课程表管理 - 周次选择交互优化

### 功能详情
- 周次选择改为点选框，根据学期总周数动态显示所有周数
- 点击周数表示该周上课，再次点击取消选择
- 支持滑动选择多周，再次滑动可取消选择

### 影响范围
- 课程编辑页面周次选择UI需要重构
- 需要支持动态周数显示（根据总周数设置）

### 兼容性说明
- 向后兼容，不影响现有数据

---

## [1.5.0] - 2026-02-23

### 变更类型：新增

### 新增功能
- 课表导航顺序调整：将"我的课表"放在"Ta的课表"前面
- 课表周数日期显示：在周数下方显示对应的日期范围
- 课表左侧节数与时间显示：左侧标注节数，显示对应时间

### 功能详情

#### 课表导航顺序调整
- 底部导航栏顺序调整为：主页 → 我的课表 → Ta的课表 → 设置
- "我的课表"排在前面，方便用户优先查看自己的课表

#### 课表周数日期显示
- 根据开学时间和当前周数自动计算每周各天的日期
- 显示格式：每天列头下方显示"月/日"，如周一下方显示"2/24"
- 周一至周日依次显示对应日期

#### 课表左侧节数与时间显示
- 左侧显示节数（如"第1节"、"第2节"）
- 节数下方或旁边显示对应时间（如"08:00-08:45"）
- 时间根据用户设置的课表时间配置显示

### 影响范围
- 底部导航栏顺序调整
- 课表页面UI布局调整
- 需要依赖课表参数设置功能（开学时间、课表时间）

### 兼容性说明
- 向后兼容，不影响现有功能

---

## [1.4.0] - 2026-02-23

### 变更类型：新增

### 新增功能
- 课表参数设置：支持为两个人分别设置开学时间、学期总周数、当前周数、课表节数和课表时间
- 非本周课程显示开关：控制是否在课表中显示非本周的课程

### 功能详情

#### 课表参数设置
- **开学时间设置**：两人分别设置各自的学期开学日期，用于自动计算当前周数
- **学期总周数设置**：两人分别设置各自的学期总周数（如16周、18周、20周）
- **当前周数设置**：两人分别设置当前所处周次，可手动调整或根据开学时间自动计算
- **课表节数设置**：两人分别设置每天的课表总节数（如12节、14节）
- **课表时间设置**：两人分别设置每节课的开始和结束时间

#### 非本周课程显示开关
- 开启时显示所有课程，非本周课程以灰色或透明度降低显示
- 关闭时仅显示当前周有课的课程
- 默认状态为关闭

### 业务规则
- 两人课表的开学时间、总周数、当前周数、节数和时间需分别独立设置
- 开学时间用于自动计算当前周数
- 学期总周数限制当前周数的最大值
- 课表节数决定课表网格的行数
- 非本周课程显示开关对两人课表同时生效

### 影响范围
- 设置页面需重构，增加大量配置项
- 数据存储层需扩展，支持两人独立的参数配置
- 课表显示逻辑需调整，支持非本周课程显示控制

### 兼容性说明
- 向后兼容，现有数据将迁移为默认值
- 现有的单一当前周数设置将迁移为两人共用同一周数

---

## [1.3.0] - 2026-02-23

### 变更类型：新增

### 新增功能
- Ta的课表页面：独立的页面展示Ta（人员A）的完整周课表，支持查看和编辑
- 我的课表页面：独立的页面展示我（人员B）的完整周课表，支持查看和编辑

### 功能详情
- 周课表视图：周一至周日的课程网格展示
- 支持切换周次查看
- 点击课程可进入编辑页面
- 点击空白时段可快速添加课程
- 区分单双周课程显示

### 影响范围
- 新增两个独立页面，需要更新导航结构
- 底部导航栏需要添加入口

### 兼容性说明
- 向后兼容，不影响现有功能

---

## [1.2.0] - 2026-02-23

### 变更类型：修改

### 功能调整
- 桌面小组件：新增小米负一屏（桌面助手）兼容性要求，需适配MIUI/HyperOS小组件规范

### 兼容性要求调整
- 新增：小组件支持小米负一屏添加

### 技术栈调整
- 小组件开发：新增小米负一屏适配要求

### 影响范围
- 小组件功能需额外适配小米MIUI/HyperOS规范

---

## [1.1.0] - 2026-02-23

### 变更类型：修改

### 技术栈调整
- 开发框架：Flutter → Android原生（Kotlin）
- 原因：用户要求改为Android原生开发，便于后续适配信息岛功能

### 具体变更
- 开发语言：Kotlin（Android官方推荐）
- 本地存储：Room + SharedPreferences
- 小组件：AppWidgetProvider + Jetpack Glance
- 状态管理：ViewModel + LiveData/Flow
- UI框架：Jetpack Compose + Material Design 3
- 依赖注入：Hilt

### 影响范围
- 全部开发工作将基于Android原生技术栈

### 兼容性说明
- 仅支持Android平台

---

## [1.0.0] - 2026-02-23

### 变更类型：新增

### 新增功能
- 双人课程表管理：支持本地存储和管理两个人的独立课程表，支持复杂周期设置
- 当前课程状态显示：实时显示两个人当前正在上的课程，包括课程名称和剩余时间
- 今日课程列表：显示两个人今天的完整课程列表
- 共同空闲时间：计算并显示两个人今天共同的空闲时间段
- 桌面小组件：提供桌面小组件，无需打开应用即可查看当前课程状态
- 课程数据导入：支持从Wakeup课程表、教务系统等多种来源导入课程数据
- 信息岛适配（后续版本）：适配Android原生信息岛功能

### 影响范围
- 全部功能为首次新增，无影响范围

### 兼容性说明
- 初始版本，无兼容性问题

---

## 版本号规范

遵循语义化版本规范：`MAJOR.MINOR.PATCH`

- **MAJOR（主版本号）**：不兼容的 API 修改或重大功能变更
- **MINOR（次版本号）**：向下兼容的功能性新增
- **PATCH（修订号）**：向下兼容的问题修正

---

**文档版本**：2.73.0

**最后更新**：2026-03-02
