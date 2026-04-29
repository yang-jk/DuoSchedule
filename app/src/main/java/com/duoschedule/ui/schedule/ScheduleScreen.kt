package com.duoschedule.ui.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlin.math.abs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.settings.components.GlassConfirmDialog
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.getCourseColorByName
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.duoschedule.ui.theme.getLabelsVibrantTertiary
import com.duoschedule.ui.theme.LiquidGlassButton
import com.duoschedule.ui.theme.LiquidGlassButtonStyle
import com.duoschedule.ui.theme.getLiquidGlassFillShadow
import com.duoschedule.ui.theme.getLiquidGlassGradient
import com.duoschedule.ui.theme.getLiquidGlassShadowColor
import com.duoschedule.ui.theme.ScheduleDimensions
import com.duoschedule.ui.theme.getScheduleGridSeparatorColor
import com.duoschedule.ui.theme.getWeekChipSelectedColor
import com.duoschedule.ui.theme.getWeekChipUnselectedColor
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val showDashedBorder by viewModel.showDashedBorder.collectAsState()
    val courseNameFontSize by viewModel.courseNameFontSize.collectAsState()
    val courseLocationFontSize by viewModel.courseLocationFontSize.collectAsState()

    val personName by viewModel.getPersonName(personType).collectAsState()

    var selectedWeek by remember(currentWeek) { mutableIntStateOf(currentWeek) }
    var showWeekSelector by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    
    val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)
    
    LaunchedEffect(totalPeriods, currentWeek, totalWeeks) {
        if (totalPeriods > 0 && currentWeek > 0 && totalWeeks > 0) {
            isDataLoaded = true
        }
    }

    val currentWeekDates = remember(semesterStartDate, selectedWeek) {
        viewModel.getWeekDates(semesterStartDate, selectedWeek)
    }
    
    val prevWeekDates = remember(semesterStartDate, selectedWeek) {
        if (selectedWeek > 1) viewModel.getWeekDates(semesterStartDate, selectedWeek - 1) else emptyList()
    }
    
    val nextWeekDates = remember(semesterStartDate, selectedWeek) {
        if (selectedWeek < totalWeeks) viewModel.getWeekDates(semesterStartDate, selectedWeek + 1) else emptyList()
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

    val personColor = if (personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()

    val daysToShow = remember(showSaturday, showSunday) {
        val days = mutableListOf<String>()
        days.addAll(listOf("周一", "周二", "周三", "周四", "周五"))
        if (showSaturday) days.add("周六")
        if (showSunday) days.add("周日")
        days
    }
    
    val currentWeekCourses = remember(selectedWeek, showNonCurrentWeekCourses, courses) {
        if (showNonCurrentWeekCourses) {
            courses
        } else {
            courses.filter { it.isInWeek(selectedWeek) }
        }
    }
    
    val prevWeekCourses = remember(selectedWeek, showNonCurrentWeekCourses, courses) {
        if (selectedWeek > 1) {
            if (showNonCurrentWeekCourses) courses else courses.filter { it.isInWeek(selectedWeek - 1) }
        } else emptyList()
    }
    
    val nextWeekCourses = remember(selectedWeek, showNonCurrentWeekCourses, courses) {
        if (selectedWeek < totalWeeks) {
            if (showNonCurrentWeekCourses) courses else courses.filter { it.isInWeek(selectedWeek + 1) }
        } else emptyList()
    }
    
    val currentCourseSlotMap = remember(currentWeekCourses, parsedPeriodTimes) {
        buildCourseSlotMap(currentWeekCourses, parsedPeriodTimes)
    }
    
    val prevCourseSlotMap = remember(prevWeekCourses, parsedPeriodTimes) {
        if (selectedWeek > 1) buildCourseSlotMap(prevWeekCourses, parsedPeriodTimes) else emptyMap()
    }
    
    val nextCourseSlotMap = remember(nextWeekCourses, parsedPeriodTimes) {
        if (selectedWeek < totalWeeks) buildCourseSlotMap(nextWeekCourses, parsedPeriodTimes) else emptyMap()
    }

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuCellBounds by remember { mutableStateOf(CellBounds(0, 0, 0, 0)) }
    var contextMenuItems by remember { mutableStateOf<List<ContextMenuItem>>(emptyList()) }
    var selectedContextMenuSlot by remember { mutableStateOf<EmptySlotPosition?>(null) }
    var showPasteConflictDialog by remember { mutableStateOf(false) }
    var pendingPasteNewCourse by remember { mutableStateOf<Course?>(null) }
    var pendingPasteConflictCourse by remember { mutableStateOf<Course?>(null) }
    var pendingPasteSlot by remember { mutableStateOf<EmptySlotPosition?>(null) }
    
    val hasClipboardContent by CourseClipboard.clippedCourse.collectAsState()

    LaunchedEffect(selectedCourse) {
        if (selectedCourse != null) {
            showPreview = true
        }
    }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "第 $selectedWeek 周",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = labelsPrimary,
                        modifier = Modifier.clickable { showWeekSelector = !showWeekSelector }
                    )
                }
            },
            navigationIcon = {
                Text(
                    "${personName}的课表",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            },
            actions = {
                GlassSymbolIconButton(
                    onClick = { onNavigateToEdit(null, null, null) },
                    style = GlassSymbolButtonStyle.NonTinted
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加课程", tint = labelsPrimary)
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (showWeekSelector) {
                WeekSelectorDropdown(
                    totalWeeks = totalWeeks,
                    currentWeek = currentWeek,
                    selectedWeek = selectedWeek,
                    onWeekSelected = { week ->
                        selectedWeek = week
                        showWeekSelector = false
                    },
                    onDismiss = { showWeekSelector = false },
                    backdrop = LocalBackdrop.current ?: emptyBackdrop()
                )
            }

            val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
            var isDragging by remember { mutableStateOf(false) }
            val density = androidx.compose.ui.platform.LocalDensity.current
            val screenWidth = with(density) { 
                androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx() 
            }
            val threshold = screenWidth * 0.3f
            val scope = rememberCoroutineScope()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalWeeks, selectedWeek) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                scope.launch {
                                    when {
                                        swipeOffset.value > threshold && selectedWeek > 1 -> {
                                            swipeOffset.animateTo(
                                                targetValue = screenWidth,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            )
                                            selectedWeek--
                                            swipeOffset.snapTo(0f)
                                        }
                                        swipeOffset.value < -threshold && selectedWeek < totalWeeks -> {
                                            swipeOffset.animateTo(
                                                targetValue = -screenWidth,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            )
                                            selectedWeek++
                                            swipeOffset.snapTo(0f)
                                        }
                                        else -> {
                                            swipeOffset.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                                            )
                                        }
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                scope.launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                scope.launch {
                                    val newOffset = swipeOffset.value + dragAmount
                                    val targetOffset = when {
                                        selectedWeek == 1 && newOffset > 0 -> newOffset * 0.3f
                                        selectedWeek == totalWeeks && newOffset < 0 -> newOffset * 0.3f
                                        else -> newOffset
                                    }
                                    swipeOffset.snapTo(targetOffset)
                                }
                            }
                        )
                    }
            ) {
                if (isDataLoaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    ) {
                        WeeklyScheduleGrid(
                            courseSlotMap = currentCourseSlotMap,
                            currentWeek = selectedWeek,
                            personColor = personColor,
                            weekDates = currentWeekDates,
                            totalPeriods = totalPeriods,
                            periodTimes = displayPeriodTimes,
                            showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                            showSaturday = showSaturday,
                            showSunday = showSunday,
                            daysToShow = daysToShow,
                            showDashedBorder = showDashedBorder,
                            courseNameFontSize = courseNameFontSize,
                            courseLocationFontSize = courseLocationFontSize,
                            selectedContextMenuSlot = selectedContextMenuSlot,
                            onCourseClick = { course ->
                                selectedCourse = course
                            },
                            onEmptySlotClick = { dayOfWeek, periodIndex ->
                                onNavigateToEdit(null, dayOfWeek, periodIndex)
                            },
                            onCourseLongPress = { course, cellBounds ->
                                contextMenuCellBounds = cellBounds
                                selectedContextMenuSlot = EmptySlotPosition(course.dayOfWeek, course.startPeriod)
                                contextMenuItems = listOf(
                                    ContextMenuItem(
                                        label = "复制"
                                    ) {
                                        CourseClipboard.copy(course)
                                    },
                                    ContextMenuItem(
                                        label = "编辑"
                                    ) {
                                        onNavigateToEdit(course.id, null, null)
                                    },
                                    ContextMenuItem(
                                        label = "删除",
                                        isDestructive = true
                                    ) {
                                        selectedCourse = course
                                        showDeleteConfirm = true
                                    }
                                )
                                showContextMenu = true
                            },
                            onEmptySlotLongPress = { dayOfWeek, period, cellBounds ->
                                contextMenuCellBounds = cellBounds
                                selectedContextMenuSlot = EmptySlotPosition(dayOfWeek, period)
                                val items = mutableListOf<ContextMenuItem>()
                                
                                if (hasClipboardContent != null) {
                                    items.add(
                                        ContextMenuItem(
                                            label = "粘贴"
                                        ) {
                                            pendingPasteSlot = EmptySlotPosition(dayOfWeek, period)
                                            viewModel.viewModelScope.launch {
                                                val result = viewModel.pasteCourse(
                                                    dayOfWeek = dayOfWeek,
                                                    period = period,
                                                    periodTimes = displayPeriodTimes,
                                                    personType = personType
                                                )
                                                when (result) {
                                                    is PasteResult.Success -> {}
                                                    is PasteResult.Conflict -> {
                                                        pendingPasteNewCourse = result.newCourse
                                                        pendingPasteConflictCourse = result.existingCourse
                                                        showPasteConflictDialog = true
                                                    }
                                                    is PasteResult.NoContent -> {}
                                                    is PasteResult.InvalidPeriod -> {}
                                                }
                                            }
                                        }
                                    )
                                }
                                
                                items.add(
                                    ContextMenuItem(
                                        label = "添加课程"
                                    ) {
                                        onNavigateToEdit(null, dayOfWeek, period)
                                    }
                                )
                                
                                contextMenuItems = items
                                showContextMenu = true
                            }
                        )
                    }
                    
                    if (selectedWeek > 1 && prevCourseSlotMap.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset((swipeOffset.value - screenWidth).roundToInt(), 0) }
                        ) {
                            WeeklyScheduleGrid(
                                courseSlotMap = prevCourseSlotMap,
                                currentWeek = selectedWeek - 1,
                                personColor = personColor,
                                weekDates = prevWeekDates,
                                totalPeriods = totalPeriods,
                                periodTimes = displayPeriodTimes,
                                showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                                showSaturday = showSaturday,
                                showSunday = showSunday,
                                daysToShow = daysToShow,
                                showDashedBorder = showDashedBorder,
                                courseNameFontSize = courseNameFontSize,
                                courseLocationFontSize = courseLocationFontSize,
                                selectedContextMenuSlot = null,
                                onCourseClick = { },
                                onEmptySlotClick = { _, _ -> },
                                onCourseLongPress = { _, _ -> },
                                onEmptySlotLongPress = { _, _, _ -> }
                            )
                        }
                    }
                    
                    if (selectedWeek < totalWeeks && nextCourseSlotMap.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset((swipeOffset.value + screenWidth).roundToInt(), 0) }
                        ) {
                            WeeklyScheduleGrid(
                                courseSlotMap = nextCourseSlotMap,
                                currentWeek = selectedWeek + 1,
                                personColor = personColor,
                                weekDates = nextWeekDates,
                                totalPeriods = totalPeriods,
                                periodTimes = displayPeriodTimes,
                                showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                                showSaturday = showSaturday,
                                showSunday = showSunday,
                                daysToShow = daysToShow,
                                showDashedBorder = showDashedBorder,
                                courseNameFontSize = courseNameFontSize,
                                courseLocationFontSize = courseLocationFontSize,
                                selectedContextMenuSlot = null,
                                onCourseClick = { },
                                onEmptySlotClick = { _, _ -> },
                                onCourseLongPress = { _, _ -> },
                                onEmptySlotLongPress = { _, _, _ -> }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = getLabelsVibrantPrimary()
                        )
                    }
                }
            }
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
        GlassConfirmDialog(
            backdrop = LocalBackdrop.current ?: emptyBackdrop(),
            title = "删除课程",
            message = "确定要删除「${selectedCourse?.name}」吗？",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = {
                viewModel.deleteCourse(selectedCourse!!.id)
                showDeleteConfirm = false
                showPreview = false
                selectedCourse = null
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
    
    if (showPasteConflictDialog && pendingPasteNewCourse != null && pendingPasteConflictCourse != null && pendingPasteSlot != null) {
        GlassConfirmDialog(
            backdrop = LocalBackdrop.current ?: emptyBackdrop(),
            title = "时间冲突",
            message = "该时间段已有课程「${pendingPasteConflictCourse!!.name}」，是否覆盖？",
            confirmText = "覆盖",
            dismissText = "取消",
            onConfirm = {
                viewModel.viewModelScope.launch {
                    viewModel.forcePasteWithConflictResolution(
                        conflictCourse = pendingPasteConflictCourse!!,
                        dayOfWeek = pendingPasteSlot!!.dayOfWeek,
                        period = pendingPasteSlot!!.period,
                        periodTimes = displayPeriodTimes,
                        personType = personType
                    )
                }
                showPasteConflictDialog = false
                pendingPasteNewCourse = null
                pendingPasteConflictCourse = null
                pendingPasteSlot = null
            },
            onDismiss = { showPasteConflictDialog = false }
        )
    }
    
    CourseContextMenu(
        expanded = showContextMenu,
        onDismiss = { 
            showContextMenu = false
            selectedContextMenuSlot = null
        },
        menuItems = contextMenuItems,
        cellBounds = contextMenuCellBounds,
        backdrop = LocalBackdrop.current ?: emptyBackdrop()
    )
}

@Composable
private fun WeekSelectorDropdown(
    totalWeeks: Int,
    currentWeek: Int,
    selectedWeek: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    backdrop: com.kyant.backdrop.Backdrop
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val selectedColor = getWeekChipSelectedColor()
    
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(ScheduleDimensions.WeekSelectorWidth)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .padding(Spacing.lg)
        ) {
            Column {
                Text(
                    text = "选择周次",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items((1..totalWeeks).toList()) { week ->
                        val isSelected = week == selectedWeek
                        val isCurrent = week == currentWeek
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.medium))
                                .background(
                                    if (isSelected) selectedColor.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable { onWeekSelected(week) }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "第 $week 周",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) selectedColor else labelsPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (isCurrent) {
                                Text(
                                    text = "当前",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = selectedColor
                                )
                            }
                        }
                    }
                }
            }
        }
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

