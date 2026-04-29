package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val todayCourseDisplayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val showSaturday by viewModel.showSaturday.collectAsState()
    val showSunday by viewModel.showSunday.collectAsState()
    val showNonCurrentWeek by viewModel.showNonCurrentWeek.collectAsState()
    val showGridSeparator by viewModel.showDashedBorder.collectAsState()
    val courseNameFontSize by viewModel.courseNameFontSize.collectAsState()
    val courseLocationFontSize by viewModel.courseLocationFontSize.collectAsState()

    val labelsPrimary = getLabelsVibrantPrimary()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "显示设置",
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

            SettingsSection(title = "外观") {
                SettingsMenuRow(
                    title = "主题模式",
                    subtitle = "选择应用的显示主题",
                    icon = Icons.Outlined.Palette,
                    iconBackgroundColor = IOSColors.Indigo,
                    selectedOption = getThemeModeLabel(themeMode),
                    options = ThemeMode.entries.map { getThemeModeLabel(it) },
                    onOptionSelected = { selected ->
                        val mode = ThemeMode.entries.find { getThemeModeLabel(it) == selected }
                        mode?.let { viewModel.setThemeMode(it) }
                    }
                )
            }

            SettingsSection(title = "主页显示") {
                SettingsMenuRow(
                    title = "今日课程显示",
                    subtitle = "选择今日课程显示模式",
                    icon = Icons.Outlined.Today,
                    iconBackgroundColor = IOSColors.Blue,
                    selectedOption = getTodayCourseDisplayModeLabel(todayCourseDisplayMode),
                    options = TodayCourseDisplayMode.entries.map { getTodayCourseDisplayModeLabel(it) },
                    onOptionSelected = { selected ->
                        val mode = TodayCourseDisplayMode.entries.find { getTodayCourseDisplayModeLabel(it) == selected }
                        mode?.let { viewModel.setTodayCourseDisplayMode(it) }
                    }
                )
            }

            SettingsSection(title = "课表外观") {
                SettingsToggleRow(
                    title = "显示网格线分隔",
                    subtitle = "在课表格子之间显示横线和竖线分隔",
                    icon = Icons.Outlined.GridView,
                    iconBackgroundColor = IOSColors.Purple,
                    checked = showGridSeparator,
                    onCheckedChange = { viewModel.setShowDashedBorder(it) }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsToggleRow(
                    title = "显示周六",
                    subtitle = "在课表中显示周六的课程",
                    icon = Icons.Outlined.CalendarMonth,
                    iconBackgroundColor = IOSColors.Green,
                    checked = showSaturday,
                    onCheckedChange = { viewModel.setShowSaturday(it) }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsToggleRow(
                    title = "显示周日",
                    subtitle = "在课表中显示周日的课程",
                    icon = Icons.Outlined.CalendarMonth,
                    iconBackgroundColor = IOSColors.Green,
                    checked = showSunday,
                    onCheckedChange = { viewModel.setShowSunday(it) }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsToggleRow(
                    title = "显示非本周课程",
                    subtitle = "显示不在当前周的课程（灰色显示）",
                    icon = Icons.Outlined.DateRange,
                    iconBackgroundColor = IOSColors.Orange,
                    checked = showNonCurrentWeek,
                    onCheckedChange = { viewModel.setShowNonCurrentWeekCourses(it) }
                )
            }

            SettingsSection(title = "课表字体") {
                SettingsMenuRow(
                    title = "课程名称字号",
                    subtitle = "设置课表中课程名称的字体大小",
                    icon = Icons.Outlined.TextFields,
                    iconBackgroundColor = IOSColors.Blue,
                    selectedOption = "${courseNameFontSize}sp",
                    options = listOf("10sp", "11sp", "12sp", "13sp", "14sp", "15sp", "16sp"),
                    onOptionSelected = { selected ->
                        val size = selected.removeSuffix("sp").toIntOrNull()
                        size?.let { viewModel.setCourseNameFontSize(it) }
                    }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsMenuRow(
                    title = "上课地点字号",
                    subtitle = "设置课表中上课地点的字体大小",
                    icon = Icons.Outlined.TextFields,
                    iconBackgroundColor = IOSColors.Teal,
                    selectedOption = "${courseLocationFontSize}sp",
                    options = listOf("9sp", "10sp", "11sp", "12sp", "13sp", "14sp"),
                    onOptionSelected = { selected ->
                        val size = selected.removeSuffix("sp").toIntOrNull()
                        size?.let { viewModel.setCourseLocationFontSize(it) }
                    }
                )
            }

            SettingsFooter(
                text = "显示设置会影响课表和主页的显示方式。"
            )

            Spacer(modifier = Modifier.weight(1f))
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

private fun getTodayCourseDisplayModeLabel(mode: TodayCourseDisplayMode): String {
    return when (mode) {
        TodayCourseDisplayMode.SELF_ONLY -> "仅显示我的"
        TodayCourseDisplayMode.TA_ONLY -> "仅显示Ta的"
        TodayCourseDisplayMode.BOTH -> "同时显示"
    }
}
