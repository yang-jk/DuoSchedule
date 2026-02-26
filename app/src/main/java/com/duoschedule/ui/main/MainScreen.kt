package com.duoschedule.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.main.components.*
import com.duoschedule.ui.theme.FlatCard
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEdit: (Long?, Int?, Int?, PersonType?) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val personACurrentCourse by viewModel.personACurrentCourse.collectAsState()
    val personBCurrentCourse by viewModel.personBCurrentCourse.collectAsState()
    val personATodayCourses by viewModel.personATodayCourses.collectAsState()
    val personBTodayCourses by viewModel.personBTodayCourses.collectAsState()
    val freeTimeSlots by viewModel.freeTimeSlots.collectAsState()
    val personACurrentWeek by viewModel.personACurrentWeek.collectAsState()
    val personBCurrentWeek by viewModel.personBCurrentWeek.collectAsState()
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()
    val displayMode by viewModel.todayCourseDisplayMode.collectAsState()
    val personAPeriodTimes by viewModel.personAPeriodTimes.collectAsState()
    val personBPeriodTimes by viewModel.personBPeriodTimes.collectAsState()

    val currentTime = remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                viewModel.updateTime()
                currentTime.value = LocalTime.now()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(60000)
        }
    }
    val currentHour = currentTime.value.hour
    val currentMinute = currentTime.value.minute

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedCoursePersonType by remember { mutableStateOf<PersonType?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCourse) {
        if (selectedCourse != null) {
            showPreview = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "双人课程表",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "$personBName: 第${personBCurrentWeek}周  |  $personAName: 第${personACurrentWeek}周",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DualCurrentCourseSection(
                    personAState = personACurrentCourse,
                    personBState = personBCurrentCourse
                )
            }

            item {
                FreeTimeSection(freeTimeSlots = freeTimeSlots)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日课程",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TodayCourseDisplayMode.values().forEach { mode ->
                            val isSelected = displayMode == mode
                            val label = when (mode) {
                                TodayCourseDisplayMode.SELF_ONLY -> personBName.ifEmpty { "我" }
                                TodayCourseDisplayMode.TA_ONLY -> personAName.ifEmpty { "Ta" }
                                TodayCourseDisplayMode.BOTH -> "全部"
                            }
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTodayCourseDisplayMode(mode) },
                                label = { 
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            if (personATodayCourses.isEmpty() && personBTodayCourses.isEmpty()) {
                item {
                    FlatCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 2.dp
                    ) {
                        Text(
                            text = "今日无课",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }
            } else {
                item {
                    TodayScheduleTimeline(
                        personACourses = personATodayCourses,
                        personBCourses = personBTodayCourses,
                        displayMode = displayMode,
                        currentHour = currentHour,
                        currentMinute = currentMinute,
                        periodTimesA = personAPeriodTimes,
                        periodTimesB = personBPeriodTimes,
                        onCourseClick = { course, personType ->
                            selectedCourse = course
                            selectedCoursePersonType = personType
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showPreview && selectedCourse != null) {
        CoursePreviewBottomSheet(
            course = selectedCourse!!,
            onDismiss = {
                showPreview = false
                selectedCourse = null
                selectedCoursePersonType = null
            },
            onEdit = {
                showPreview = false
                val course = selectedCourse
                val personType = selectedCoursePersonType
                selectedCourse = null
                selectedCoursePersonType = null
                course?.let { onNavigateToEdit(it.id, null, null, personType) }
            },
            onDelete = {
                showDeleteConfirm = true
            },
            sheetState = previewSheetState
        )
    }

    if (showDeleteConfirm && selectedCourse != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = getDialogBackgroundColor(),
            shape = MaterialTheme.shapes.large,
            title = { Text("删除课程") },
            text = { Text("确定要删除「${selectedCourse?.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCourse(selectedCourse!!.id)
                        showDeleteConfirm = false
                        showPreview = false
                        selectedCourse = null
                        selectedCoursePersonType = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun getPeriodText(course: Course, periodTimes: List<String>): String {
    if (periodTimes.isEmpty()) return ""
    
    val courseStartMinutes = course.startHour * 60 + course.startMinute
    val courseEndMinutes = course.endHour * 60 + course.endMinute
    
    var startPeriod = -1
    var endPeriod = -1
    
    for ((index, periodTime) in periodTimes.withIndex()) {
        val parts = periodTime.split("-")
        if (parts.size == 2) {
            val startParts = parts[0].split(":")
            val endParts = parts[1].split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val periodStartMinutes = (startParts[0].toIntOrNull() ?: 0) * 60 +
                    (startParts[1].toIntOrNull() ?: 0)
                val periodEndMinutes = (endParts[0].toIntOrNull() ?: 0) * 60 +
                    (endParts[1].toIntOrNull() ?: 0)
                
                if (startPeriod == -1 && courseStartMinutes >= periodStartMinutes && courseStartMinutes < periodEndMinutes) {
                    startPeriod = index + 1
                }
                if (courseEndMinutes > periodStartMinutes && courseEndMinutes <= periodEndMinutes) {
                    endPeriod = index + 1
                }
            }
        }
    }
    
    return if (startPeriod > 0 && endPeriod > 0) {
        if (startPeriod == endPeriod) {
            "第${startPeriod}节"
        } else {
            "第${startPeriod}-${endPeriod}节"
        }
    } else {
        ""
    }
}
