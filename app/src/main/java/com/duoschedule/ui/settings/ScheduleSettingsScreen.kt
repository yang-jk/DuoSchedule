package com.duoschedule.ui.settings

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.NumberInputDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPeriodTimes: (PersonType, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()

    val personASemesterStartDate by viewModel.getSemesterStartDate(PersonType.PERSON_A).collectAsState()
    val personBSemesterStartDate by viewModel.getSemesterStartDate(PersonType.PERSON_B).collectAsState()
    val personATotalWeeks by viewModel.getTotalWeeks(PersonType.PERSON_A).collectAsState()
    val personBTotalWeeks by viewModel.getTotalWeeks(PersonType.PERSON_B).collectAsState()
    val personACurrentWeek by viewModel.getCurrentWeek(PersonType.PERSON_A).collectAsState()
    val personBCurrentWeek by viewModel.getCurrentWeek(PersonType.PERSON_B).collectAsState()
    val personATotalPeriods by viewModel.getTotalPeriods(PersonType.PERSON_A).collectAsState()
    val personBTotalPeriods by viewModel.getTotalPeriods(PersonType.PERSON_B).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课表设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 我的课表设置
            PersonSettingsSection(
                title = "${personBName}的课表设置",
                personType = PersonType.PERSON_B,
                personName = personBName,
                semesterStartDate = personBSemesterStartDate,
                totalWeeks = personBTotalWeeks,
                currentWeek = personBCurrentWeek,
                totalPeriods = personBTotalPeriods,
                onSemesterStartDateChange = { viewModel.setSemesterStartDate(PersonType.PERSON_B, it) },
                onTotalWeeksChange = { viewModel.setTotalWeeks(PersonType.PERSON_B, it) },
                onCurrentWeekChange = { viewModel.setCurrentWeek(PersonType.PERSON_B, it) },
                onTotalPeriodsChange = { viewModel.setTotalPeriods(PersonType.PERSON_B, it) },
                onNavigateToPeriodTimes = { onNavigateToPeriodTimes(PersonType.PERSON_B, personBName) }
            )

            // Ta的课表设置
            PersonSettingsSection(
                title = "${personAName}的课表设置",
                personType = PersonType.PERSON_A,
                personName = personAName,
                semesterStartDate = personASemesterStartDate,
                totalWeeks = personATotalWeeks,
                currentWeek = personACurrentWeek,
                totalPeriods = personATotalPeriods,
                onSemesterStartDateChange = { viewModel.setSemesterStartDate(PersonType.PERSON_A, it) },
                onTotalWeeksChange = { viewModel.setTotalWeeks(PersonType.PERSON_A, it) },
                onCurrentWeekChange = { viewModel.setCurrentWeek(PersonType.PERSON_A, it) },
                onTotalPeriodsChange = { viewModel.setTotalPeriods(PersonType.PERSON_A, it) },
                onNavigateToPeriodTimes = { onNavigateToPeriodTimes(PersonType.PERSON_A, personAName) }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonSettingsSection(
    title: String,
    personType: PersonType,
    personName: String,
    semesterStartDate: LocalDate,
    totalWeeks: Int,
    currentWeek: Int,
    totalPeriods: Int,
    onSemesterStartDateChange: (LocalDate) -> Unit,
    onTotalWeeksChange: (Int) -> Unit,
    onCurrentWeekChange: (Int) -> Unit,
    onTotalPeriodsChange: (Int) -> Unit,
    onNavigateToPeriodTimes: () -> Unit
) {
    val context = LocalContext.current
    
    var showTotalWeeksDialog by remember { mutableStateOf(false) }
    var showCurrentWeekDialog by remember { mutableStateOf(false) }
    var showTotalPeriodsDialog by remember { mutableStateOf(false) }

    // 小米风格卡片
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 开学时间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onSemesterStartDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            semesterStartDate.year,
                            semesterStartDate.monthValue - 1,
                            semesterStartDate.dayOfMonth
                        ).show()
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "开学时间",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "用于自动计算当前周次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = semesterStartDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // 学期总周数
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTotalWeeksDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "学期总周数",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "本学期的总周数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${totalWeeks}周",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // 当前周次
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrentWeekDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "当前周次",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "第${currentWeek}周 (共${totalWeeks}周)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "第${currentWeek}周",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 周次切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        val newWeek = currentWeek - 1
                        if (newWeek >= 1) {
                            onCurrentWeekChange(newWeek)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = currentWeek > 1,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("上一周")
                }

                Button(
                    onClick = { 
                        val newWeek = currentWeek + 1
                        if (newWeek <= totalWeeks) {
                            onCurrentWeekChange(newWeek)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = currentWeek < totalWeeks,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("下一周")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // 每天课表节数
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTotalPeriodsDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "每天课表节数",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "课表显示的行数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${totalPeriods}节",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // 时间设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPeriodTimes() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "时间设置",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "设置每节课的开始和结束时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "进入",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showTotalWeeksDialog) {
        NumberInputDialog(
            title = "学期总周数",
            label = "请输入学期总周数",
            initialValue = totalWeeks,
            minValue = 1,
            maxValue = 30,
            onDismiss = { showTotalWeeksDialog = false },
            onConfirm = { 
                onTotalWeeksChange(it)
                showTotalWeeksDialog = false
            }
        )
    }

    if (showCurrentWeekDialog) {
        NumberInputDialog(
            title = "当前周次",
            label = "请输入当前周次 (1-$totalWeeks)",
            initialValue = currentWeek,
            minValue = 1,
            maxValue = totalWeeks,
            onDismiss = { showCurrentWeekDialog = false },
            onConfirm = { 
                onCurrentWeekChange(it)
                showCurrentWeekDialog = false
            }
        )
    }

    if (showTotalPeriodsDialog) {
        NumberInputDialog(
            title = "每天课表节数",
            label = "请输入每天课表节数",
            initialValue = totalPeriods,
            minValue = 1,
            maxValue = 20,
            onDismiss = { showTotalPeriodsDialog = false },
            onConfirm = { 
                onTotalPeriodsChange(it)
                showTotalPeriodsDialog = false
            }
        )
    }
}
