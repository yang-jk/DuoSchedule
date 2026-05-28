package com.duoschedule.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPeriodTimes: (PersonType) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val personASemesterStart by viewModel.personASemesterStart.collectAsState()
    val personBSemesterStart by viewModel.personBSemesterStart.collectAsState()
    val personATotalWeeks by viewModel.personATotalWeeks.collectAsState()
    val personBTotalWeeks by viewModel.personBTotalWeeks.collectAsState()
    val personACurrentWeek by viewModel.personACurrentWeek.collectAsState()
    val personBCurrentWeek by viewModel.personBCurrentWeek.collectAsState()
    val personAPeriodsPerDay by viewModel.personAPeriodsPerDay.collectAsState()
    val personBPeriodsPerDay by viewModel.personBPeriodsPerDay.collectAsState()
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()

    var showPersonASemesterStartDialog by remember { mutableStateOf(false) }
    var showPersonBSemesterStartDialog by remember { mutableStateOf(false) }
    var showPersonATotalWeeksDialog by remember { mutableStateOf(false) }
    var showPersonBTotalWeeksDialog by remember { mutableStateOf(false) }
    var showPersonACurrentWeekDialog by remember { mutableStateOf(false) }
    var showPersonBCurrentWeekDialog by remember { mutableStateOf(false) }
    var showPersonAPeriodsDialog by remember { mutableStateOf(false) }
    var showPersonBPeriodsDialog by remember { mutableStateOf(false) }

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
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled) {
                SmallTopAppBar(
                    title = "课表设置",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.iOS26.groupSpacing)
            ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            SettingsSection(title = if (singleModeEnabled) "课表设置" else "我的课表设置") {
                SettingsValueRow(
                    title = "开学时间",
                    value = personASemesterStart?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "未设置",
                    onClick = { showPersonASemesterStartDialog = true }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsValueRow(
                    title = "学期总周数",
                    value = "${personATotalWeeks} 周",
                    onClick = { showPersonATotalWeeksDialog = true }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsValueRow(
                    title = "当前周次",
                    value = "第 ${personACurrentWeek} 周",
                    onClick = { showPersonACurrentWeekDialog = true }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsValueRow(
                    title = "每天节数",
                    value = "${personAPeriodsPerDay} 节",
                    onClick = { showPersonAPeriodsDialog = true }
                )
                
                Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                
                SettingsNavigationRow(
                    title = "时间设置",
                    subtitle = "设置每节课的开始和结束时间",
                    icon = Icons.Outlined.Schedule,
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = { onNavigateToPeriodTimes(PersonType.PERSON_A) }
                )
            }

            if (!singleModeEnabled) {
                SettingsSection(title = "Ta的课表设置") {
                    SettingsValueRow(
                        title = "开学时间",
                        value = personBSemesterStart?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "未设置",
                        onClick = { showPersonBSemesterStartDialog = true }
                    )
                    
                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                    
                    SettingsValueRow(
                        title = "学期总周数",
                        value = "${personBTotalWeeks} 周",
                        onClick = { showPersonBTotalWeeksDialog = true }
                    )
                    
                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                    
                    SettingsValueRow(
                        title = "当前周次",
                        value = "第 ${personBCurrentWeek} 周",
                        onClick = { showPersonBCurrentWeekDialog = true }
                    )
                    
                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                    
                    SettingsValueRow(
                        title = "每天节数",
                        value = "${personBPeriodsPerDay} 节",
                        onClick = { showPersonBPeriodsDialog = true }
                    )
                    
                    Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                    
                    SettingsNavigationRow(
                        title = "时间设置",
                        subtitle = "设置每节课的开始和结束时间",
                        icon = Icons.Outlined.Schedule,
                        iconBackgroundColor = BrandColors.PersonB,
                        onClick = { onNavigateToPeriodTimes(PersonType.PERSON_B) }
                    )
                }
            }

            SettingsFooter(
                text = if (singleModeEnabled) "课表设置用于配置学期信息和显示方式。" else "课表设置用于配置学期信息和显示方式。开学时间用于计算当前周次。"
            )

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (showPersonBSemesterStartDialog) {
        DatePickerDialog(
            title = "选择开学时间",
            initialDate = personBSemesterStart ?: LocalDate.now(),
            onDismiss = { showPersonBSemesterStartDialog = false },
            onConfirm = { date ->
                viewModel.setPersonSemesterStart(PersonType.PERSON_B, date)
                showPersonBSemesterStartDialog = false
            }
        )
    }

    if (showPersonASemesterStartDialog) {
        DatePickerDialog(
            title = "选择开学时间",
            initialDate = personASemesterStart ?: LocalDate.now(),
            onDismiss = { showPersonASemesterStartDialog = false },
            onConfirm = { date ->
                viewModel.setPersonSemesterStart(PersonType.PERSON_A, date)
                showPersonASemesterStartDialog = false
            }
        )
    }

    if (showPersonBTotalWeeksDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "学期总周数",
            label = "请输入学期总周数",
            initialValue = personBTotalWeeks,
            onDismiss = { showPersonBTotalWeeksDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonTotalWeeks(PersonType.PERSON_B, value)
                showPersonBTotalWeeksDialog = false
            },
            range = 1..30
        )
    }

    if (showPersonATotalWeeksDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "学期总周数",
            label = "请输入学期总周数",
            initialValue = personATotalWeeks,
            onDismiss = { showPersonATotalWeeksDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonTotalWeeks(PersonType.PERSON_A, value)
                showPersonATotalWeeksDialog = false
            },
            range = 1..30
        )
    }

    if (showPersonBCurrentWeekDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "当前周次",
            label = "请输入当前周次",
            initialValue = personBCurrentWeek,
            onDismiss = { showPersonBCurrentWeekDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonCurrentWeek(PersonType.PERSON_B, value)
                showPersonBCurrentWeekDialog = false
            },
            range = 1..personBTotalWeeks
        )
    }

    if (showPersonACurrentWeekDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "当前周次",
            label = "请输入当前周次",
            initialValue = personACurrentWeek,
            onDismiss = { showPersonACurrentWeekDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonCurrentWeek(PersonType.PERSON_A, value)
                showPersonACurrentWeekDialog = false
            },
            range = 1..personATotalWeeks
        )
    }

    if (showPersonBPeriodsDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "每天节数",
            label = "请输入每天节数",
            initialValue = personBPeriodsPerDay,
            onDismiss = { showPersonBPeriodsDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonPeriodsPerDay(PersonType.PERSON_B, value)
                showPersonBPeriodsDialog = false
            },
            range = 1..20
        )
    }

    if (showPersonAPeriodsDialog) {
        val dialogBackdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = dialogBackdrop,
            title = "每天节数",
            label = "请输入每天节数",
            initialValue = personAPeriodsPerDay,
            onDismiss = { showPersonAPeriodsDialog = false },
            onConfirm = { value: Int ->
                viewModel.setPersonPeriodsPerDay(PersonType.PERSON_A, value)
                showPersonAPeriodsDialog = false
            },
            range = 1..20
        )
    }
}

@Composable
private fun DatePickerDialog(
    title: String,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            LiquidGlassButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onConfirm(selectedDate)
                    }
                },
                text = "确定",
                style = LiquidGlassButtonStyle.Tinted
            )
        },
        dismissButton = {
            LiquidGlassButton(
                onClick = onDismiss,
                text = "取消",
                style = LiquidGlassButtonStyle.NonTinted
            )
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
