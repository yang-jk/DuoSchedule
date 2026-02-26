package com.duoschedule.ui.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.ui.theme.getCourseColorByName
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import android.widget.Toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    personType: PersonType,
    onNavigateToEdit: (Long?, Int?, Int?) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val courses by viewModel.getCoursesByPerson(personType).collectAsState(initial = emptyList())
    val currentWeek by viewModel.getCurrentWeek(personType).collectAsState()
    val totalWeeks by viewModel.getTotalWeeks(personType).collectAsState()
    val semesterStartDate by viewModel.getSemesterStartDate(personType).collectAsState()
    val totalPeriods by viewModel.getTotalPeriods(personType).collectAsState()
    val periodTimes by viewModel.getPeriodTimes(personType).collectAsState()
    val showNonCurrentWeekCourses by viewModel.showNonCurrentWeekCourses.collectAsState()
    val showSaturday by viewModel.showSaturday.collectAsState()
    val showSunday by viewModel.showSunday.collectAsState()

    val personName by if (personType == PersonType.PERSON_A) {
        viewModel.personAName.collectAsState()
    } else {
        viewModel.personBName.collectAsState()
    }

    var selectedWeek by remember(currentWeek) { mutableStateOf(currentWeek) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var lastDragDirection by remember { mutableIntStateOf(0) }
    
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var isAnimating by remember { mutableStateOf(false) }

    val weekDates = remember(semesterStartDate, selectedWeek) {
        viewModel.getWeekDates(semesterStartDate, selectedWeek)
    }

    val displayPeriodTimes = remember(periodTimes, totalPeriods) {
        if (periodTimes.isEmpty()) {
            (1..totalPeriods).map { "08:00-08:45" }
        } else {
            periodTimes.take(totalPeriods)
        }
    }

    val parsedPeriodTimes = remember(displayPeriodTimes) {
        parsePeriodTimes(displayPeriodTimes)
    }

    val weekCourses = remember(selectedWeek, showNonCurrentWeekCourses, courses) {
        if (showNonCurrentWeekCourses) {
            courses
        } else {
            courses.filter { it.isInWeek(selectedWeek) }
        }
    }

    val courseSlotMap = remember(weekCourses, parsedPeriodTimes) {
        buildCourseSlotMap(weekCourses, parsedPeriodTimes)
    }

    val personColor = if (personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()

    val daysToShow = remember(showSaturday, showSunday) {
        val days = mutableListOf<String>()
        days.addAll(listOf("周一", "周二", "周三", "周四", "周五"))
        if (showSaturday) days.add("周六")
        if (showSunday) days.add("周日")
        days
    }

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${personName}的课表",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            AnimatedContent(
                                targetState = selectedWeek,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInHorizontally(
                                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                                        ) { it } togetherWith slideOutHorizontally(
                                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                                        ) { -it }).using(SizeTransform(clip = false))
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                                        ) { -it } togetherWith slideOutHorizontally(
                                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                                        ) { it }).using(SizeTransform(clip = false))
                                    }
                                },
                                label = "week_number_animation"
                            ) { week ->
                                Text(
                                    text = "第 $week 周",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(null, null, null) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加课程")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(totalWeeks, selectedWeek, isAnimating) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (isAnimating) {
                                dragOffset = 0f
                                return@detectHorizontalDragGestures
                            }
                            
                            val density = this@pointerInput
                            val threshold = with(density) { 100.dp.toPx() }
                            val velocity = abs(dragOffset)
                            
                            when {
                                dragOffset > threshold && selectedWeek > 1 -> {
                                    lastDragDirection = -1
                                    isAnimating = true
                                    selectedWeek--
                                }
                                dragOffset < -threshold && selectedWeek < totalWeeks -> {
                                    lastDragDirection = 1
                                    isAnimating = true
                                    selectedWeek++
                                }
                                selectedWeek == 1 && dragOffset > threshold -> {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "已是第一周", Toast.LENGTH_SHORT).show()
                                }
                                selectedWeek == totalWeeks && dragOffset < -threshold -> {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "已是最后一周", Toast.LENGTH_SHORT).show()
                                }
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!isAnimating) {
                                dragOffset += dragAmount
                            }
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedWeek,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                        ) { it } togetherWith slideOutHorizontally(
                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                        ) { -it }).using(SizeTransform(clip = false))
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                        ) { -it } togetherWith slideOutHorizontally(
                            animationSpec = tween(150, easing = FastOutSlowInEasing)
                        ) { it }).using(SizeTransform(clip = false))
                    }
                },
                label = "schedule_animation"
            ) { week ->
                val weekDatesForWeek = remember(semesterStartDate, week) {
                    viewModel.getWeekDates(semesterStartDate, week)
                }
                
                val weekCoursesForWeek = remember(week, showNonCurrentWeekCourses, courses) {
                    if (showNonCurrentWeekCourses) {
                        courses
                    } else {
                        courses.filter { it.isInWeek(week) }
                    }
                }
                
                val courseSlotMapForWeek = remember(weekCoursesForWeek, parsedPeriodTimes) {
                    buildCourseSlotMap(weekCoursesForWeek, parsedPeriodTimes)
                }
                
                WeeklyScheduleGrid(
                    courseSlotMap = courseSlotMapForWeek,
                    currentWeek = week,
                    personColor = personColor,
                    weekDates = weekDatesForWeek,
                    totalPeriods = totalPeriods,
                    periodTimes = displayPeriodTimes,
                    showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                    showSaturday = showSaturday,
                    showSunday = showSunday,
                    daysToShow = daysToShow,
                    onCourseClick = { course ->
                        selectedCourse = course
                    },
                    onEmptySlotClick = { dayOfWeek, periodIndex ->
                        onNavigateToEdit(null, dayOfWeek, periodIndex)
                    }
                )
            }
        }
        
        LaunchedEffect(selectedWeek) {
            isAnimating = false
        }
    }

    if (showPreview && selectedCourse != null) {
        CoursePreviewBottomSheet(
            course = selectedCourse!!,
            onDismiss = {
                showPreview = false
                selectedCourse = null
            },
            onEdit = {
                showPreview = false
                val course = selectedCourse
                selectedCourse = null
                course?.let { onNavigateToEdit(it.id, null, null) }
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

private data class PeriodTimeRange(
    val startMinutes: Int,
    val endMinutes: Int
)

private fun parsePeriodTimes(periodTimes: List<String>): List<PeriodTimeRange> {
    return periodTimes.map { timeRange ->
        val times = timeRange.split("-")
        if (times.size == 2) {
            val startParts = times[0].split(":")
            val endParts = times[1].split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val startMinutes = startParts[0].toIntOrNull()?.let { h ->
                    startParts[1].toIntOrNull()?.let { m -> h * 60 + m }
                } ?: 8 * 60
                val endMinutes = endParts[0].toIntOrNull()?.let { h ->
                    endParts[1].toIntOrNull()?.let { m -> h * 60 + m }
                } ?: 8 * 60 + 45
                PeriodTimeRange(startMinutes, endMinutes)
            } else {
                PeriodTimeRange(8 * 60, 8 * 60 + 45)
            }
        } else {
            PeriodTimeRange(8 * 60, 8 * 60 + 45)
        }
    }
}

private fun buildCourseSlotMap(
    courses: List<Course>,
    parsedPeriodTimes: List<PeriodTimeRange>
): Map<Pair<Int, Int>, Course> {
    val map = mutableMapOf<Pair<Int, Int>, Course>()

    for (course in courses) {
        val period = getPeriodFromTimeFast(course.startHour, course.startMinute, parsedPeriodTimes)
        val key = Pair(course.dayOfWeek, period)
        if (!map.containsKey(key)) {
            map[key] = course
        }
    }

    return map
}

private fun getPeriodFromTimeFast(hour: Int, minute: Int, parsedPeriodTimes: List<PeriodTimeRange>): Int {
    val totalMinutes = hour * 60 + minute

    parsedPeriodTimes.forEachIndexed { index, range ->
        if (totalMinutes in range.startMinutes until range.endMinutes) {
            return index + 1
        }
    }

    return 1
}

@Composable
fun WeeklyScheduleGrid(
    courseSlotMap: Map<Pair<Int, Int>, Course>,
    currentWeek: Int,
    personColor: Color,
    weekDates: List<LocalDate>,
    totalPeriods: Int,
    periodTimes: List<String>,
    showNonCurrentWeekCourses: Boolean,
    showSaturday: Boolean,
    showSunday: Boolean,
    daysToShow: List<String>,
    onCourseClick: (Course) -> Unit,
    onEmptySlotClick: (Int, Int) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
    val periodList = remember(totalPeriods) { (1..totalPeriods).toList() }

    val dayOfWeekIndices = remember(showSaturday, showSunday) {
        val indices = mutableListOf<Int>()
        indices.addAll(listOf(0, 1, 2, 3, 4))
        if (showSaturday) indices.add(5)
        if (showSunday) indices.add(6)
        indices.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "节",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            dayOfWeekIndices.forEachIndexed { displayIndex, weekIndex ->
                key(displayIndex) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = daysToShow[displayIndex],
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (weekIndex < weekDates.size) {
                            Text(
                                text = weekDates[weekIndex].format(dateFormatter),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = periodList,
                key = { it }
            ) { period ->
                PeriodRow(
                    period = period,
                    periodTimes = periodTimes,
                    courseSlotMap = courseSlotMap,
                    currentWeek = currentWeek,
                    personColor = personColor,
                    showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                    dayOfWeekIndices = dayOfWeekIndices,
                    onCourseClick = onCourseClick,
                    onEmptySlotClick = onEmptySlotClick
                )
            }
        }
    }
}

@Composable
private fun PeriodRow(
    period: Int,
    periodTimes: List<String>,
    courseSlotMap: Map<Pair<Int, Int>, Course>,
    currentWeek: Int,
    personColor: Color,
    showNonCurrentWeekCourses: Boolean,
    dayOfWeekIndices: List<Int>,
    onCourseClick: (Course) -> Unit,
    onEmptySlotClick: (Int, Int) -> Unit
) {
    val timeRange = periodTimes.getOrNull(period - 1) ?: "08:00-08:45"
    val (startTime, endTime) = remember(timeRange) {
        val times = timeRange.split("-")
        (times.getOrNull(0) ?: "08:00") to (times.getOrNull(1) ?: "08:45")
    }

    val fixedCellHeight = 100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = fixedCellHeight.dp)
    ) {
        Column(
            modifier = Modifier.width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$period",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = startTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = endTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        dayOfWeekIndices.forEach { weekIndex ->
            key(weekIndex) {
                val dayOfWeek = weekIndex + 1
                CourseSlot(
                    dayOfWeek = dayOfWeek,
                    period = period,
                    course = courseSlotMap[Pair(dayOfWeek, period)],
                    currentWeek = currentWeek,
                    personColor = personColor,
                    showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                    cellHeight = fixedCellHeight,
                    onCourseClick = onCourseClick,
                    onEmptySlotClick = onEmptySlotClick
                )
            }
        }
    }
}

@Composable
private fun RowScope.CourseSlot(
    dayOfWeek: Int,
    period: Int,
    course: Course?,
    currentWeek: Int,
    personColor: Color,
    showNonCurrentWeekCourses: Boolean,
    cellHeight: Int,
    onCourseClick: (Course) -> Unit,
    onEmptySlotClick: (Int, Int) -> Unit
) {
    val isCurrentWeekCourse = course?.isInWeek(currentWeek) ?: true

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(2.dp)
            .heightIn(min = cellHeight.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (course != null) {
                    val courseColor = getCourseColorByName(course.name)
                    val alpha = if (showNonCurrentWeekCourses && !isCurrentWeekCourse) 0.25f else 0.75f
                    Modifier
                        .background(courseColor.copy(alpha = alpha))
                        .clickable { onCourseClick(course) }
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { onEmptySlotClick(dayOfWeek, period) }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (course != null) {
            CourseCellContent(
                courseName = course.name,
                location = course.location,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun CourseCellContent(
    courseName: String,
    location: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AutoSizeText(
            text = courseName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            maxLines = 3,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (location.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))

            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(2.dp))

            AutoSizeText(
                text = location,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    style: TextStyle,
    maxLines: Int,
    color: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    var textSize by remember { mutableStateOf(style.fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = style.copy(fontSize = textSize, color = color),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        modifier = modifier,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                textSize = (textSize.value * 0.9f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}
