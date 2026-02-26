# 适配 Android 16 Live Updates

Live Updates 是 Android 16 引入的一种新型通知，允许应用向用户展示实时变化的信息（例如体育比分、航班状态、打车进度等）。与普通通知不同，Live Updates 会在状态栏、锁屏和通知中心持续显示，并支持高频更新，同时优化了系统资源占用。

本指南将帮助你在应用中适配 Live Updates。

## 1. 前提条件

- 项目 `compileSdk` 和目标 `targetSdk` 均设为 **Android 16 (API 35)** 或更高版本。
- 使用 Android Studio 最新稳定版（建议 Giraffe 或更高）。
- 了解 [通知](https://developer.android.com/develop/ui/views/notifications) 的基本用法。

## 2. 添加依赖

Live Updates 依赖 Jetpack 库 `androidx.core:core` 版本 1.15+，请确保 `build.gradle` 中包含：

```groovy
dependencies {
    implementation "androidx.core:core:1.15.0"
}
```

如果使用 Kotlin，建议同时添加 `androidx.core:core-ktx` 扩展库。

## 3. 声明权限和组件

在 `AndroidManifest.xml` 中添加以下权限（从 Android 16 开始，发布 Live Updates 需要显式权限）：

```xml
<uses-permission android:name="android.permission.POST_LIVE_UPDATES" />
```

如果你的应用需要后台更新 Live Updates，还需声明前台服务权限：

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_TYPE_SPECIAL_USE" />
```

并在 `<application>` 内注册对应的前台服务（可选）：

```xml
<service
    android:name=".LiveUpdateService"
    android:foregroundServiceType="specialUse"
    android:exported="false" />
```

## 4. 创建 Live Update

Live Update 的创建与普通通知类似，但必须使用 `Notification.Builder` 并调用 `setLiveUpdate(true)` 将其标记为 Live Update。同时建议使用 `setCategory(Notification.CATEGORY_STATUS)` 和 `setVisibility(Notification.VISIBILITY_PUBLIC)`。

### Kotlin 示例

```kotlin
val builder = Notification.Builder(context, CHANNEL_ID)
    .setContentTitle("比赛进行中")
    .setContentText("主队 2 - 1 客队")
    .setSmallIcon(R.drawable.ic_sport)
    .setLiveUpdate(true)               // 关键标记
    .setCategory(Notification.CATEGORY_STATUS)
    .setVisibility(Notification.VISIBILITY_PUBLIC)
    .setOngoing(true)                   // 不可滑动删除，建议设置
    .setOnlyAlertOnce(true)              // 避免每次更新都震动/响铃

val notification = builder.build()
with(NotificationManagerCompat.from(context)) {
    notify(NOTIFICATION_ID, notification)
}
```

**注意**：`setLiveUpdate(true)` 仅在 Android 16+ 上生效，低版本系统会忽略该标记，因此你的代码应做好向下兼容。

### 通道配置

确保通知通道已创建，并设置适当的属性：

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "实时更新",
        NotificationManager.IMPORTANCE_LOW   // Live Updates 建议低重要性，避免频繁打扰
    ).apply {
        description = "接收比赛、航班等实时动态"
        setShowBadge(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    notificationManager.createNotificationChannel(channel)
}
```

## 5. 更新 Live Update

更新一个已有的 Live Update 非常简单：使用相同的 `NOTIFICATION_ID` 再次调用 `notify()`，并传入更新后的 `Notification` 对象。系统会自动刷新显示。

```kotlin
fun updateScore(home: Int, away: Int) {
    val updatedNotification = builder
        .setContentText("主队 $home - $away 客队")
        .build()
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, updatedNotification)
}
```

**性能建议**：Live Updates 设计用于高频更新，但请避免每秒超过 10 次更新，以免影响系统性能。如果需要非常高的更新频率（如动画），请考虑使用 `setLiveUpdate(true)` 配合自定义视图，并控制更新速率。

## 6. 移除 Live Update

当实时信息结束时，应主动取消 Live Update：

```kotlin
NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
```

如果 Live Update 与后台任务关联，请确保任务结束后取消通知，并停止前台服务（如果使用）。

## 7. 后台更新策略

Live Updates 通常需要后台持续更新。从 Android 16 开始，系统对后台启动的前台服务有限制。如果你的更新逻辑需要长时间在后台运行，建议：

- 使用 **前台服务**，并将服务类型声明为 `specialUse`（如上清单所示）。
- 在服务启动时，立即显示一个 Live Update 作为持续通知。
- 更新数据时通过 `notify()` 修改通知内容，服务本身可以持续运行直到任务完成。

示例服务框架：

```kotlin
class LiveUpdateService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createInitialNotification(), FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        // 开始实时数据获取并更新通知
        return START_STICKY
    }
    // ... 其他逻辑
}
```

## 8. 最佳实践

- **重要性**：使用 `IMPORTANCE_LOW` 避免过度打扰用户，同时确保 Live Update 能出现在状态栏。
- **唯一 ID**：为每个 Live Update 使用唯一的 `NOTIFICATION_ID`，以便独立管理。
- **内容简洁**：实时信息应保持精炼，避免大段文字；可搭配进度条或图标。
- **适应用户操作**：可以为 Live Update 添加 `PendingIntent` 来响应用户点击，跳转到应用内详情页面。
- **测试**：使用 Android 16 模拟器或真机测试更新效果，并检查系统资源占用。

## 9. 常见问题

**Q: 为什么 `setLiveUpdate(true)` 不生效？**  
A: 请确认 targetSdk 为 35+，且设备运行 Android 16 或以上。低版本系统会忽略该标记，通知将表现为普通通知。

**Q: Live Update 可以包含操作按钮吗？**  
A: 可以。通过 `addAction()` 添加按钮，用户点击后执行相应操作。

**Q: 如何避免每次更新都触发通知音？**  
A: 使用 `setOnlyAlertOnce(true)`，这样仅首次显示会提醒，后续更新不会打扰用户。

**Q: 系统会限制更新频率吗？**  
A: 系统会对高频更新的通知进行采样或降级，以确保用户体验。建议合理控制更新间隔，非必要不更新。

---

[Android 开发者官方文档](https://developer.android.com/develop/ui/views/notifications/live-updates)。