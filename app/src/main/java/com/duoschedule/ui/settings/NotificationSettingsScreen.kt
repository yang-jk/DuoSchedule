package com.duoschedule.ui.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.notification.SilentModeType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAndroid15OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    val isAndroid12OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val reminderMinutesBefore by viewModel.reminderMinutesBefore.collectAsState()
    val liveNotificationEnabled by viewModel.liveNotificationEnabled.collectAsState()
    val autoSilentEnabled by viewModel.autoSilentEnabled.collectAsState()
    val autoSilentModeType by viewModel.autoSilentModeType.collectAsState()
    val autoSilentAdvanceTime by viewModel.autoSilentAdvanceTime.collectAsState()

    var showReminderDialog by remember { mutableStateOf(false) }
    var showSilentModeDialog by remember { mutableStateOf(false) }
    var showSilentAdvanceDialog by remember { mutableStateOf(false) }
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(false) }
    var canScheduleExactAlarms by remember { mutableStateOf(true) }
    var hasNotificationPolicyAccess by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(true) }
    var previousPolicyAccess by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasNotificationPermission = true
            viewModel.setNotificationEnabled(true)
            viewModel.rescheduleNotifications()
            Toast.makeText(context, "通知权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            hasNotificationPermission = false
            viewModel.setNotificationEnabled(false)
            Toast.makeText(context, "通知权限被拒绝，无法发送通知", Toast.LENGTH_LONG).show()
        }
    }

    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasNotificationPermission = true
                    viewModel.setNotificationEnabled(true)
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            hasNotificationPermission = true
            viewModel.setNotificationEnabled(true)
        }
    }

    val labelsPrimary = getLabelsVibrantPrimary()

    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        if (isAndroid12OrAbove) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        hasNotificationPolicyAccess = notificationManager.isNotificationPolicyAccessGranted
        previousPolicyAccess = hasNotificationPolicyAccess
        
        hasNotificationPermission = checkNotificationPermission()
    }
    
    LaunchedEffect(hasNotificationPolicyAccess) {
        if (hasNotificationPolicyAccess && !previousPolicyAccess) {
            Log.d("NotificationSettings", "勿扰模式权限已授予，重新调度静音任务")
            viewModel.rescheduleAutoSilent()
            previousPolicyAccess = true
        }
    }
    
    DisposableEffect(Unit) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val checkRunnable = object : Runnable {
            override fun run() {
                val currentAccess = notificationManager.isNotificationPolicyAccessGranted
                if (currentAccess != hasNotificationPolicyAccess) {
                    hasNotificationPolicyAccess = currentAccess
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(checkRunnable)
        onDispose {
            handler.removeCallbacks(checkRunnable)
        }
    }
    
    val rescheduleResult by viewModel.rescheduleResult.collectAsState()
    LaunchedEffect(rescheduleResult) {
        rescheduleResult?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearRescheduleResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "通知设置",
                        color = labelsPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) 
                },
                navigationIcon = {
                    GlassSymbolIconButton(
                        onClick = onNavigateBack,
                        style = GlassSymbolButtonStyle.NonTinted,
                        size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize,
                        contentPadding = PaddingValues(start = Spacing.sm)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = labelsPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingsSection(title = "通知开关") {
                SettingsToggleRow(
                    title = "启用通知",
                    subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        "需要授予通知权限"
                    } else {
                        "开启后将收到课前提醒通知"
                    },
                    icon = Icons.Outlined.Notifications,
                    iconBackgroundColor = IOSColors.Red,
                    checked = notificationEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermissionIfNeeded()
                        } else {
                            viewModel.setNotificationEnabled(false)
                        }
                    }
                )
            }

            if (notificationEnabled && hasNotificationPermission) {
                SettingsSection(title = "提醒设置") {
                    SettingsNavigationRow(
                        title = "课前提醒时间",
                        subtitle = "课程开始前多久提醒",
                        icon = Icons.Outlined.Timer,
                        iconBackgroundColor = IOSColors.Orange,
                        value = "${reminderMinutesBefore} 分钟",
                        onClick = { showReminderDialog = true }
                    )
                }

                if (isAndroid15OrAbove) {
                    SettingsSection(title = "实况通知") {
                        SettingsToggleRow(
                            title = "实况通知",
                            subtitle = "在状态栏显示当前课程信息（Android 15+）",
                            icon = Icons.Outlined.LiveTv,
                            iconBackgroundColor = IOSColors.Purple,
                            checked = liveNotificationEnabled,
                            onCheckedChange = { viewModel.setLiveNotificationEnabled(it) }
                        )
                    }
                }

                SettingsSection(title = "上课自动静音") {
                    SettingsToggleRow(
                        title = "自动静音",
                        subtitle = "上课时自动切换为静音/振动模式",
                        icon = Icons.Outlined.VolumeOff,
                        iconBackgroundColor = IOSColors.Blue,
                        checked = autoSilentEnabled,
                        onCheckedChange = { viewModel.setAutoSilentEnabled(it) }
                    )
                    
                    if (autoSilentEnabled) {
                        val currentModeType = try {
                            SilentModeType.valueOf(autoSilentModeType)
                        } catch (e: Exception) {
                            SilentModeType.VIBRATE
                        }
                        
                        SettingsNavigationRow(
                            title = "静音提前时间",
                            subtitle = "课程开始前多久进入静音模式",
                            icon = Icons.Outlined.Schedule,
                            iconBackgroundColor = IOSColors.Green,
                            value = "${autoSilentAdvanceTime} 分钟",
                            onClick = { showSilentAdvanceDialog = true }
                        )
                        
                        SettingsNavigationRow(
                            title = "静音模式",
                            subtitle = "选择上课时的静音方式",
                            icon = Icons.Outlined.PhonelinkRing,
                            iconBackgroundColor = IOSColors.Teal,
                            value = currentModeType.displayName,
                            onClick = { showSilentModeDialog = true }
                        )
                        
                        if (!hasNotificationPolicyAccess) {
                            SettingsNavigationRow(
                                title = "勿扰模式权限",
                                subtitle = "需要授权才能自动切换静音模式",
                                icon = Icons.Outlined.AdminPanelSettings,
                                iconBackgroundColor = IOSColors.Red,
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = "package:${context.packageName}".toUri()
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "权限管理") {
                SettingsNavigationRow(
                    title = "通知权限",
                    subtitle = if (hasNotificationPermission) "已授予" else "未授予，点击授权",
                    icon = Icons.Outlined.Settings,
                    iconBackgroundColor = if (hasNotificationPermission) IOSColors.Green else IOSColors.Red,
                    onClick = { viewModel.openNotificationSettings(context) }
                )

                if (!isIgnoringBatteryOptimizations) {
                    SettingsNavigationRow(
                        title = "电池优化",
                        subtitle = "⚠️ 未关闭电池优化可能导致通知延迟",
                        icon = Icons.Outlined.BatterySaver,
                        iconBackgroundColor = IOSColors.Orange,
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                } else {
                    SettingsNavigationRow(
                        title = "电池优化",
                        subtitle = "✓ 已关闭，通知可准时送达",
                        icon = Icons.Outlined.BatterySaver,
                        iconBackgroundColor = IOSColors.Green,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                val isXiaomiDevice = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                                     Build.MANUFACTURER.equals("Redmi", ignoreCase = true)
                if (isXiaomiDevice) {
                    SettingsNavigationRow(
                        title = "自启动权限（小米）",
                        subtitle = "⚠️ 必须开启才能收到通知",
                        icon = Icons.Outlined.Start,
                        iconBackgroundColor = IOSColors.Red,
                        onClick = {
                            try {
                                val intent = Intent().apply {
                                    action = "miui.intent.action.OP_AUTO_START"
                                    addCategory(Intent.CATEGORY_DEFAULT)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS").apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    )
                    
                    SettingsNavigationRow(
                        title = "后台运行权限（小米）",
                        subtitle = "允许应用在后台运行",
                        icon = Icons.Outlined.AppSettingsAlt,
                        iconBackgroundColor = IOSColors.Blue,
                        onClick = {
                            try {
                                val intent = Intent("miui.intent.action.PER_MIUI_APP_SETTINGS").apply {
                                    putExtra("package", context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                if (isAndroid12OrAbove && !canScheduleExactAlarms) {
                    SettingsNavigationRow(
                        title = "精确闹钟权限",
                        subtitle = "⚠️ 未开启可能导致通知延迟几分钟",
                        icon = Icons.Outlined.Alarm,
                        iconBackgroundColor = IOSColors.Orange,
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                } else if (isAndroid12OrAbove && canScheduleExactAlarms) {
                    SettingsNavigationRow(
                        title = "精确闹钟权限",
                        subtitle = "✓ 已开启，通知将准时送达",
                        icon = Icons.Outlined.Alarm,
                        iconBackgroundColor = IOSColors.Green,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            SettingsSection(title = "调试测试") {
                SettingsNavigationRow(
                    title = "测试通知",
                    subtitle = "立即发送一条测试通知",
                    icon = Icons.Outlined.NotificationsActive,
                    iconBackgroundColor = IOSColors.Orange,
                    onClick = { viewModel.testNotification() }
                )
                
                SettingsNavigationRow(
                    title = "测试上课中通知",
                    subtitle = "立即启动上课中实况通知",
                    icon = Icons.Outlined.PlayCircle,
                    iconBackgroundColor = IOSColors.Purple,
                    onClick = { viewModel.testOngoingNotification(context) }
                )
                
                SettingsNavigationRow(
                    title = "重新调度通知",
                    subtitle = "手动触发通知调度",
                    icon = Icons.Outlined.Refresh,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = { viewModel.rescheduleNotifications() }
                )
            }

            if (notificationEnabled) {
                val isXiaomiDevice = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                                     Build.MANUFACTURER.equals("Redmi", ignoreCase = true)
                val allPermissionsOk = isIgnoringBatteryOptimizations && 
                                       canScheduleExactAlarms && 
                                       (!isXiaomiDevice)
                
                SettingsSection(title = if (allPermissionsOk) "✅ 通知设置正常" else "⚠️ 通知可靠性提示") {
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        if (!isIgnoringBatteryOptimizations) {
                            Text(
                                text = "• ⚠️ 电池优化未关闭 → 通知可能被延迟",
                                style = MaterialTheme.typography.bodySmall,
                                color = IOSColors.Orange
                            )
                        }
                        if (isXiaomiDevice) {
                            Text(
                                text = "• ⚠️ 小米设备必须开启自启动权限",
                                style = MaterialTheme.typography.bodySmall,
                                color = IOSColors.Orange
                            )
                        }
                        if (isAndroid12OrAbove && !canScheduleExactAlarms) {
                            Text(
                                text = "• ⚠️ 精确闹钟权限未开启 → 通知可能延迟几分钟",
                                style = MaterialTheme.typography.bodySmall,
                                color = IOSColors.Orange
                            )
                        }
                        
                        if (allPermissionsOk) {
                            Text(
                                text = "✅ 所有权限已正确配置，通知将准时送达",
                                style = MaterialTheme.typography.bodySmall,
                                color = IOSColors.Green
                            )
                        }
                        
                        Text(
                            text = "• 设备重启后会自动重新调度通知",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• 请勿在系统设置中强制停止应用",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (autoSilentEnabled && !hasNotificationPolicyAccess) {
                            Text(
                                text = "• 自动静音需要勿扰模式权限",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            SettingsFooter(
                text = if (notificationEnabled) {
                    "通知将在每节课开始前 $reminderMinutesBefore 分钟提醒您。"
                } else {
                    "开启通知后，您将在课程开始前收到提醒。"
                }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showReminderDialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        SettingsOptionDialog(
            title = "课前提醒时间",
            options = listOf(5, 10, 15, 20, 30),
            selectedOption = reminderMinutesBefore,
            backdrop = backdrop,
            optionLabel = { "${it} 分钟" },
            onDismiss = { showReminderDialog = false },
            onConfirm = { minutes ->
                viewModel.setReminderMinutesBefore(minutes)
                showReminderDialog = false
            }
        )
    }

    if (showSilentModeDialog) {
        val currentModeType = try {
            SilentModeType.valueOf(autoSilentModeType)
        } catch (e: Exception) {
            SilentModeType.VIBRATE
        }
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        
        SettingsOptionDialog(
            title = "静音模式",
            options = SilentModeType.entries.toList(),
            selectedOption = currentModeType,
            backdrop = backdrop,
            optionLabel = { it.displayName },
            onDismiss = { showSilentModeDialog = false },
            onConfirm = { modeType ->
                viewModel.setAutoSilentModeType(modeType)
                showSilentModeDialog = false
            }
        )
    }

    if (showSilentAdvanceDialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        SettingsOptionDialog(
            title = "静音提前时间",
            options = listOf(1, 2, 3, 5, 10),
            selectedOption = autoSilentAdvanceTime,
            backdrop = backdrop,
            optionLabel = { "${it} 分钟" },
            onDismiss = { showSilentAdvanceDialog = false },
            onConfirm = { minutes ->
                viewModel.setAutoSilentAdvanceTime(minutes)
                showSilentAdvanceDialog = false
            }
        )
    }
}
