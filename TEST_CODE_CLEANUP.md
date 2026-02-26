# 测试代码清理指南

此文件记录了用于测试通知和岛功能的临时代码，测试完成后需要删除。

## 需要删除的文件

### 1. NotificationTestHelper.kt
**路径**: `app/src/main/java/com/duoschedule/notification/NotificationTestHelper.kt`

**说明**: 通知测试工具类，包含以下测试方法：
- `testReminderNotification()` - 测试课前提醒通知
- `testOngoingNotification()` - 测试单人上课中通知
- `testDualOngoingNotification()` - 测试双人同时上课通知
- `testIslandNotification()` - 测试单人岛通知（小米设备）
- `testDualIslandNotification()` - 测试双人岛通知（小米设备）
- `cancelAllTestNotifications()` - 清除所有测试通知

**操作**: 删除整个文件

---

## 需要修改的文件

### 2. SettingsViewModel.kt
**路径**: `app/src/main/java/com/duoschedule/ui/settings/SettingsViewModel.kt`

**需要修改的内容**:

1. 删除 import 语句：
```kotlin
import com.duoschedule.notification.NotificationTestHelper
```

2. 修改构造函数，删除 `testHelper` 参数：
```kotlin
// 当前代码
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CourseRepository,
    val testHelper: NotificationTestHelper
) : ViewModel() {

// 改回
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {
```

---

### 3. NotificationSettingsScreen.kt
**路径**: `app/src/main/java/com/duoschedule/ui/settings/NotificationSettingsScreen.kt`

**需要修改的内容**:

1. 删除 import 语句：
```kotlin
import com.duoschedule.notification.NotificationTestHelper
```

2. 修改函数签名，删除 `testHelper` 参数：
```kotlin
// 当前代码
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    testHelper: NotificationTestHelper = hiltViewModel<SettingsViewModel>().testHelper
) {

// 改回
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
```

3. 删除 `LaunchedEffect` 块：
```kotlin
// 删除这段代码
LaunchedEffect(Unit) {
    testHelper.createTestChannels()
}
```

4. 删除 `NotificationTestSection` 调用：
```kotlin
// 删除这行
NotificationTestSection(testHelper = testHelper)
```

5. 删除整个 `NotificationTestSection` 函数（文件末尾约120行代码）

---

## 清理命令（可选）

如果使用 Git，可以查看修改：
```bash
git diff app/src/main/java/com/duoschedule/notification/
git diff app/src/main/java/com/duoschedule/ui/settings/
```

恢复原始文件：
```bash
git checkout -- app/src/main/java/com/duoschedule/ui/settings/SettingsViewModel.kt
git checkout -- app/src/main/java/com/duoschedule/ui/settings/NotificationSettingsScreen.kt
rm app/src/main/java/com/duoschedule/notification/NotificationTestHelper.kt
```

---

## 测试功能说明

测试区域位于：**设置 → 通知设置 → 页面底部**

### 普通通知测试
- **提醒通知(我)**: 发送一条"我"的课前提醒通知
- **提醒通知(Ta)**: 发送一条"Ta"的课前提醒通知
- **上课中(我)**: 发送一条"我"正在上课的持续通知
- **双人上课**: 发送两人同时在上课的通知

### 岛通知测试（小米设备）
- **岛通知(我)**: 发送带岛参数的通知（小米设备会显示为岛样式）
- **双人岛通知**: 发送双人同时上课的岛通知

### 清除通知
- **清除所有测试通知**: 取消所有测试通知

---

**创建日期**: 2026-02-25
**用途**: 真机测试通知和岛功能
