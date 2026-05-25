package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DisplaySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val todayCourseDisplayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val showSaturday by viewModel.showSaturday.collectAsState()
    val showSunday by viewModel.showSunday.collectAsState()
    val showNonCurrentWeek by viewModel.showNonCurrentWeek.collectAsState()
    val showGridSeparator by viewModel.showDashedBorder.collectAsState()
    val courseNameFontSize by viewModel.courseNameFontSize.collectAsState()
    val courseLocationFontSize by viewModel.courseLocationFontSize.collectAsState()

    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()
    val scrollProgress by remember { derivedStateOf { (scrollState.value.toFloat() / 300f).coerceIn(0f, 1f) } }
    val blurActive = scrollProgress >= 0.5f
    val barColor = if (blurActive) Color.Transparent else if (scrollProgress >= 0.5f) MiuixTheme.colorScheme.surface else Color.Transparent

    CompositionLocalProvider(LocalHazeState provides hazeState) {
    Scaffold(
        topBar = {
            BlurredBar(null, blurActive) {
                SmallTopAppBar(
                    title = "显示设置",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)),
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

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

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    }
}

private fun getTodayCourseDisplayModeLabel(mode: TodayCourseDisplayMode): String {
    return when (mode) {
        TodayCourseDisplayMode.SELF_ONLY -> "仅显示我的"
        TodayCourseDisplayMode.TA_ONLY -> "仅显示Ta的"
        TodayCourseDisplayMode.BOTH -> "同时显示"
    }
}
