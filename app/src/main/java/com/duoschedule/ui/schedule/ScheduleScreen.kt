package com.duoschedule.ui.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
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
import androidx.compose.ui.unit.Dp
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
import com.duoschedule.ui.edit.CourseEditContent
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.settings.components.GlassConfirmDialog
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.getCourseColor
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
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)

private val ContainerTransformSpring: SpringSpec<Dp> = spring(dampingRatio = 0.9f, stiffness = 600f)
private val MicroTween: TweenSpec<Float> = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing)

private val EmptyCourseAction: (Course) -> Unit = { }
private val EmptyBiAction: (Int, Int) -> Unit = { _, _ -> }
private val EmptyTriAction: (Int, Int, CellBounds) -> Unit = { _, _, _ -> }

@Immutable
data class EditTarget(
    val courseId: Long?,
    val dayOfWeek: Int?,
    val periodIndex: Int?,
    val sourceKey: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ScheduleScreen(
    personType: PersonType,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val courses by viewModel.getCoursesByPerson(personType).collectAsState(initial = emptyList())
    var editTarget by remember { mutableStateOf<EditTarget?>(null) }
    val editTransitionScope = rememberCoroutineScope()

    BackHandler(enabled = editTarget != null) { editTarget = null }
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

    val handleCourseClick: (Course) -> Unit = remember { { selectedCourse = it } }

    LaunchedEffect(selectedCourse) {
        if (selectedCourse != null) {
            showPreview = true
        }
    }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val cornerRadius = LocalDeviceCornerRadius.current

    AnimatedContent(
        targetState = editTarget,
        transitionSpec = {
            val isSharedElement = targetState?.sourceKey?.isNotEmpty() == true ||
                    initialState?.sourceKey?.isNotEmpty() == true
            if (isSharedElement) {
                EnterTransition.None togetherWith
                fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
            } else {
                fadeIn(animationSpec = tween(250, delayMillis = 50, easing = FastOutSlowInEasing)) togetherWith
                fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
            }
        },
        label = "schedule_edit_transition"
    ) { target ->
        if (target == null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${personName}的课表",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "第 $selectedWeek 周",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary,
                modifier = Modifier.clickable { showWeekSelector = !showWeekSelector }
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                GlassSymbolIconButton(
                    onClick = { editTarget = EditTarget(null, null, null, "addButton_${personType.name}") },
                    style = GlassSymbolButtonStyle.NonTinted,
                    modifier = sharedTransitionScope?.let { scope ->
                        with(scope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "addButton_${personType.name}"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ -> spring(dampingRatio = 0.9f, stiffness = 600f) },
                                renderInOverlayDuringTransition = true,
                                clipInOverlayDuringTransition = OverlayClip(
                                    ContinuousRoundedRectangle(BorderRadius.iOS26.large)
                                )
                            )
                        }
                    } ?: Modifier
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加课程", tint = labelsPrimary)
                }
            }
        }

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
                                scope.launch {
                                    swipeOffset.snapTo(swipeOffset.value)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                scope.launch {
                                    when {
                                        swipeOffset.value > threshold && selectedWeek > 1 -> {
                                            swipeOffset.animateTo(
                                                targetValue = screenWidth,
                                                animationSpec = spring(dampingRatio = 1.0f, stiffness = 500f)
                                            )
                                            Snapshot.withMutableSnapshot {
                                                selectedWeek--
                                                swipeOffset.snapTo(0f)
                                            }
                                        }
                                        swipeOffset.value < -threshold && selectedWeek < totalWeeks -> {
                                            swipeOffset.animateTo(
                                                targetValue = -screenWidth,
                                                animationSpec = spring(dampingRatio = 1.0f, stiffness = 500f)
                                            )
                                            Snapshot.withMutableSnapshot {
                                                selectedWeek++
                                                swipeOffset.snapTo(0f)
                                            }
                                        }
                                        else -> {
                                            swipeOffset.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(dampingRatio = 1.0f, stiffness = 500f)
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
                                        animationSpec = spring(dampingRatio = 1.0f, stiffness = 500f)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                val newOffset = swipeOffset.value + dragAmount
                                val targetOffset = when {
                                    selectedWeek == 1 && newOffset > 0 -> newOffset * 0.3f
                                    selectedWeek == totalWeeks && newOffset < 0 -> newOffset * 0.3f
                                    else -> newOffset
                                }
                                scope.launch {
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
                            parsedPeriodTimes = parsedPeriodTimes,
                            showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                            showSaturday = showSaturday,
                            showSunday = showSunday,
                            daysToShow = daysToShow,
                            showDashedBorder = showDashedBorder,
                            courseNameFontSize = courseNameFontSize,
                            courseLocationFontSize = courseLocationFontSize,
                            selectedContextMenuSlot = selectedContextMenuSlot,
                            onCourseClick = handleCourseClick,
                            onEmptySlotClick = { dayOfWeek, periodIndex ->
                                editTarget = EditTarget(null, dayOfWeek, periodIndex, "addButton_${personType.name}")
                            },
                            onCourseLongPress = { course, cellBounds ->
                                contextMenuCellBounds = cellBounds
                                val contextPeriod = if (course.isCustomTime) {
                                    getPeriodFromTimeFast(course.startHour, course.startMinute, parsedPeriodTimes)
                                } else {
                                    course.startPeriod
                                }
                                selectedContextMenuSlot = EmptySlotPosition(course.dayOfWeek, contextPeriod)
                                contextMenuItems = listOf(
                                    ContextMenuItem(
                                        label = "复制"
                                    ) {
                                        CourseClipboard.copy(course)
                                    },
                                    ContextMenuItem(
                                        label = "编辑"
                                    ) {
                                        showContextMenu = false
                                        editTransitionScope.launch {
                                            delay(150)
                                            editTarget = EditTarget(course.id, null, null, "courseCard_${course.id}")
                                        }
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
                                        editTarget = EditTarget(null, dayOfWeek, period, "addButton_${personType.name}")
                                    }
                                )
                                
                                contextMenuItems = items
                                showContextMenu = true
                            },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = this@AnimatedContent
                        )
                    }
                    
                    if (selectedWeek > 1) {
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
                                parsedPeriodTimes = parsedPeriodTimes,
                                showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                                showSaturday = showSaturday,
                                showSunday = showSunday,
                                daysToShow = daysToShow,
                                showDashedBorder = showDashedBorder,
                                courseNameFontSize = courseNameFontSize,
                                courseLocationFontSize = courseLocationFontSize,
                                selectedContextMenuSlot = null,
                                onCourseClick = EmptyCourseAction,
                                onEmptySlotClick = EmptyBiAction,
                                onCourseLongPress = { _, _ -> },
                                onEmptySlotLongPress = EmptyTriAction,
                                sharedTransitionScope = null,
                                animatedVisibilityScope = null
                            )
                        }
                    }
                    
                    if (selectedWeek < totalWeeks) {
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
                                parsedPeriodTimes = parsedPeriodTimes,
                                showNonCurrentWeekCourses = showNonCurrentWeekCourses,
                                showSaturday = showSaturday,
                                showSunday = showSunday,
                                daysToShow = daysToShow,
                                showDashedBorder = showDashedBorder,
                                courseNameFontSize = courseNameFontSize,
                                courseLocationFontSize = courseLocationFontSize,
                                selectedContextMenuSlot = null,
                                onCourseClick = EmptyCourseAction,
                                onEmptySlotClick = EmptyBiAction,
                                onCourseLongPress = { _, _ -> },
                                onEmptySlotLongPress = EmptyTriAction,
                                sharedTransitionScope = null,
                                animatedVisibilityScope = null
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
        } else {
            CourseEditContent(
                courseId = target.courseId,
                initialDayOfWeek = target.dayOfWeek,
                initialPeriod = target.periodIndex,
                initialPersonType = personType,
                onNavigateBack = { editTarget = null },
                animatedVisibilityScope = this@AnimatedContent,
                sharedElementSourceKey = target.sourceKey,
                viewModel = hiltViewModel(key = "edit_${target.courseId ?: "new"}_${personType.name}")
            )
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
                course?.let { editTarget = EditTarget(it.id, null, null, "courseCard_${it.id}") }
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
                    items(
                        items = (1..totalWeeks).toList(),
                        key = { week -> week }
                    ) { week ->
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

data class PeriodTimeRange(
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

private fun calculateCustomTimePosition(
    startHour: Int, startMinute: Int,
    endHour: Int, endMinute: Int,
    parsedPeriodTimes: List<PeriodTimeRange>,
    totalPeriods: Int
): Pair<Float, Float> {
    val startTotalMinutes = startHour * 60 + startMinute
    val endTotalMinutes = endHour * 60 + endMinute

    if (parsedPeriodTimes.isEmpty()) {
        return Pair(1f, 1f)
    }

    val gridStartMinutes = parsedPeriodTimes.first().startMinutes
    val gridEndMinutes = parsedPeriodTimes.last().endMinutes
    val gridTotalMinutes = gridEndMinutes - gridStartMinutes

    if (gridTotalMinutes <= 0) {
        return Pair(1f, 1f)
    }

    val clampedStart = startTotalMinutes.coerceIn(gridStartMinutes, gridEndMinutes)
    val clampedEnd = endTotalMinutes.coerceIn(gridStartMinutes, gridEndMinutes)

    val startFraction = (clampedStart - gridStartMinutes).toFloat() / gridTotalMinutes
    val endFraction = (clampedEnd - gridStartMinutes).toFloat() / gridTotalMinutes

    val fractionalStartPeriod = startFraction * totalPeriods + 1
    val fractionalSpan = (endFraction - startFraction) * totalPeriods

    return Pair(fractionalStartPeriod, fractionalSpan.coerceAtLeast(0.3f))
}

@Immutable
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
        val startPeriod = if (course.isCustomTime) {
            getPeriodFromTimeFast(course.startHour, course.startMinute, parsedPeriodTimes)
        } else if (course.startPeriod > 0) {
            course.startPeriod
        } else {
            getPeriodFromTimeFast(course.startHour, course.startMinute, parsedPeriodTimes)
        }

        val endPeriod = if (course.isCustomTime) {
            getPeriodFromTimeFast(course.endHour, course.endMinute, parsedPeriodTimes)
        } else if (course.endPeriod > 0) {
            course.endPeriod
        } else {
            getPeriodFromTimeFast(course.endHour, course.endMinute, parsedPeriodTimes)
        }

        val clampedEndPeriod = endPeriod.coerceAtLeast(startPeriod)
        val span = (clampedEndPeriod - startPeriod + 1).coerceAtLeast(1)

        for (period in startPeriod..clampedEndPeriod) {
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

    var result = 1
    parsedPeriodTimes.forEachIndexed { index, range ->
        if (totalMinutes >= range.startMinutes) {
            result = index + 1
        }
    }

    return result
}

@Immutable
data class EmptySlotPosition(
    val dayOfWeek: Int,
    val period: Int
)

@Immutable
private data class CourseLayoutInfo(
    val course: Course,
    val dayIndex: Int,
    val startPeriod: Float,
    val span: Float,
    val isCustomTime: Boolean = false
)

@Composable
fun WeeklyScheduleGrid(
    courseSlotMap: Map<Pair<Int, Int>, CourseSlotInfo>,
    currentWeek: Int,
    personColor: Color,
    weekDates: List<LocalDate>,
    totalPeriods: Int,
    periodTimes: List<String>,
    parsedPeriodTimes: List<PeriodTimeRange>,
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
    onEmptySlotLongPress: (Int, Int, CellBounds) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
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
    
    val uniqueCourses = remember(courseSlotMap, parsedPeriodTimes, totalPeriods) {
        val seen = mutableSetOf<Long>()
        courseSlotMap.entries
            .filter { it.value.isStart }
            .map { entry ->
                val dayOfWeek = entry.key.first
                val course = entry.value.course
                if (course.isCustomTime) {
                    val (fractionalStart, fractionalSpan) = calculateCustomTimePosition(
                        course.startHour, course.startMinute,
                        course.endHour, course.endMinute,
                        parsedPeriodTimes,
                        totalPeriods
                    )
                    CourseLayoutInfo(
                        course = course,
                        dayIndex = dayOfWeek - 1,
                        startPeriod = fractionalStart,
                        span = fractionalSpan,
                        isCustomTime = true
                    )
                } else {
                    CourseLayoutInfo(
                        course = course,
                        dayIndex = dayOfWeek - 1,
                        startPeriod = course.startPeriod.toFloat(),
                        span = entry.value.span.toFloat()
                    )
                }
            }
            .filter { seen.add(it.course.id) }
    }
    
    var columnWidth by remember { mutableStateOf(0) }
    var gridOffsetY by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        onEmptySlotFirstClick = { dayOfWeek: Int, periodIndex: Int ->
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
                                isCustomTime = layoutInfo.isCustomTime,
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
                                gridOffsetY = gridOffsetY,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun CourseOverlayCard(
    course: Course,
    dayIndex: Int,
    startPeriod: Float,
    span: Float,
    isCustomTime: Boolean,
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
    gridOffsetY: Int,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    val isCurrentWeekCourse = course.isInWeek(currentWeek)
    val darkTheme = LocalDarkTheme.current
    
    val fillShadow = getLiquidGlassFillShadow()
    val shadowColor = getLiquidGlassShadowColor()
    val targetCornerRadius = when (animatedVisibilityScope?.transition?.targetState) {
        EnterExitState.PreEnter -> BorderRadius.iOS26.xxlarge
        EnterExitState.Visible -> BorderRadius.iOS26.medium
        EnterExitState.PostExit -> BorderRadius.iOS26.xxlarge
        else -> BorderRadius.iOS26.medium
    }
    val cornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = ContainerTransformSpring,
        label = "card_corner"
    )
    val shape = ContinuousRoundedRectangle(cornerRadius.coerceAtLeast(0.dp))
    
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = MicroTween,
        label = "card_scale"
    )
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }
    
    val cardWidth = columnWidth - cellPaddingPx * 2
    val singleCellHeightPx = with(density) { cellHeight.dp.toPx().toInt() }
    val totalHeightPx = (singleCellHeightPx * span + cellSpacing * (span - 1)).roundToInt()

    val offsetX = timeColumnWidth + dayIndex * columnWidth + cellPaddingPx
    val offsetY = ((startPeriod - 1) * (singleCellHeightPx + cellSpacing) + cellPaddingPx).roundToInt()
    
    var cardBounds by remember { mutableStateOf(CellBounds(0, 0, 0, 0)) }

    val courseColor = remember(course.name, darkTheme) { getCourseColor(course.name, darkTheme) }
    val baseAlpha = if (showNonCurrentWeekCourses && !isCurrentWeekCourse) {
        if (darkTheme) 0.35f else 0.30f
    } else {
        if (darkTheme) 0.92f else 0.82f
    }
    val gradientBrush = remember(courseColor, baseAlpha) {
        val topAlpha = if (darkTheme) baseAlpha * 0.98f else baseAlpha * 0.92f
        val bottomAlpha = if (darkTheme) baseAlpha * 0.88f else baseAlpha * 0.80f
        Brush.verticalGradient(
            colors = listOf(
                courseColor.copy(alpha = topAlpha),
                courseColor.copy(alpha = bottomAlpha)
            )
        )
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .width(with(density) { cardWidth.toDp() })
            .height(with(density) { totalHeightPx.toDp() })
            .then(
                sharedTransitionScope?.let { scope ->
                    animatedVisibilityScope?.let { avScope ->
                        with(scope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "courseCard_${course.id}"),
                                animatedVisibilityScope = avScope,
                                boundsTransform = { _, _ -> spring(dampingRatio = 0.9f, stiffness = 600f) },
                                renderInOverlayDuringTransition = true,
                                clipInOverlayDuringTransition = OverlayClip(
                                    ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
                                )
                            )
                        }
                    }
                } ?: Modifier
            )
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
                    Modifier.background(gradientBrush)
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
        val courseTextColor = Color.White.copy(alpha = textAlpha)
        val courseSecondaryTextColor = Color.White.copy(alpha = textAlpha * 0.8f)
        val courseSeparatorColor = Color.White.copy(alpha = 0.25f)
        
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
                color = courseTextColor,
                textAlign = TextAlign.Center
            )

            if (isCustomTime) {
                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(courseSeparatorColor)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = course.getTimeString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = courseLocationFontSize.sp),
                    maxLines = 1,
                    color = courseSecondaryTextColor,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (course.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(courseSeparatorColor)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = course.location,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = courseLocationFontSize.sp),
                    maxLines = ScheduleDimensions.LocationMaxLines,
                    color = courseSecondaryTextColor,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
