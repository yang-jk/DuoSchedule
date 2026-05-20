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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.ScrollTopBlurOverlay
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScheduleSettings: () -> Unit,
    onNavigateToDisplaySettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val predictiveBackEnabled by viewModel.predictiveBackEnabled.collectAsState()
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val reminderMinutesBefore by viewModel.reminderMinutesBefore.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

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
    val labelsTertiary = getLabelsVibrantTertiary()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
    val scrollBackdrop = rememberLayerBackdrop()
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .layerBackdrop(scrollBackdrop),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = labelsPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.lg,
                        bottom = Spacing.xs
                    )
            )

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
            }

            SettingsSection(title = "关于") {
                SettingsNavigationRow(
                    title = "关于 双人课程表",
                    subtitle = "版本 $versionName",
                    icon = Icons.Outlined.Info,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
            }

            ScrollTopBlurOverlay(backdrop = scrollBackdrop, scrollOffset = scrollState.value)
        }
    }

    if (showPersonADialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        TextInputAlert(
            backdrop = backdrop,
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

    if (showPersonBDialog) {
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        TextInputAlert(
            backdrop = backdrop,
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

private fun getThemeModeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色模式"
        ThemeMode.DARK -> "深色模式"
    }
}
