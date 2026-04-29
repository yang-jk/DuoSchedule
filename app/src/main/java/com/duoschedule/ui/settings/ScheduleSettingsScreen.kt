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
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
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

    var showPersonASemesterStartDialog by remember { mutableStateOf(false) }
    var showPersonBSemesterStartDialog by remember { mutableStateOf(false) }
    var showPersonATotalWeeksDialog by remember { mutableStateOf(false) }
    var showPersonBTotalWeeksDialog by remember { mutableStateOf(false) }
    var showPersonACurrentWeekDialog by remember { mutableStateOf(false) }
    var showPersonBCurrentWeekDialog by remember { mutableStateOf(false) }
    var showPersonAPeriodsDialog by remember { mutableStateOf(false) }
    var showPersonBPeriodsDialog by remember { mutableStateOf(false) }

    val labelsPrimary = getLabelsVibrantPrimary()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "课表设置",
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

            SettingsSection(title = "我的课表设置") {
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
                    iconBackgroundColor = IOSColors.Blue,
                    onClick = { onNavigateToPeriodTimes(PersonType.PERSON_B) }
                )
            }

            SettingsSection(title = "Ta的课表设置") {
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
                    iconBackgroundColor = BrandColors.PersonA,
                    onClick = { onNavigateToPeriodTimes(PersonType.PERSON_A) }
                )
            }

            SettingsFooter(
                text = "课表设置用于配置学期信息和显示方式。开学时间用于计算当前周次。"
            )

            Spacer(modifier = Modifier.weight(1f))
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
        NumberInputAlert(
            backdrop = backdrop,
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
    val labelsPrimary = getLabelsVibrantPrimary()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
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