data class CourseSlotInfo(
    val course: Course,
    val isStart: Boolean,
    val span: Int
)

private fun buildCourseSlotMap(
    courses: List<Course>,
    parsedPeriodTimes: List<PeriodTimeRange>
): Map<Pair<Int, Int>, CourseSlotInfo> {
    val map = mutableMapOf<Pair<Int, Int>, CourseSlotInfo>()

    for (course in courses) {
        val startPeriod = if (course.startPeriod > 0) {
            course.startPeriod
        } else {
            getPeriodFromTimeFast(course.startHour, course.startMinute, parsedPeriodTimes)
        }
        
        val endPeriod = if (course.endPeriod > 0) {
            course.endPeriod
        } else {
            getPeriodFromTimeFast(course.endHour, course.endMinute, parsedPeriodTimes)
        }
        
        val span = (endPeriod - startPeriod + 1).coerceAtLeast(1)
        
        for (period in startPeriod..endPeriod) {
            val key = Pair(course.dayOfWeek, period)
            if (!map.containsKey(key)) {
                map[key] = CourseSlotInfo(
                    course = course,
                    isStart = period == startPeriod,
                    span = span
                )
            }
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

data class EmptySlotPosition(
    val dayOfWeek: Int,
    val period: Int
)

private data class CourseLayoutInfo(
    val course: Course,
    val dayIndex: Int,
    val startPeriod: Int,
    val span: Int
)

@Composable
fun WeeklyScheduleGrid(
    courseSlotMap: Map<Pair<Int, Int>, CourseSlotInfo>,
    currentWeek: Int,
    personColor: Color,
    weekDates: List<LocalDate>,
    totalPeriods: Int,
    periodTimes: List<String>,
    showNonCurrentWeekCourses: Boolean,
    showSaturday: Boolean,
    showSunday: Boolean,
    daysToShow: List<String>,
    showDashedBorder: Boolean,
    courseNameFontSize: Int,
    courseLocationFontSize: Int,
    selectedContextMenuSlot: EmptySlotPosition?,
    onCourseClick: (Course) -> Unit,
    onEmptySlotClick: (Int, Int) -> Unit,
    onCourseLongPress: (Course, CellBounds) -> Unit,
    onEmptySlotLongPress: (Int, Int, CellBounds) -> Unit
) {
    var selectedEmptySlot by remember { mutableStateOf<EmptySlotPosition?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
    val periodList = remember(totalPeriods) { (1..totalPeriods).toList() }

    val dayOfWeekIndices = remember(showSaturday, showSunday) {
        val indices = mutableListOf<Int>()
        indices.addAll(listOf(0, 1, 2, 3, 4))
        if (showSaturday) indices.add(5)
        if (showSunday) indices.add(6)
        indices.toList()
    }
    
    val today = remember { LocalDate.now() }
    val darkTheme = LocalDarkTheme.current
    val gridSeparatorColor = getScheduleGridSeparatorColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    
    val fixedCellHeight = 100
    val cellSpacingPx = with(androidx.compose.ui.platform.LocalDensity.current) { Spacing.xxs.toPx().toInt() }
    
    val uniqueCourses = remember(courseSlotMap) {
        val seen = mutableSetOf<Long>()
        courseSlotMap.entries
            .filter { it.value.isStart }
            .map { entry ->
                val dayOfWeek = entry.key.first
                CourseLayoutInfo(
                    course = entry.value.course,
                    dayIndex = dayOfWeek - 1,
                    startPeriod = entry.value.course.startPeriod,
                    span = entry.value.span
                )
            }
            .filter { seen.add(it.course.id) }
    }
    
    var columnWidth by remember { mutableStateOf(0) }
    var gridOffsetY by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { selectedEmptySlot = null }
        ) {
            Column(
                modifier = Modifier.width(ScheduleDimensions.TimeColumnWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "节",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelsTertiary,
                    textAlign = TextAlign.Center
                )
            }

            dayOfWeekIndices.forEachIndexed { displayIndex, weekIndex ->
                key(displayIndex) {
                    val isToday = weekIndex < weekDates.size && weekDates[weekIndex] == today
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                if (displayIndex == 0) {
                                    columnWidth = coordinates.size.width
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = daysToShow[displayIndex],
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isToday) personColor else labelsPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (weekIndex < weekDates.size) {
                            Text(
                                text = weekDates[weekIndex].format(dateFormatter),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                textAlign = TextAlign.Center,
                                color = if (isToday) personColor else labelsTertiary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(gridSeparatorColor)
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        val totalGridHeight = remember(totalPeriods, fixedCellHeight) {
            totalPeriods * fixedCellHeight + totalPeriods * 4
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalGridHeight.dp)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    gridOffsetY = position.y.roundToInt()
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                periodList.forEach { period ->
                    PeriodRow(
                        period = period,
                        periodTimes = periodTimes,
                        currentWeek = currentWeek,
                        dayOfWeekIndices = dayOfWeekIndices,
                        showDashedBorder = showDashedBorder,
                        selectedEmptySlot = selectedEmptySlot,
                        selectedContextMenuSlot = selectedContextMenuSlot,
                        cellHeight = fixedCellHeight,
                        onEmptySlotFirstClick = { dayOfWeek, periodIndex ->
                            selectedEmptySlot = EmptySlotPosition(dayOfWeek, periodIndex)
                        },
                        onEmptySlotSecondClick = onEmptySlotClick,
                        onEmptySlotLongPress = onEmptySlotLongPress
                    )
                }
            }
            
            if (columnWidth > 0) {
                uniqueCourses.forEach { layoutInfo ->
                    key(layoutInfo.course.id) {
                        val dayIndex = layoutInfo.dayIndex
                        if (dayIndex >= 0 && dayIndex < dayOfWeekIndices.size) {
                            CourseOverlayCard(
                                course = layoutInfo.course,
                                dayIndex = dayIndex,
                                startPeriod = layoutInfo.startPeriod,
                                span = layoutInfo.span,
                                currentWeek = currentWeek,
                                columnWidth = columnWidth,
                                cellHeight = fixedCellHeight,
                                cellSpacing = cellSpacingPx,
                                timeColumnWidth = with(androidx.compose.ui.platform.LocalDensity.current) { 
                                    ScheduleDimensions.TimeColumnWidth.toPx().toInt() 
                                },
                                showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                                courseNameFontSize = courseNameFontSize,
                                courseLocationFontSize = courseLocationFontSize,
                                onCourseClick = onCourseClick,
                                onCourseLongPress = onCourseLongPress,
                                gridOffsetY = gridOffsetY
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodRow(
    period: Int,
    periodTimes: List<String>,
    currentWeek: Int,
    dayOfWeekIndices: List<Int>,
    showDashedBorder: Boolean,
    selectedEmptySlot: EmptySlotPosition?,
    selectedContextMenuSlot: EmptySlotPosition?,
    cellHeight: Int,
    onEmptySlotFirstClick: (Int, Int) -> Unit,
    onEmptySlotSecondClick: (Int, Int) -> Unit,
    onEmptySlotLongPress: (Int, Int, CellBounds) -> Unit
) {
    val timeRange = periodTimes.getOrNull(period - 1) ?: "08:00-08:45"
    val (startTime, endTime) = remember(timeRange) {
        val times = timeRange.split("-")
        (times.getOrNull(0) ?: "08:00") to (times.getOrNull(1) ?: "08:45")
    }

    val darkTheme = LocalDarkTheme.current
    val gridSeparatorColor = getScheduleGridSeparatorColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val overlayBackgroundColor = if (darkTheme) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.18f)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellHeight.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(ScheduleDimensions.TimeColumnWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$period",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    color = labelsPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    textAlign = TextAlign.Center,
                    color = labelsTertiary,
                    maxLines = 1
                )
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    textAlign = TextAlign.Center,
                    color = labelsTertiary,
                    maxLines = 1
                )
            }

            dayOfWeekIndices.forEach { weekIndex ->
                key(weekIndex) {
                    val dayOfWeek = weekIndex + 1
                    EmptySlot(
                        dayOfWeek = dayOfWeek,
                        period = period,
                        cellHeight = cellHeight,
                        isSelected = selectedEmptySlot?.dayOfWeek == dayOfWeek && selectedEmptySlot?.period == period,
                        isContextMenuSelected = selectedContextMenuSlot?.dayOfWeek == dayOfWeek && selectedContextMenuSlot?.period == period,
                        overlayBackgroundColor = overlayBackgroundColor,
                        onFirstClick = onEmptySlotFirstClick,
                        onSecondClick = onEmptySlotSecondClick,
                        onLongPress = onEmptySlotLongPress
                    )
                }
            }
        }
        
        if (showDashedBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(gridSeparatorColor)
                    .padding(start = ScheduleDimensions.TimeColumnWidth)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RowScope.EmptySlot(
    dayOfWeek: Int,
    period: Int,
    cellHeight: Int,
    isSelected: Boolean,
    isContextMenuSelected: Boolean,
    overlayBackgroundColor: Color,
    onFirstClick: (Int, Int) -> Unit,
    onSecondClick: (Int, Int) -> Unit,
    onLongPress: (Int, Int, CellBounds) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
    val selectionBorderColor = Color(0xFF4789FE)
    
    var cellBounds by remember { mutableStateOf(CellBounds(0, 0, 0, 0)) }
    
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(ScheduleDimensions.CellPadding)
            .then(
                if (isContextMenuSelected) {
                    Modifier.border(width = 2.dp, color = selectionBorderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                cellBounds = CellBounds(
                    x = position.x.roundToInt(),
                    y = position.y.roundToInt(),
                    width = size.width,
                    height = size.height
                )
            }
    ) {
        Box(
            modifier = Modifier
                .height(cellHeight.dp)
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (isSelected) overlayBackgroundColor
                    else if (darkTheme) Color.White.copy(alpha = 0.02f)
                    else Color.Black.copy(alpha = 0.01f)
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (isSelected) {
                            onSecondClick(dayOfWeek, period)
                        } else {
                            onFirstClick(dayOfWeek, period)
                        }
                    },
                    onLongClick = {
                        onLongPress(dayOfWeek, period, cellBounds)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加课程",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CourseOverlayCard(
    course: Course,
    dayIndex: Int,
    startPeriod: Int,
    span: Int,
    currentWeek: Int,
    columnWidth: Int,
    cellHeight: Int,
    cellSpacing: Int,
    timeColumnWidth: Int,
    showNonCurrentWeekCourses: Boolean,
    courseNameFontSize: Int,
    courseLocationFontSize: Int,
    onCourseClick: (Course) -> Unit,
    onCourseLongPress: (Course, CellBounds) -> Unit,
    gridOffsetY: Int
) {
    val isCurrentWeekCourse = course.isInWeek(currentWeek)
    val darkTheme = LocalDarkTheme.current
    
    val fillShadow = getLiquidGlassFillShadow()
    val shadowColor = getLiquidGlassShadowColor()
    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
    
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "card_scale"
    )
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }
    
    val cardWidth = columnWidth - cellPaddingPx * 2
    val singleCellHeightPx = with(density) { cellHeight.dp.toPx().toInt() }
    val totalHeightPx = singleCellHeightPx * span + cellSpacing * (span - 1)
    
    val offsetX = timeColumnWidth + dayIndex * columnWidth + cellPaddingPx
    val offsetY = (startPeriod - 1) * (singleCellHeightPx + cellSpacing) + cellPaddingPx
    
    var cardBounds by remember { mutableStateOf(CellBounds(0, 0, 0, 0)) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .width(with(density) { cardWidth.toDp() })
            .height(with(density) { totalHeightPx.toDp() })
            .scale(scale)
            .clip(shape)
            .graphicsLayer {
                shadowElevation = 16.dp.toPx()
                this.shape = shape
                clip = true
                ambientShadowColor = Color.Transparent
                spotShadowColor = shadowColor
            }
            .background(fillShadow)
            .then(
                run {
                    val courseColor = getCourseColorByName(course.name)
                    val alpha = if (showNonCurrentWeekCourses && !isCurrentWeekCourse) 0.35f else 0.92f
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                courseColor.copy(alpha = alpha * 0.98f),
                                courseColor.copy(alpha = alpha * 0.88f)
                            )
                        )
                    )
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCourseClick(course) },
                onLongClick = {
                    onCourseLongPress(course, cardBounds)
                }
            )
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                cardBounds = CellBounds(
                    x = position.x.roundToInt(),
                    y = position.y.roundToInt(),
                    width = size.width,
                    height = size.height
                )
            }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val textAlpha = if (showNonCurrentWeekCourses && !isCurrentWeekCourse) 0.6f else 1f
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = courseNameFontSize.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = labelsPrimary.copy(alpha = textAlpha),
                textAlign = TextAlign.Center
            )

            if (course.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(labelsSecondary.copy(alpha = 0.25f))
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = course.location,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = courseLocationFontSize.sp),
                    maxLines = ScheduleDimensions.LocationMaxLines,
                    color = labelsSecondary.copy(alpha = textAlpha * 0.8f),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
