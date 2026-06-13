package com.duoschedule.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun SettingsScreen(
    onNavigateToScheduleSettings: () -> Unit,
    onNavigateToDisplaySettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToSyncSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToLogViewer: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val predictiveBackEnabled by viewModel.predictiveBackEnabled.collectAsState()
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val reminderMinutesBefore by viewModel.reminderMinutesBefore.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()

    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "3.4.2"
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.setNotificationEnabled(true)
            viewModel.rescheduleNotifications()
            Toast.makeText(context, "通知权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setNotificationEnabled(false)
            Toast.makeText(context, "通知权限被拒绝，无法发送通知", Toast.LENGTH_LONG).show()
        }
    }

    var showPersonADialog by remember { mutableStateOf(false) }
    var showPersonBDialog by remember { mutableStateOf(false) }

    val labelsPrimary = getLabelsVibrantPrimary()

    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val blurEnabled = scrollState.value > 0

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(
                    if (appThemeMode == AppThemeMode.MIUIX) Spacing.lg else Spacing.iOS26.groupSpacing
                )
            ) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "设置",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MiuixTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg)
                        .padding(top = Spacing.sm)
                )
            } else {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = labelsPrimary,
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg)
                )
            }

            SettingsSection(title = "用户与身份") {
                SettingsToggleRow(
                    title = "单人模式",
                    checked = singleModeEnabled,
                    onCheckedChange = remember { { viewModel.setSingleModeEnabled(it) } },
                    subtitle = if (singleModeEnabled) "仅显示一个人的课表" else "同时显示两个人的课表",
                    icon = Icons.Outlined.PersonOff,
                    iconBackgroundColor = IOSColors.Purple
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsNavigationRow(
                    title = "我的名称",
                    value = personAName,
                    icon = Icons.Outlined.Person,
                    iconBackgroundColor = BrandColors.PersonA,
                    onClick = { showPersonADialog = true }
                )

                if (!singleModeEnabled) {
                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                    SettingsNavigationRow(
                        title = "Ta的名称",
                        value = personBName,
                        icon = Icons.Outlined.Person,
                        iconBackgroundColor = BrandColors.PersonB,
                        onClick = { showPersonBDialog = true }
                    )
                }
            }

            SettingsSection(title = "课表参数") {
                SettingsNavigationRow(
                    title = "课表设置",
                    subtitle = if (singleModeEnabled) "课表参数配置" else "我和Ta的课表参数配置",
                    icon = Icons.Outlined.Schedule,
                    iconBackgroundColor = IOSColors.Orange,
                    onClick = onNavigateToScheduleSettings
                )
            }

            SettingsSection(title = "外观与显示") {
                SettingsMenuRow(
                    title = "主题模式",
                    icon = Icons.Outlined.Palette,
                    iconBackgroundColor = IOSColors.Indigo,
                    selectedOption = getThemeModeLabel(themeMode),
                    options = ThemeMode.entries.map { getThemeModeLabel(it) },
                    onOptionSelected = remember { { selected: String ->
                        val mode = ThemeMode.entries.find { getThemeModeLabel(it) == selected }
                        mode?.let { viewModel.setThemeMode(it) }
                    } },
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsMenuRow(
                    title = "界面风格",
                    icon = Icons.Outlined.Brush,
                    iconBackgroundColor = IOSColors.Indigo,
                    selectedOption = appThemeMode.displayName,
                    options = AppThemeMode.entries.map { it.displayName },
                    onOptionSelected = { displayName ->
                        val mode = AppThemeMode.entries.find { it.displayName == displayName } ?: AppThemeMode.IOS
                        viewModel.setAppThemeMode(mode)
                    }
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsNavigationRow(
                    title = "显示设置",
                    subtitle = "课表外观和字体设置",
                    icon = Icons.Outlined.DisplaySettings,
                    iconBackgroundColor = IOSColors.Indigo,
                    onClick = onNavigateToDisplaySettings
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsToggleRow(
                    title = "预测式返回",
                    checked = predictiveBackEnabled,
                    onCheckedChange = remember { { viewModel.setPredictiveBackEnabled(it) } },
                    subtitle = "返回时显示页面过渡动画预览（实验性功能）",
                    icon = Icons.Outlined.TouchApp,
                    iconBackgroundColor = IOSColors.Blue
                )
            }

            SettingsSection(title = "通知与提醒") {
                SettingsToggleRow(
                    title = "启用通知",
                    checked = notificationEnabled && hasNotificationPermission,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    hasNotificationPermission = true
                                    viewModel.setNotificationEnabled(true)
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                hasNotificationPermission = true
                                viewModel.setNotificationEnabled(true)
                            }
                        } else {
                            viewModel.setNotificationEnabled(false)
                        }
                    },
                    subtitle = if (notificationEnabled && hasNotificationPermission) "开启后将收到课前提醒通知" else if (notificationEnabled && !hasNotificationPermission) "需要授予通知权限" else "通知已关闭",
                    icon = Icons.Outlined.Notifications,
                    iconBackgroundColor = IOSColors.Red
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsNavigationRow(
                    title = "通知设置",
                    subtitle = if (notificationEnabled && hasNotificationPermission) "课前${reminderMinutesBefore}分钟提醒" else "已关闭",
                    icon = Icons.Outlined.NotificationsActive,
                    iconBackgroundColor = IOSColors.Orange,
                    onClick = onNavigateToNotificationSettings
                )
            }

            SettingsSection(title = "数据") {
                SettingsNavigationRow(
                    title = "数据管理",
                    subtitle = "导入导出课表数据",
                    icon = Icons.Outlined.DataUsage,
                    iconBackgroundColor = IOSColors.Green,
                    onClick = onNavigateToDataManagement
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsNavigationRow(
                    title = "课表同步",
                    subtitle = "通过云盘同步双人课表",
                    icon = Icons.Outlined.Sync,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = onNavigateToSyncSettings
                )
            }

            SettingsSection(title = "关于") {
                SettingsNavigationRow(
                    title = "关于 双人课程表",
                    subtitle = "版本 $versionName",
                    icon = Icons.Outlined.Info,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = onNavigateToAbout
                )

                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

                SettingsNavigationRow(
                    title = "查看日志",
                    subtitle = "查看应用运行日志",
                    icon = Icons.Outlined.BugReport,
                    iconBackgroundColor = IOSColors.Gray,
                    onClick = onNavigateToLogViewer
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }
        }
    }

    if (showPersonADialog) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixTextInputDialog(
                title = "我的名称",
                initialValue = personAName,
                placeholder = "例如：小明",
                onDismiss = { showPersonADialog = false },
                onConfirm = {
                    viewModel.setPersonName(PersonType.PERSON_A, it)
                    showPersonADialog = false
                }
            )
        } else {
            val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
            TextInputAlert(
                backdrop = dialogBackdrop,
                title = "我的名称",
                label = "请输入我的名称",
                initialValue = personAName,
                onDismiss = { showPersonADialog = false },
                onConfirm = {
                    viewModel.setPersonName(PersonType.PERSON_A, it)
                    showPersonADialog = false
                },
                placeholder = "例如：小明"
            )
        }
    }

    if (showPersonBDialog) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            MiuixTextInputDialog(
                title = "Ta的名称",
                initialValue = personBName,
                placeholder = "例如：小红",
                onDismiss = { showPersonBDialog = false },
                onConfirm = {
                    viewModel.setPersonName(PersonType.PERSON_B, it)
                    showPersonBDialog = false
                }
            )
        } else {
            val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
            TextInputAlert(
                backdrop = dialogBackdrop,
                title = "Ta的名称",
                label = "请输入Ta的名称",
                initialValue = personBName,
                onDismiss = { showPersonBDialog = false },
                onConfirm = {
                    viewModel.setPersonName(PersonType.PERSON_B, it)
                    showPersonBDialog = false
                },
                placeholder = "例如：小红"
            )
        }
    }
}

@Composable
private fun MiuixTextInputDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    WindowDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss
    ) {
        TextField(
            value = value,
            onValueChange = { value = it },
            label = placeholder,
            useLabelAsPlaceholder = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors()
            ) {
                top.yukonga.miuix.kmp.basic.Text("取消")
            }
            Button(
                onClick = { onConfirm(value) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColorsPrimary(),
                enabled = value.isNotBlank()
            ) {
                top.yukonga.miuix.kmp.basic.Text("确定")
            }
        }
    }
}

private fun getThemeModeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色模式"
        ThemeMode.DARK -> "深色模式"
    }
}
