package com.duoschedule.ui.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import com.duoschedule.ui.settings.components.SettingsDefaults
import com.duoschedule.ui.settings.components.SettingsSection
import com.duoschedule.ui.theme.Separator
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity

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

    val weekPickerState = rememberModalBottomSheetState()
    val periodPickerState = rememberModalBottomSheetState()
    var showWeekPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

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
            CourseEditTopBar(
                isEditing = state.isEditing,
                onNavigateBack = onNavigateBack,
                onDelete = { showDeleteDialog = true },
                onSave = viewModel::saveCourse
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.iOS26.groupSpacing),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ErrorMessageCard(
                        message = state.errorMessage ?: "",
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    )
                }
            }

            item {
                CourseNameSection(
                    name = state.name,
                    onNameChange = { 
                        viewModel.setName(it)
                        showCourseNameSuggestions = it.isNotEmpty()
                    },
                    courseHistory = courseHistory,
                    showSuggestions = showCourseNameSuggestions,
                    onSuggestionClick = { history ->
                        viewModel.selectFromHistory(history)
                        showCourseNameSuggestions = false
                    },
                    onDismissSuggestions = { showCourseNameSuggestions = false }
                )
            }

            item {
                PersonTypeSection(
                    personType = state.personType,
                    onPersonTypeChange = viewModel::setPersonType
                )
            }

            item {
                CourseDetailsSection(
                    dayOfWeek = state.dayOfWeek,
                    startPeriod = state.startPeriod,
                    endPeriod = state.endPeriod,
                    selectedWeeks = state.selectedWeeks,
                    totalWeeks = totalWeeks,
                    location = state.location,
                    teacher = state.teacher,
                    teacherHistory = teacherHistory,
                    showTeacherSuggestions = showTeacherSuggestions,
                    onPeriodClick = { showPeriodPicker = true },
                    onWeekClick = { showWeekPicker = true },
                    onLocationChange = viewModel::setLocation,
                    onTeacherChange = { 
                        viewModel.setTeacher(it)
                        showTeacherSuggestions = it.isNotEmpty()
                    },
                    onTeacherSuggestionClick = { teacher ->
                        viewModel.setTeacher(teacher)
                        showTeacherSuggestions = false
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
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
        GlassAlert(
            onDismissRequest = { showDeleteDialog = false },
            title = "删除课程",
            text = "确定要删除这门课程吗？此操作不可撤销。",
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        viewModel.deleteCourse()
                        showDeleteDialog = false
                    },
                    text = "删除",
                    style = LiquidGlassButtonStyle.Tinted
                )
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { showDeleteDialog = false },
                    text = "取消",
                    style = LiquidGlassButtonStyle.NonTinted
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseEditTopBar(
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    
    TopAppBar(
        title = { 
            Text(
                text = if (isEditing) "编辑课程" else "添加课程",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
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
                    Icons.Default.ArrowBack, 
                    contentDescription = "返回",
                    tint = labelsPrimary
                )
            }
        },
        actions = {
            if (isEditing) {
                GlassSymbolIconButton(
                    onClick = onDelete,
                    style = GlassSymbolButtonStyle.NonTinted,
                    size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "删除",
                        tint = SemanticColors.ErrorLight
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
            }
            GlassSymbolIconButton(
                onClick = onSave,
                style = GlassSymbolButtonStyle.Tinted,
                size = ComponentSize.LiquidGlassButton.TopAppBarIconButtonSize
            ) {
                Icon(
                    Icons.Default.Check, 
                    contentDescription = "保存",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(Spacing.sm))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier,
    backdrop: com.kyant.backdrop.Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.container) },
                effects = {
                    vibrancy()
                    blur(with(density) { GlassBottomSheetDefaults.BlurRadius.toPx() })
                    lens(
                        refractionHeight = with(density) { GlassBottomSheetDefaults.LensRefractionHeight.toPx() },
                        refractionAmount = with(density) { GlassBottomSheetDefaults.LensRefractionAmount.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(SemanticColors.ErrorLight, blendMode = BlendMode.Hue)
                    drawRect(SemanticColors.ErrorLight.copy(alpha = 0.1f))
                }
            )
            .padding(Spacing.lg)
    ) {
        Text(
            text = message,
            color = SemanticColors.ErrorLight,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CourseNameSection(
    name: String,
    onNameChange: (String) -> Unit,
    courseHistory: List<CourseHistoryItem>,
    showSuggestions: Boolean,
    onSuggestionClick: (CourseHistoryItem) -> Unit,
    onDismissSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    
    SettingsSection(
        title = "课程名称",
        modifier = modifier
    ) {
        GlassTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "请输入课程名称",
            transparentBackground = true
        )

        AnimatedVisibility(
            visible = showSuggestions && courseHistory.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val filteredHistory = courseHistory.filter {
                it.name.contains(name, ignoreCase = true) && it.name != name
            }.take(3)

            if (filteredHistory.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(
                        text = "历史课程",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelsSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    filteredHistory.forEach { history ->
                        SuggestionItem(
                            text = history.name,
                            onClick = { onSuggestionClick(history) }
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonTypeSection(
    personType: PersonType,
    onPersonTypeChange: (PersonType) -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    
    SettingsSection(
        title = "所属课表",
        modifier = modifier
    ) {
        val personTypeOptions = listOf(
            SegmentOption(PersonType.PERSON_B, "我的课表"),
            SegmentOption(PersonType.PERSON_A, "Ta 的课表")
        )

        SegmentedControl(
            options = personTypeOptions,
            selectedOption = personType,
            onOptionSelected = onPersonTypeChange
        )
    }
}

@Composable
private fun CourseDetailsSection(
    dayOfWeek: Int,
    startPeriod: Int,
    endPeriod: Int,
    selectedWeeks: Set<Int>,
    totalWeeks: Int,
    location: String,
    teacher: String,
    teacherHistory: List<String>,
    showTeacherSuggestions: Boolean,
    onPeriodClick: () -> Unit,
    onWeekClick: () -> Unit,
    onLocationChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit,
    onTeacherSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "课程详情",
        modifier = modifier
    ) {
        CourseEditNavigationRow(
            icon = Icons.Default.Schedule,
            title = "上课时间",
            value = getPeriodDisplayText(dayOfWeek, startPeriod, endPeriod),
            iconBackgroundColor = IOSColors.Blue,
            onClick = onPeriodClick
        )

        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

        CourseEditNavigationRow(
            icon = Icons.Default.CalendarMonth,
            title = "上课周数",
            value = getWeekDisplayText(selectedWeeks, totalWeeks),
            iconBackgroundColor = IOSColors.Orange,
            onClick = onWeekClick
        )

        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

        LocationInputRow(
            location = location,
            onLocationChange = onLocationChange
        )

        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

        TeacherInputRow(
            teacher = teacher,
            onTeacherChange = onTeacherChange,
            teacherHistory = teacherHistory,
            showSuggestions = showTeacherSuggestions,
            onSuggestionClick = onTeacherSuggestionClick
        )
    }
}

@Composable
private fun CourseEditNavigationRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconBackgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "row_scale"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = SettingsDefaults.ItemVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SettingsDefaults.IconBackgroundSize)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.icon))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SettingsDefaults.IconSize),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = labelsPrimary,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.width(Spacing.xs))
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = labelsSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LocationInputRow(
    location: String,
    onLocationChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SettingsDefaults.ItemVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SettingsDefaults.IconBackgroundSize)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.icon))
                .background(IOSColors.Green),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(SettingsDefaults.IconSize),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        Text(
            text = "教室地点",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = labelsPrimary,
            modifier = Modifier.width(72.dp)
        )
        
        GlassTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.sm),
            placeholder = "点击输入",
            transparentBackground = true,
            singleLine = true
        )
    }
}

