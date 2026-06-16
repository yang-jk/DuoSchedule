package com.duoschedule.ui.schedule

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ScrollState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Todo
import com.duoschedule.ui.edit.CoursePreviewBottomSheet
import com.duoschedule.ui.edit.TodoPreviewBottomSheet
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.settings.components.GlassConfirmDialog
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.window.WindowListPopup
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt
import kotlin.math.tanh
import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.collectLatest
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
import com.duoschedule.ui.theme.InteractiveHighlight
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
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)

private val MicroTween: TweenSpec<Float> = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    personType: PersonType,
    onNavigateToEdit: (Long?, Int?, Int?, PersonType?) -> Unit,
    onNavigateToTodoEdit: (Long?, Long?, PersonType?, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _ -> },
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

    var selectedWeek by remember { mutableIntStateOf(1) }
    var showWeekSelector by remember { mutableStateOf(false) }

    val isDataReady by remember {
        derivedStateOf {
            totalPeriods > 0 && currentWeek > 0 && totalWeeks > 0
        }
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
    
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var courseToDelete by remember { mutableStateOf<Course?>(null) }

    val todoPreviewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTodoPreview by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<Todo?>(null) }
    var showTodoDeleteConfirm by remember { mutableStateOf(false) }
    
    var showAddMenu by remember { mutableStateOf(false) }

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

    LaunchedEffect(selectedTodo) {
        if (selectedTodo != null) {
            showTodoPreview = true
        }
    }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    val appThemeMode = LocalAppThemeMode.current
    val contentBackdrop = kyantRememberLayerBackdrop()

    val sharedTransitionScope = LocalSharedTransitionScope.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            SmallTopAppBar(
                title = "${personName}的课表",
                scrollBehavior = null,
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                defaultWindowInsetsPadding = true,
                actions = {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "第 $selectedWeek 周",
                        color = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable { showWeekSelector = !showWeekSelector }
                    )
                    Box {
                        IconButton(
                            onClick = { showAddMenu = !showAddMenu }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加", tint = MiuixTheme.colorScheme.onBackground)
                        }
                        WindowListPopup(
                            show = showAddMenu,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showAddMenu = false },
                            enableWindowDim = false
                        ) {
                            val dismissState = LocalDismissState.current
                            ListPopupColumn {
                                DropdownImpl(
                                    text = "添加课程",
                                    optionSize = 2,
                                    isSelected = false,
                                    index = 0,
                                    onSelectedIndexChange = {
                                        onNavigateToEdit(null, null, null, personType)
                                        dismissState?.invoke()
                                    }
                                )
                                DropdownImpl(
                                    text = "添加待办",
                                    optionSize = 2,
                                    isSelected = false,
                                    index = 1,
                                    onSelectedIndexChange = {
                                        onNavigateToTodoEdit(null, LocalDate.now().toEpochDay(), personType, -1, -1, -1, -1)
                                        dismissState?.invoke()
                                    }
                                )
                            }
                        }
                    }
                }
            )
        } else {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        CompositionLocalProvider(LocalBackdrop provides contentBackdrop) {
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
                var addButtonHeight by remember { mutableIntStateOf(0) }
                GlassSymbolIconButton(
                    onClick = { showAddMenu = !showAddMenu },
                    style = GlassSymbolButtonStyle.NonTinted,
                    modifier = Modifier.onGloballyPositioned { addButtonHeight = it.size.height }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加", tint = labelsPrimary)
                }
                if (showAddMenu) {
                    val localDensity = LocalDensity.current
                    val addMenuAnimationScope = rememberCoroutineScope()
                    val addMenuInteractiveHighlight = remember(addMenuAnimationScope) {
                        InteractiveHighlight(
                            animationScope = addMenuAnimationScope
                        )
                    }
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, addButtonHeight + with(localDensity) { 4.dp.roundToPx() }),
                        properties = PopupProperties(dismissOnBackPress = true, dismissOnClickOutside = true, focusable = true),
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        val backdrop = LocalBackdrop.current ?: emptyBackdrop()
                        val cornerRadius = BorderRadius.iOS26.container
                        val separatorColor = if (darkTheme) Color(0x26FFFFFF) else Color(0x14000000)
                        val containerColor = if (darkTheme) Color(0xFF121212).copy(alpha = 0.4f) else Color(0xFFFAFAFA).copy(alpha = 0.6f)

                        Box(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { ContinuousRoundedRectangle(cornerRadius) },
                                    effects = {
                                        colorControls(
                                            brightness = if (darkTheme) 0f else 0.2f,
                                            saturation = 1.5f
                                        )
                                        blur(with(localDensity) { 8.dp.toPx() })
                                        lens(
                                            refractionHeight = with(localDensity) { 24.dp.toPx() },
                                            refractionAmount = with(localDensity) { 48.dp.toPx() },
                                            chromaticAberration = true,
                                            depthEffect = true
                                        )
                                    },
                                    layerBlock = {
                                        val progress = addMenuInteractiveHighlight.pressProgress
                                        val scale = lerp(1f, 1f + 2f.dp.toPx() / size.height, progress)

                                        val maxOffset = size.minDimension
                                        val initialDerivative = 0.05f
                                        val offset = addMenuInteractiveHighlight.offset
                                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                                        val maxDragScale = 2f.dp.toPx() / size.height
                                        val offsetAngle = atan2(offset.y, offset.x)
                                        scaleX =
                                            scale +
                                                    maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                                    (size.width / size.height).fastCoerceAtMost(1f)
                                        scaleY =
                                            scale +
                                                    maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                                    (size.height / size.width).fastCoerceAtMost(1f)
                                    },
                                    highlight = { Highlight.Plain },
                                    onDrawSurface = { drawRect(containerColor) }
                                )
                                .then(addMenuInteractiveHighlight.modifier)
                                .then(addMenuInteractiveHighlight.gestureModifier)
                                .padding(4.dp)
                        ) {
                            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                                IOSMenuItem(
                                    text = "添加课程",
                                    darkTheme = darkTheme,
                                    onClick = {
                                        onNavigateToEdit(null, null, null, personType)
                                        showAddMenu = false
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .padding(horizontal = 8.dp)
                                        .background(separatorColor)
                                )
                                IOSMenuItem(
                                    text = "添加待办",
                                    darkTheme = darkTheme,
                                    onClick = {
                                        onNavigateToTodoEdit(null, LocalDate.now().toEpochDay(), personType, -1, -1, -1, -1)
                                        showAddMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        } // end CompositionLocalProvider
        }

        Column(
            modifier = Modifier.fillMaxSize().kyantLayerBackdrop(contentBackdrop)
        ) {
                key(isDataReady) {
                if (isDataReady) {
                val pagerState = rememberPagerState(
                    initialPage = (currentWeek - 1).coerceIn(0, kotlin.math.max(totalWeeks - 1, 0)),
                    pageCount = { totalWeeks }
                )
                var sharedScrollPosition by remember { mutableIntStateOf(0) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(pagerState.currentPage) {
                    selectedWeek = pagerState.currentPage + 1
                }

                if (showWeekSelector) {
                    WeekSelectorDropdown(
                        totalWeeks = totalWeeks,
                        currentWeek = currentWeek,
                        selectedWeek = selectedWeek,
                        onWeekSelected = { week ->
                            showWeekSelector = false
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(week - 1)
                            }
                        },
                        onDismiss = { showWeekSelector = false },
                        backdrop = LocalBackdrop.current ?: emptyBackdrop()
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = true
                ) { page ->
                    val week = page + 1
                    
                    // Compute week-specific data inside each page
                    val weekDates = remember(semesterStartDate, week) {
                        viewModel.getWeekDates(semesterStartDate, week)
                    }
                    
                    val weekCourses = remember(courses, week, showNonCurrentWeekCourses) {
                        if (showNonCurrentWeekCourses) courses else courses.filter { it.isInWeek(week) }
                    }
                    
                    // 获取当前周的待办数据
                    val weekTodosFlow = remember(weekDates) {
                        if (weekDates.isEmpty()) {
                            kotlinx.coroutines.flow.flowOf(emptyList<Todo>())
                        } else {
                            viewModel.getTodosForWeek(
                                personType,
                                weekDates.first().toEpochDay(),
                                weekDates.last().toEpochDay()
                            )
                        }
                    }
                    val weekTodos by weekTodosFlow.collectAsState(initial = emptyList())
                    
                    val courseSlotMap = remember(weekCourses, parsedPeriodTimes) {
                        buildCourseSlotMap(weekCourses, parsedPeriodTimes)
                    }
                    
                    val scrollState = remember { ScrollState(sharedScrollPosition) }
                    
                    LaunchedEffect(scrollState) {
                        snapshotFlow { scrollState.value }
                            .collectLatest { sharedScrollPosition = it }
                    }
                    
                    WeeklyScheduleGrid(
                        courseSlotMap = courseSlotMap,
                        currentWeek = week,
                        personColor = personColor,
                        weekDates = weekDates,
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
                        todos = weekTodos,
                        onCourseClick = handleCourseClick,
                        onEmptySlotClick = { dayOfWeek, periodIndex ->
                            onNavigateToEdit(null, dayOfWeek, periodIndex, personType)
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
                                ContextMenuItem(label = "复制") {
                                    CourseClipboard.copy(course)
                                },
                                ContextMenuItem(label = "编辑") {
                                    showContextMenu = false
                                    onNavigateToEdit(course.id, null, null, personType)
                                },
                                ContextMenuItem(label = "删除", isDestructive = true) {
                                    courseToDelete = course
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
                                    ContextMenuItem(label = "粘贴") {
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
                                ContextMenuItem(label = "添加课程") {
                                    showContextMenu = false
                                    onNavigateToEdit(null, dayOfWeek, period, personType)
                                }
                            )

                            // 添加待办选项
                            val dateForSlot = if (dayOfWeek - 1 < weekDates.size) weekDates[dayOfWeek - 1] else null
                            val timeRange = displayPeriodTimes.getOrNull(period - 1)
                            val slotStartHour: Int
                            val slotStartMinute: Int
                            val slotEndHour: Int
                            val slotEndMinute: Int
                            if (timeRange != null) {
                                val parts = timeRange.split("-")
                                val sp = parts.getOrNull(0)?.split(":") ?: listOf("8", "0")
                                val ep = parts.getOrNull(1)?.split(":") ?: listOf("8", "45")
                                slotStartHour = sp.getOrNull(0)?.toIntOrNull() ?: 8
                                slotStartMinute = sp.getOrNull(1)?.toIntOrNull() ?: 0
                                slotEndHour = ep.getOrNull(0)?.toIntOrNull() ?: 8
                                slotEndMinute = ep.getOrNull(1)?.toIntOrNull() ?: 45
                            } else {
                                slotStartHour = 8
                                slotStartMinute = 0
                                slotEndHour = 8
                                slotEndMinute = 45
                            }
                            items.add(
                                ContextMenuItem(label = "添加待办") {
                                    showContextMenu = false
                                    onNavigateToTodoEdit(null, dateForSlot?.toEpochDay(), personType, slotStartHour, slotStartMinute, slotEndHour, slotEndMinute)
                                }
                            )
                            
                            contextMenuItems = items
                            showContextMenu = true
                        },
                        onTodoClick = { todo ->
                            selectedTodo = todo
                        },
                        editingCourseId = null,
                        animatedVisibilityScope = null,
                        scrollState = scrollState
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (appThemeMode == AppThemeMode.MIUIX) {
                        MiuixCircularProgressIndicator()
                    } else {
                        CircularProgressIndicator(
                            color = getLabelsVibrantPrimary()
                        )
                    }
                }
            }
            } // key(isDataReady)
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
                course?.let {
                    onNavigateToEdit(it.id, null, null, it.personType)
                }
            },
            onDelete = {
                showDeleteConfirm = true
            },
            sheetState = previewSheetState
        )
    }

    val deletingCourse = courseToDelete ?: selectedCourse
    if (showDeleteConfirm && deletingCourse != null) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除课程",
                onDismissRequest = {
                    showDeleteConfirm = false
                    courseToDelete = null
                }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除「${deletingCourse.name}」吗？",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            courseToDelete = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteCourse(deletingCourse.id)
                            showDeleteConfirm = false
                            val fromContextMenu = courseToDelete != null
                            courseToDelete = null
                            if (!fromContextMenu) {
                                showPreview = false
                                selectedCourse = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
        GlassConfirmDialog(
            backdrop = LocalBackdrop.current ?: emptyBackdrop(),
            title = "删除课程",
            message = "确定要删除「${deletingCourse.name}」吗？",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = {
                viewModel.deleteCourse(deletingCourse.id)
                showDeleteConfirm = false
                val fromContextMenu = courseToDelete != null
                courseToDelete = null
                if (!fromContextMenu) {
                    showPreview = false
                    selectedCourse = null
                }
            },
            onDismiss = {
                showDeleteConfirm = false
                courseToDelete = null
            }
        )
        }
    }

    if (showTodoPreview && selectedTodo != null) {
        TodoPreviewBottomSheet(
            todo = selectedTodo!!,
            onDismiss = {
                showTodoPreview = false
                selectedTodo = null
            },
            onEdit = {
                showTodoPreview = false
                val todo = selectedTodo
                selectedTodo = null
                todo?.let {
                    onNavigateToTodoEdit(it.id, null, null, -1, -1, -1, -1)
                }
            },
            onDelete = {
                showTodoDeleteConfirm = true
            },
            sheetState = todoPreviewSheetState
        )
    }

    if (showTodoDeleteConfirm && selectedTodo != null) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除待办",
                onDismissRequest = {
                    showTodoDeleteConfirm = false
                }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除「${selectedTodo?.title}」吗？",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = { showTodoDeleteConfirm = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteTodo(selectedTodo!!.id)
                            showTodoDeleteConfirm = false
                            showTodoPreview = false
                            selectedTodo = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
            GlassConfirmDialog(
                backdrop = LocalBackdrop.current ?: emptyBackdrop(),
                title = "删除待办",
                message = "确定要删除「${selectedTodo?.title}」吗？",
                confirmText = "删除",
                dismissText = "取消",
                onConfirm = {
                    viewModel.deleteTodo(selectedTodo!!.id)
                    showTodoDeleteConfirm = false
                    showTodoPreview = false
                    selectedTodo = null
                },
                onDismiss = { showTodoDeleteConfirm = false }
            )
        }
    }

    if (showPasteConflictDialog && pendingPasteNewCourse != null && pendingPasteConflictCourse != null && pendingPasteSlot != null) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "时间冲突",
                onDismissRequest = { showPasteConflictDialog = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "该时间段已有课程「${pendingPasteConflictCourse!!.name}」，是否覆盖？",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Button(
                        onClick = { showPasteConflictDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("覆盖")
                    }
                }
            }
        } else {
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
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "选择周次",
            onDismissRequest = onDismiss
        ) {
            Surface(
                color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(BorderRadius.lg),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.height(300.dp).padding(vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items(
                        items = (1..totalWeeks).toList(),
                        key = { week -> week }
                    ) { week ->
                        val isSelected = week == selectedWeek
                        val isCurrent = week == currentWeek

                        Surface(
                            color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(BorderRadius.lg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWeekSelected(week) }
                                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = "第 $week 周",
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isCurrent) {
                                    top.yukonga.miuix.kmp.basic.Text(
                                        text = "当前",
                                        color = MiuixTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

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

/** 有时间段的待办块，使用虚线边框和半透明背景区分于课程块 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TodoOverlayCard(
    todo: Todo,
    dayIndex: Int,
    startPeriod: Float,
    span: Float,
    columnWidth: Int,
    cellHeight: Dp,
    cellSpacing: Int,
    timeColumnWidth: Int,
    onTodoClick: (Todo) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    val shape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.lg) else ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }
    
    val cardWidth = (columnWidth - cellPaddingPx * 2).coerceAtLeast(1)
    val singleCellHeightPx = with(density) { cellHeight.toPx().toInt() }
    val totalHeightPx = (singleCellHeightPx * span + cellSpacing * (span - 1)).roundToInt().coerceAtLeast(1)
    
    val offsetX = timeColumnWidth + dayIndex * columnWidth + cellPaddingPx
    val offsetY = ((startPeriod - 1) * (singleCellHeightPx + cellSpacing) + cellPaddingPx).roundToInt()
    
    // 根据人物类型选择颜色
    val personColor = if (todo.personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()
    val todoBackgroundColor = personColor.copy(alpha = 0.15f)
    val todoBorderColor = personColor.copy(alpha = 0.5f)
    val todoTextColor = if (darkTheme) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
    val todoTimeColor = if (darkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "todo_card_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "todo_card_alpha"
    )
    
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .width(with(density) { cardWidth.toDp() })
            .height(with(density) { totalHeightPx.toDp() })
            .scale(scale)
            .alpha(alpha)
            .clip(shape)
            .background(todoBackgroundColor)
            // 虚线边框，用于与课程块视觉区分
            .drawBehind {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val outline = shape.createOutline(size, layoutDirection, this)
                val borderPath = Path().apply { addOutline(outline) }
                drawPath(
                    path = borderPath,
                    color = todoBorderColor,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = pathEffect)
                )
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTodoClick(todo) }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 右上角复选框图标
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "待办",
                tint = personColor.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val nameMaxLines = ((cellHeight * span - 20.dp) / 14.dp).toInt().coerceIn(1, 4)
            Text(
                text = todo.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = nameMaxLines,
                overflow = TextOverflow.Ellipsis,
                color = todoTextColor,
                textAlign = TextAlign.Center
            )
            
            val shouldShowTime = span >= 1.5f || todo.isDeadlineOnly()
            if (shouldShowTime) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = if (todo.isDeadlineOnly()) "${todo.getEndTimeString()}前" else todo.getTimeString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    maxLines = 1,
                    color = todoTimeColor,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 无时间待办的浮动卡片，放在空位中 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun UntimedTodoOverlayCard(
    todo: Todo,
    dayIndex: Int,
    period: Int,
    columnWidth: Int,
    cellHeight: Dp,
    cellSpacing: Int,
    timeColumnWidth: Int,
    onTodoClick: (Todo) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    val shape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.lg) else ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }

    val cardWidth = (columnWidth - cellPaddingPx * 2).coerceAtLeast(1)
    val singleCellHeightPx = with(density) { cellHeight.toPx().toInt() }
    val totalHeightPx = singleCellHeightPx  // span = 1

    val offsetX = timeColumnWidth + dayIndex * columnWidth + cellPaddingPx
    val offsetY = (period - 1) * (singleCellHeightPx + cellSpacing) + cellPaddingPx

    val personColor = if (todo.personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()
    val todoBackgroundColor = personColor.copy(alpha = 0.12f)
    val todoBorderColor = personColor.copy(alpha = 0.45f)
    val todoTextColor = if (darkTheme) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "untimed_todo_card_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "untimed_todo_card_alpha"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .width(with(density) { cardWidth.toDp() })
            .height(with(density) { totalHeightPx.toDp() })
            .scale(scale)
            .alpha(alpha)
            .clip(shape)
            .graphicsLayer {
                shadowElevation = 16.dp.toPx()
                this.shape = shape
                clip = true
                ambientShadowColor = Color.Transparent
                spotShadowColor = Color.Transparent  // No visible shadow for todos
            }
            .background(todoBackgroundColor)
            .drawBehind {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                val outline = shape.createOutline(size, layoutDirection, this)
                val borderPath = Path().apply { addOutline(outline) }
                drawPath(
                    path = borderPath,
                    color = todoBorderColor,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = pathEffect)
                )
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTodoClick(todo) }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 右上角复选框图标
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "待办",
                tint = personColor.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = todoTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** 无时间待办的底部药丸，当天列无空位时在底部堆叠 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun UntimedTodoBottomPill(
    todo: Todo,
    dayIndex: Int,
    columnWidth: Int,
    timeColumnWidth: Int,
    offsetY: Int,
    onTodoClick: (Todo) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    val shape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.lg) else ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }

    val pillWidth = (columnWidth - cellPaddingPx * 2).coerceAtLeast(1)
    val offsetX = timeColumnWidth + dayIndex * columnWidth + cellPaddingPx

    val personColor = if (todo.personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()
    val pillBackgroundColor = personColor.copy(alpha = 0.10f)
    val pillTextColor = if (darkTheme) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.75f)

    // 优先级颜色
    val priorityColor = when (todo.priority) {
        com.duoschedule.data.model.Priority.HIGH -> Color(0xFFFF3B30)
        com.duoschedule.data.model.Priority.MEDIUM -> Color(0xFFFF9500)
        com.duoschedule.data.model.Priority.LOW -> Color(0xFF34C759)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "untimed_pill_scale"
    )

    Row(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .width(with(density) { pillWidth.toDp() })
            .height(20.dp)
            .scale(scale)
            .clip(shape)
            .graphicsLayer {
                shadowElevation = 16.dp.toPx()
                this.shape = shape
                clip = true
                ambientShadowColor = Color.Transparent
                spotShadowColor = Color.Transparent
            }
            .background(pillBackgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTodoClick(todo) }
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 优先级圆点
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(priorityColor)
        )
        Text(
            text = todo.title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = pillTextColor
        )
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

/** 待办在课表网格中的布局信息 */
@Immutable
private data class TodoLayoutInfo(
    val todo: Todo,
    val dayIndex: Int,
    val startPeriod: Float,
    val span: Float
)

/** 无时间待办的布局信息 */
@Immutable
private data class UntimedTodoLayoutInfo(
    val todo: Todo,
    val dayIndex: Int,
    val slotPeriod: Int,  // 放入的课时位置，-1 表示底部堆叠
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
    todos: List<Todo> = emptyList(),
    onCourseClick: (Course) -> Unit,
    onEmptySlotClick: (Int, Int) -> Unit,
    onCourseLongPress: (Course, CellBounds) -> Unit,
    onEmptySlotLongPress: (Int, Int, CellBounds) -> Unit,
    onTodoClick: (Todo) -> Unit = {},
    editingCourseId: Long? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    scrollState: ScrollState
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
    
    // 计算有时间段待办（SCHEDULE类型）的布局信息
    val scheduleTodos = remember(todos, parsedPeriodTimes, totalPeriods, weekDates) {
        todos.filter { it.hasTimeRange() }.mapNotNull { todo ->
            val dayIndex = weekDates.indexOf(LocalDate.ofEpochDay(todo.date))
            if (dayIndex < 0) return@mapNotNull null
            val (fractionalStart, fractionalSpan) = calculateCustomTimePosition(
                todo.startHour, todo.startMinute,
                todo.endHour, todo.endMinute,
                parsedPeriodTimes,
                totalPeriods
            )
            TodoLayoutInfo(
                todo = todo,
                dayIndex = dayIndex,
                startPeriod = fractionalStart,
                span = fractionalSpan
            )
        }
    }
    
    // 计算截止日期类型待办的布局信息（放在截止时间位置，占1课时）
    val deadlineTodos = remember(todos, parsedPeriodTimes, totalPeriods, weekDates) {
        todos.filter { it.isDeadlineOnly() }.mapNotNull { todo ->
            val dayIndex = weekDates.indexOf(LocalDate.ofEpochDay(todo.date))
            if (dayIndex < 0) return@mapNotNull null
            // Use end time to calculate position, span = 1
            val (fractionalStart, _) = calculateCustomTimePosition(
                todo.endHour, todo.endMinute,
                todo.endHour, todo.endMinute,
                parsedPeriodTimes,
                totalPeriods
            )
            TodoLayoutInfo(
                todo = todo,
                dayIndex = dayIndex,
                startPeriod = fractionalStart,
                span = 1f
            )
        }
    }

    // 计算无时间待办的布局信息
    val untimedTodos = remember(todos, courseSlotMap, weekDates) {
        todos.filter { !it.hasStartTime() && !it.hasEndTime() }.mapNotNull { todo ->
            val dayIndex = weekDates.indexOf(LocalDate.ofEpochDay(todo.date))
            if (dayIndex < 0) return@mapNotNull null

            // 在该天列中寻找第一个空位
            var emptySlotPeriod = -1
            for (period in 1..totalPeriods) {
                val key = Pair(dayIndex + 1, period)  // dayOfWeek is 1-based
                if (!courseSlotMap.containsKey(key)) {
                    emptySlotPeriod = period
                    break
                }
            }

            UntimedTodoLayoutInfo(
                todo = todo,
                dayIndex = dayIndex,
                slotPeriod = emptySlotPeriod  // -1 means no empty slot, stack at bottom
            )
        }
    }
    
    var columnWidth by remember { mutableIntStateOf(0) }
    var gridOffsetY by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bottomBarHeight = LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val headerAreaHeight = Spacing.sm + ScheduleDimensions.HeaderHeight + Spacing.sm + 1.dp + Spacing.sm + Spacing.sm
        val totalSpacing = Spacing.xxs * (totalPeriods - 1)
        val cellHeight = ((maxHeight - headerAreaHeight - totalSpacing - bottomBarHeight) / totalPeriods).coerceAtLeast(60.dp)
        val totalGridHeight = cellHeight * totalPeriods + totalSpacing
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalGridHeight)
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
                        cellHeight = cellHeight,
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
                                cellHeight = cellHeight,
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
                                sharedElementKey = if (layoutInfo.course.id == editingCourseId) "course_${layoutInfo.course.id}" else null,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                }
                
                // 渲染有时间段的待办块
                scheduleTodos.forEach { layoutInfo ->
                    key("todo_${layoutInfo.todo.id}") {
                        val dayIndex = layoutInfo.dayIndex
                        if (dayIndex >= 0 && dayIndex < dayOfWeekIndices.size) {
                            TodoOverlayCard(
                                todo = layoutInfo.todo,
                                dayIndex = dayIndex,
                                startPeriod = layoutInfo.startPeriod,
                                span = layoutInfo.span,
                                columnWidth = columnWidth,
                                cellHeight = cellHeight,
                                cellSpacing = cellSpacingPx,
                                timeColumnWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
                                    ScheduleDimensions.TimeColumnWidth.toPx().toInt()
                                },
                                onTodoClick = onTodoClick
                            )
                        }
                    }
                }
                
                // 渲染截止日期类型待办（放在截止时间位置）
                deadlineTodos.forEach { layoutInfo ->
                    key("deadline_${layoutInfo.todo.id}") {
                        val dayIndex = layoutInfo.dayIndex
                        if (dayIndex >= 0 && dayIndex < dayOfWeekIndices.size) {
                            TodoOverlayCard(
                                todo = layoutInfo.todo,
                                dayIndex = dayIndex,
                                startPeriod = layoutInfo.startPeriod,
                                span = layoutInfo.span,
                                columnWidth = columnWidth,
                                cellHeight = cellHeight,
                                cellSpacing = cellSpacingPx,
                                timeColumnWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
                                    ScheduleDimensions.TimeColumnWidth.toPx().toInt()
                                },
                                onTodoClick = onTodoClick
                            )
                        }
                    }
                }

                // 渲染无时间待办（空位浮动）
                untimedTodos.filter { it.slotPeriod > 0 }.forEach { layoutInfo ->
                    key("untimed_todo_${layoutInfo.todo.id}") {
                        val dayIndex = layoutInfo.dayIndex
                        if (dayIndex >= 0 && dayIndex < dayOfWeekIndices.size) {
                            UntimedTodoOverlayCard(
                                todo = layoutInfo.todo,
                                dayIndex = dayIndex,
                                period = layoutInfo.slotPeriod,
                                columnWidth = columnWidth,
                                cellHeight = cellHeight,
                                cellSpacing = cellSpacingPx,
                                timeColumnWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
                                    ScheduleDimensions.TimeColumnWidth.toPx().toInt()
                                },
                                onTodoClick = onTodoClick
                            )
                        }
                    }
                }

                // 渲染无时间待办（底部堆叠）
                val bottomUntimedTodos = untimedTodos.filter { it.slotPeriod < 0 }.groupBy { it.dayIndex }
                bottomUntimedTodos.forEach { (dayIndex, todosForDay) ->
                    todosForDay.forEachIndexed { index, layoutInfo ->
                        key("untimed_bottom_${layoutInfo.todo.id}") {
                            if (dayIndex >= 0 && dayIndex < dayOfWeekIndices.size) {
                                val gridBottomOffset = with(androidx.compose.ui.platform.LocalDensity.current) {
                                    totalGridHeight.toPx().toInt()
                                }
                                UntimedTodoBottomPill(
                                    todo = layoutInfo.todo,
                                    dayIndex = dayIndex,
                                    columnWidth = columnWidth,
                                    timeColumnWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
                                        ScheduleDimensions.TimeColumnWidth.toPx().toInt()
                                    },
                                    offsetY = gridBottomOffset + with(androidx.compose.ui.platform.LocalDensity.current) {
                                        (index * 22).dp.toPx().toInt()
                                    },
                                    onTodoClick = onTodoClick
                                )
                            }
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
    cellHeight: Dp,
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
    val appThemeMode = LocalAppThemeMode.current
    val overlayBackgroundColor = if (appThemeMode == AppThemeMode.MIUIX) {
        MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else if (darkTheme) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color.Black.copy(alpha = 0.18f)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellHeight)
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
    cellHeight: Dp,
    isSelected: Boolean,
    isContextMenuSelected: Boolean,
    overlayBackgroundColor: Color,
    onFirstClick: (Int, Int) -> Unit,
    onSecondClick: (Int, Int) -> Unit,
    onLongPress: (Int, Int, CellBounds) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    val shape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.lg) else ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
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
                .height(cellHeight)
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (isSelected) {
                        if (appThemeMode == AppThemeMode.MIUIX) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else overlayBackgroundColor
                    } else {
                        if (appThemeMode == AppThemeMode.MIUIX) MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        else if (darkTheme) Color.White.copy(alpha = 0.02f)
                        else Color.Black.copy(alpha = 0.01f)
                    }
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
private fun CourseOverlayCard(
    course: Course,
    dayIndex: Int,
    startPeriod: Float,
    span: Float,
    isCustomTime: Boolean,
    currentWeek: Int,
    columnWidth: Int,
    cellHeight: Dp,
    cellSpacing: Int,
    timeColumnWidth: Int,
    showNonCurrentWeekCourses: Boolean,
    courseNameFontSize: Int,
    courseLocationFontSize: Int,
    onCourseClick: (Course) -> Unit,
    onCourseLongPress: (Course, CellBounds) -> Unit,
    gridOffsetY: Int,
    sharedElementKey: String? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val isCurrentWeekCourse = course.isInWeek(currentWeek)
    val darkTheme = LocalDarkTheme.current
    val appThemeMode = LocalAppThemeMode.current
    
    val fillShadow = getLiquidGlassFillShadow()
    val shadowColor = if (appThemeMode == AppThemeMode.MIUIX) MiuixTheme.colorScheme.outline else getLiquidGlassShadowColor()
    val shape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.lg) else ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
    
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "card_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "card_alpha"
    )

    val density = androidx.compose.ui.platform.LocalDensity.current
    val cellPaddingPx = with(density) { ScheduleDimensions.CellPadding.toPx().toInt() }
    
    val cardWidth = (columnWidth - cellPaddingPx * 2).coerceAtLeast(1)
    val singleCellHeightPx = with(density) { cellHeight.toPx().toInt() }
    val totalHeightPx = (singleCellHeightPx * span + cellSpacing * (span - 1)).roundToInt().coerceAtLeast(1)

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

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val boxModifier = Modifier
        .offset { IntOffset(offsetX, offsetY) }
        .width(with(density) { cardWidth.toDp() })
        .height(with(density) { totalHeightPx.toDp() })
        .then(
            if (sharedElementKey != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        rememberSharedContentState(sharedElementKey),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else {
                Modifier
            }
        )
        .scale(scale)
        .alpha(alpha)
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
        .padding(horizontal = 2.dp, vertical = 2.dp)

    Box(
        modifier = boxModifier,
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
            val courseNameMaxLines = ((cellHeight * span - 24.dp) / 16.dp).toInt().coerceIn(2, 6)
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = courseNameFontSize.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = courseNameMaxLines,
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

@Composable
private fun IOSMenuItem(
    text: String,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "item_scale"
    )

    val pressedBackgroundColor = if (darkTheme) Color(0x33FFFFFF) else Color(0x14000000)

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 80.dp)
            .scale(scale)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.medium))
            .background(if (isPressed) pressedBackgroundColor else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            ),
            color = getLabelsVibrantPrimary(),
            maxLines = 1,
            softWrap = false
        )
    }
}
