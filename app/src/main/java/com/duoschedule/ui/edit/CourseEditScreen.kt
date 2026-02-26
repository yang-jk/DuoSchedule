package com.duoschedule.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    courseId: Long?,
    initialDayOfWeek: Int? = null,
    initialPeriod: Int? = null,
    initialPersonType: PersonType? = null,
    onNavigateBack: () -> Unit,
    viewModel: CourseEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val totalWeeks by viewModel.totalWeeks.collectAsState()
    val totalPeriods by viewModel.totalPeriods.collectAsState()
    val periodTimes by viewModel.periodTimes.collectAsState()
    val courseHistory by viewModel.courseHistory.collectAsState()
    val teacherHistory by viewModel.teacherHistory.collectAsState()

    val personAColor = getPersonAColor()
    val personBColor = getPersonBColor()

    val weekPickerState = rememberModalBottomSheetState()
    val periodPickerState = rememberModalBottomSheetState()
    var showWeekPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        if (courseId != null && courseId > 0) {
            viewModel.loadCourse(courseId)
        }
    }

    LaunchedEffect(initialDayOfWeek, initialPeriod, initialPersonType) {
        if (courseId == null || courseId <= 0) {
            initialDayOfWeek?.let { viewModel.setInitialDayOfWeek(it) }
            initialPeriod?.let { viewModel.setInitialPeriod(it) }
            initialPersonType?.let { viewModel.setInitialPersonType(it) }
        }
    }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) {
            onNavigateBack()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCourseNameSuggestions by remember { mutableStateOf(false) }
    var showTeacherSuggestions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "编辑课程" else "添加课程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                    IconButton(onClick = viewModel::saveCourse) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                state.errorMessage?.let { error ->
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "课程名称",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { 
                                viewModel.setName(it)
                                showCourseNameSuggestions = it.isNotEmpty()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            placeholder = { Text("请输入课程名称") },
                            isError = state.name.isBlank() && state.errorMessage != null,
                            trailingIcon = {
                                if (state.name.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setName("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清除")
                                    }
                                }
                            }
                        )

                        if (showCourseNameSuggestions && courseHistory.isNotEmpty()) {
                            val filteredHistory = courseHistory.filter {
                                it.name.contains(state.name, ignoreCase = true) && it.name != state.name
                            }.take(3)

                            if (filteredHistory.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "历史课程",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                filteredHistory.forEach { history ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectFromHistory(history)
                                                showCourseNameSuggestions = false
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = history.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "所属课表",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SelectableChip(
                                selected = state.personType == PersonType.PERSON_A,
                                onClick = { viewModel.setPersonType(PersonType.PERSON_A) },
                                label = "Ta的课表",
                                selectedColor = personAColor,
                                modifier = Modifier.weight(1f)
                            )
                            SelectableChip(
                                selected = state.personType == PersonType.PERSON_B,
                                onClick = { viewModel.setPersonType(PersonType.PERSON_B) },
                                label = "我的课表",
                                selectedColor = personBColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingItem(
                            icon = Icons.Default.Schedule,
                            title = "上课时间",
                            value = getPeriodDisplayText(state.dayOfWeek, state.startPeriod, state.endPeriod),
                            onClick = { showPeriodPicker = true }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        SettingItem(
                            icon = Icons.Default.CalendarMonth,
                            title = "上课周数",
                            value = getWeekDisplayText(state.selectedWeeks, totalWeeks),
                            onClick = { showWeekPicker = true }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        SettingItem(
                            icon = Icons.Default.LocationOn,
                            title = "教室地点",
                            value = state.location.ifEmpty { "点击输入" },
                            onClick = {},
                            isEditable = true,
                            editableValue = state.location,
                            onValueChange = viewModel::setLocation
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Column {
                            SettingItem(
                                icon = Icons.Default.Person,
                                title = "上课老师",
                                value = state.teacher.ifEmpty { "点击输入（可选）" },
                                onClick = {},
                                isEditable = true,
                                editableValue = state.teacher,
                                onValueChange = { 
                                    viewModel.setTeacher(it)
                                    showTeacherSuggestions = it.isNotEmpty()
                                }
                            )

                            if (showTeacherSuggestions && teacherHistory.isNotEmpty()) {
                                val filteredTeachers = teacherHistory.filter {
                                    it.contains(state.teacher, ignoreCase = true) && it != state.teacher
                                }.take(3)

                                if (filteredTeachers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    filteredTeachers.forEach { teacher ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setTeacher(teacher)
                                                    showTeacherSuggestions = false
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = teacher,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWeekPicker) {
        WeekPickerBottomSheet(
            totalWeeks = totalWeeks,
            selectedWeeks = state.selectedWeeks,
            onWeeksChange = viewModel::setSelectedWeeks,
            onDismiss = { showWeekPicker = false },
            sheetState = weekPickerState
        )
    }

    if (showPeriodPicker) {
        PeriodPickerBottomSheet(
            totalPeriods = totalPeriods,
            selectedDayOfWeek = state.dayOfWeek,
            selectedStartPeriod = state.startPeriod,
            selectedEndPeriod = state.endPeriod,
            onSelectionChange = { dayOfWeek, startPeriod, endPeriod ->
                viewModel.setDayOfWeek(dayOfWeek)
                viewModel.setPeriods(startPeriod, endPeriod)
            },
            onDismiss = { showPeriodPicker = false },
            sheetState = periodPickerState
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = getDialogBackgroundColor(),
            shape = MaterialTheme.shapes.large,
            title = { Text("删除课程") },
            text = { Text("确定要删除这门课程吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCourse()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    isEditable: Boolean = false,
    editableValue: String = "",
    onValueChange: (String) -> Unit = {}
) {
    if (isEditable) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = editableValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                placeholder = { Text(value) }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor.copy(alpha = 0.15f),
            selectedLabelColor = selectedColor
        ),
        modifier = modifier
    )
}

private fun getPeriodDisplayText(dayOfWeek: Int, startPeriod: Int, endPeriod: Int): String {
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val dayText = days.getOrNull(dayOfWeek - 1) ?: "周一"
    return if (startPeriod == endPeriod) {
        "$dayText 第${startPeriod}节"
    } else {
        "$dayText 第${startPeriod}-${endPeriod}节"
    }
}

private fun getWeekDisplayText(selectedWeeks: Set<Int>, totalWeeks: Int): String {
    return when {
        selectedWeeks.isEmpty() -> "未选择"
        selectedWeeks.size == totalWeeks -> "全周 (1-${totalWeeks}周)"
        selectedWeeks.size == totalWeeks / 2 -> {
            val allOdd = selectedWeeks.all { it % 2 == 1 }
            val allEven = selectedWeeks.all { it % 2 == 0 }
            when {
                allOdd -> "单周"
                allEven -> "双周"
                else -> formatWeekRanges(selectedWeeks)
            }
        }
        else -> formatWeekRanges(selectedWeeks)
    }
}

private fun formatWeekRanges(weeks: Set<Int>): String {
    if (weeks.isEmpty()) return ""
    
    val sortedWeeks = weeks.sorted()
    val ranges = mutableListOf<String>()
    var rangeStart = sortedWeeks[0]
    var rangeEnd = sortedWeeks[0]
    
    for (i in 1 until sortedWeeks.size) {
        if (sortedWeeks[i] == rangeEnd + 1) {
            rangeEnd = sortedWeeks[i]
        } else {
            ranges.add(if (rangeStart == rangeEnd) {
                "${rangeStart}"
            } else {
                "${rangeStart}-${rangeEnd}"
            })
            rangeStart = sortedWeeks[i]
            rangeEnd = sortedWeeks[i]
        }
    }
    
    ranges.add(if (rangeStart == rangeEnd) {
        "${rangeStart}"
    } else {
        "${rangeStart}-${rangeEnd}"
    })
    
    val result = ranges.joinToString(", ")
    return if (weeks.size == 1) "第${result}周" else "${result}周"
}