@Composable
private fun TeacherInputRow(
    teacher: String,
    onTeacherChange: (String) -> Unit,
    teacherHistory: List<String>,
    showSuggestions: Boolean,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SettingsDefaults.ItemVerticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(SettingsDefaults.IconBackgroundSize)
                    .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.icon))
                    .background(IOSColors.Purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsDefaults.IconSize),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Text(
                text = "上课老师",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = labelsPrimary,
                modifier = Modifier.width(72.dp)
            )
            
            GlassTextField(
                value = teacher,
                onValueChange = onTeacherChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.sm),
                placeholder = "点击输入（可选）",
                transparentBackground = true,
                singleLine = true
            )
        }

        AnimatedVisibility(
            visible = showSuggestions && teacherHistory.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val filteredTeachers = teacherHistory.filter {
                it.contains(teacher, ignoreCase = true) && it != teacher
            }.take(3)

            if (filteredTeachers.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(
                        start = SettingsDefaults.IconBackgroundSize + Spacing.md + 72.dp + Spacing.md,
                        top = Spacing.sm
                    )
                ) {
                    filteredTeachers.forEach { teacherName ->
                        SuggestionChip(
                            text = teacherName,
                            onClick = { onSuggestionClick(teacherName) }
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getFillsVibrantTertiary()
    val labelsPrimary = getLabelsVibrantPrimary()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "suggestion_scale"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = ContinuousRoundedRectangle(BorderRadius.iOS26.container),
        color = backgroundColor,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(Spacing.md),
            color = labelsPrimary
        )
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getFillsVibrantTertiary()
    val labelsPrimary = getLabelsVibrantPrimary()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "chip_scale"
    )
    
    Surface(
        modifier = modifier.scale(scale),
        shape = ContinuousRoundedRectangle(BorderRadius.iOS26.container),
        color = backgroundColor,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            color = labelsPrimary
        )
    }
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
