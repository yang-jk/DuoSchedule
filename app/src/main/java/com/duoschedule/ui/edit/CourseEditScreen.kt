package com.duoschedule.ui.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.WeekType
import com.duoschedule.ui.settings.components.SettingsDefaults
import com.duoschedule.ui.settings.components.SettingsSection
import com.duoschedule.ui.theme.Separator
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.LocalAppSnackbarHostState
import com.duoschedule.ui.edit.CustomTimePickerBottomSheet
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.kyant.backdrop.drawBackdrop

import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalDensity
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.duoschedule.data.model.AppThemeMode
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.window.WindowDialog
import androidx.compose.foundation.shape.RoundedCornerShape

private val MicroTween: TweenSpec<Float> = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing)

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
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()

    val appSnackbarHostState = LocalAppSnackbarHostState.current
    val hapticFeedback = LocalHapticFeedback.current
    var isSaving by remember { mutableStateOf(false) }

    val weekPickerState = rememberModalBottomSheetState()
    val periodPickerState = rememberModalBottomSheetState()
    val customTimePickerState = rememberModalBottomSheetState()
    var showWeekPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    LaunchedEffect(courseId) {
        if (courseId != null && courseId > 0) {
            viewModel.loadCourse(courseId)
        } else {
            viewModel.resetForNewCourse()
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
        if (state.saved) {
            isSaving = false
            appSnackbarHostState?.showAppSnackbar("课程已保存")
            onNavigateBack()
            viewModel.resetNavigationState()
        } else if (state.deleted) {
            appSnackbarHostState?.showAppSnackbar("课程已删除")
            onNavigateBack()
            viewModel.resetNavigationState()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCourseNameSuggestions by remember { mutableStateOf(false) }
    var showTeacherSuggestions by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    val lazyListState = rememberLazyListState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val blurEnabled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0

    Scaffold(
        topBar = {
            val appThemeMode = LocalAppThemeMode.current
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "编辑课程",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        } else {
                            GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            if (state.isEditing) {
                                top.yukonga.miuix.kmp.basic.IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.error)
                                }
                            }
                            top.yukonga.miuix.kmp.basic.IconButton(
                                onClick = {
                                    if (!isSaving) {
                                        isSaving = true
                                        viewModel.saveCourse()
                                    }
                                },
                                enabled = !isSaving
                            ) {
                                AnimatedContent(
                                    targetState = isSaving,
                                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                    label = "save_button"
                                ) { saving ->
                                    if (saving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(Icons.Default.Check, contentDescription = "保存", tint = MiuixTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } else {
                            if (state.isEditing) {
                                GlassSymbolIconButton(onClick = { showDeleteDialog = true }, style = GlassSymbolButtonStyle.NonTinted) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = SemanticColors.ErrorLight)
                                }
                            }
                            GlassSymbolIconButton(
                                onClick = {
                                    if (!isSaving) {
                                        isSaving = true
                                        viewModel.saveCourse()
                                    }
                                },
                                enabled = !isSaving,
                                style = GlassSymbolButtonStyle.Tinted
                            ) {
                                AnimatedContent(
                                    targetState = isSaving,
                                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                    label = "save_button"
                                ) { saving ->
                                    if (saving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(Icons.Default.Check, contentDescription = "保存", tint = Color.White)
                                    }
                                }
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
    Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + Spacing.md,
                bottom = Spacing.iOS26.groupSpacing + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
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
                CourseNameAndPersonSection(
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
                    onDismissSuggestions = { showCourseNameSuggestions = false },
                    personType = state.personType,
                    onPersonTypeChange = viewModel::setPersonType,
                    showPersonType = !singleModeEnabled
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
                    isCustomTime = state.isCustomTime,
                    customStartHour = state.customStartHour,
                    customStartMinute = state.customStartMinute,
                    customEndHour = state.customEndHour,
                    customEndMinute = state.customEndMinute,
                    onTimeModeChange = viewModel::setTimeMode,
                    onPeriodClick = {
                        if (state.isCustomTime) {
                            showCustomTimePicker = true
                        } else {
                            showPeriodPicker = true
                        }
                    },
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

    if (showCustomTimePicker) {
        CustomTimePickerBottomSheet(
            selectedDayOfWeek = state.dayOfWeek,
            startHour = state.customStartHour,
            startMinute = state.customStartMinute,
            endHour = state.customEndHour,
            endMinute = state.customEndMinute,
            onSelectionChange = { dayOfWeek, startHour, startMinute, endHour, endMinute ->
                viewModel.setDayOfWeek(dayOfWeek)
                viewModel.setCustomTime(startHour, startMinute, endHour, endMinute)
            },
            onDismiss = { showCustomTimePicker = false },
            sheetState = customTimePickerState
        )
    }

    if (showDeleteDialog) {
        val appThemeMode = LocalAppThemeMode.current
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除课程",
                onDismissRequest = { showDeleteDialog = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除这门课程吗？此操作不可撤销。",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteCourse()
                            showDeleteDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
            GlassAlert(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteCourse()
                    showDeleteDialog = false
                },
                title = "删除课程",
                text = "确定要删除这门课程吗？此操作不可撤销。",
                confirmText = "删除",
                dismissText = "取消"
            )
        }
    }
    }
}

/**
 * 课程编辑弹窗内容，用于在 WindowDialog/ModalBottomSheet 中展示课程编辑表单。
 * 不包含 Scaffold/TopBar，由外部弹窗提供标题和容器。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditDialogContent(
    courseId: Long?,
    personType: PersonType,
    initialDayOfWeek: Int? = null,
    initialPeriodIndex: Int? = null,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val viewModel: CourseEditViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val totalWeeks by viewModel.totalWeeks.collectAsState()
    val totalPeriods by viewModel.totalPeriods.collectAsState()
    val periodTimes by viewModel.periodTimes.collectAsState()
    val courseHistory by viewModel.courseHistory.collectAsState()
    val teacherHistory by viewModel.teacherHistory.collectAsState()
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()
    val appThemeMode = LocalAppThemeMode.current

    val weekPickerState = rememberModalBottomSheetState()
    val periodPickerState = rememberModalBottomSheetState()
    val customTimePickerState = rememberModalBottomSheetState()
    var showWeekPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCourseNameSuggestions by remember { mutableStateOf(false) }
    var showTeacherSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (courseId != null && courseId > 0) {
            viewModel.loadCourse(courseId)
        } else {
            viewModel.resetForNewCourse(personType)
        }
    }

    LaunchedEffect(initialDayOfWeek, initialPeriodIndex) {
        if (courseId == null || courseId <= 0) {
            initialDayOfWeek?.let { viewModel.setInitialDayOfWeek(it) }
            initialPeriodIndex?.let { viewModel.setInitialPeriod(it) }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
            viewModel.resetNavigationState()
        }
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            onDeleted()
            viewModel.resetNavigationState()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentPadding = PaddingValues(
                top = Spacing.md,
                bottom = Spacing.lg
            ),
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
                CourseNameAndPersonSection(
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
                    onDismissSuggestions = { showCourseNameSuggestions = false },
                    personType = state.personType,
                    onPersonTypeChange = viewModel::setPersonType,
                    showPersonType = !singleModeEnabled
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
                    isCustomTime = state.isCustomTime,
                    customStartHour = state.customStartHour,
                    customStartMinute = state.customStartMinute,
                    customEndHour = state.customEndHour,
                    customEndMinute = state.customEndMinute,
                    onTimeModeChange = viewModel::setTimeMode,
                    onPeriodClick = {
                        if (state.isCustomTime) {
                            showCustomTimePicker = true
                        } else {
                            showPeriodPicker = true
                        }
                    },
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
        }

        // 底部按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (state.isEditing) {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除课程")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除课程")
                    }
                }
            }
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.Button(
                    onClick = viewModel::saveCourse,
                    modifier = Modifier.weight(1f),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("保存")
                }
            } else {
                androidx.compose.material3.Button(
                    onClick = viewModel::saveCourse,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
        }
    }

    // 选择器弹窗
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

    if (showCustomTimePicker) {
        CustomTimePickerBottomSheet(
            selectedDayOfWeek = state.dayOfWeek,
            startHour = state.customStartHour,
            startMinute = state.customStartMinute,
            endHour = state.customEndHour,
            endMinute = state.customEndMinute,
            onSelectionChange = { dayOfWeek, startHour, startMinute, endHour, endMinute ->
                viewModel.setDayOfWeek(dayOfWeek)
                viewModel.setCustomTime(startHour, startMinute, endHour, endMinute)
            },
            onDismiss = { showCustomTimePicker = false },
            sheetState = customTimePickerState
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除课程",
                onDismissRequest = { showDeleteDialog = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除这门课程吗？此操作不可撤销。",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = {
                            viewModel.deleteCourse()
                            showDeleteDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
            GlassAlert(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    viewModel.deleteCourse()
                    showDeleteDialog = false
                },
                title = "删除课程",
                text = "确定要删除这门课程吗？此操作不可撤销。",
                confirmText = "删除",
                dismissText = "取消"
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier,
    backdrop: com.kyant.backdrop.Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(BorderRadius.lg),
            color = MiuixTheme.colorScheme.errorContainer
        ) {
            top.yukonga.miuix.kmp.basic.Text(
                text = message,
                color = MiuixTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(Spacing.lg)
            )
        }
        return
    }

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
private fun CourseNameAndPersonSection(
    name: String,
    onNameChange: (String) -> Unit,
    courseHistory: List<CourseHistoryItem>,
    showSuggestions: Boolean,
    onSuggestionClick: (CourseHistoryItem) -> Unit,
    onDismissSuggestions: () -> Unit,
    personType: PersonType,
    onPersonTypeChange: (PersonType) -> Unit,
    showPersonType: Boolean,
    modifier: Modifier = Modifier
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    val appThemeMode = LocalAppThemeMode.current
    
    SettingsSection(
        title = "课程名称",
        modifier = modifier
    ) {
        if (showPersonType) {
            if (appThemeMode == AppThemeMode.MIUIX) {
                TabRowWithContour(
                    tabs = listOf("我的课表", "Ta的课表"),
                    selectedTabIndex = if (personType == PersonType.PERSON_A) 0 else 1,
                    onTabSelected = { onPersonTypeChange(if (it == 0) PersonType.PERSON_A else PersonType.PERSON_B) }
                )
            } else {
                val personTypeOptions = listOf(
                    SegmentOption(PersonType.PERSON_A, "我的课表"),
                    SegmentOption(PersonType.PERSON_B, "Ta的课表")
                )
                SegmentedControl(
                    options = personTypeOptions,
                    selectedOption = personType,
                    onOptionSelected = onPersonTypeChange
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }

        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = "请输入课程名称",
                useLabelAsPlaceholder = true
            )
        } else {
            GlassTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "请输入课程名称",
                transparentBackground = true
            )
        }

        AnimatedVisibility(
            visible = showSuggestions && courseHistory.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val filteredHistory by remember {
                derivedStateOf {
                    courseHistory.filter {
                        it.name.contains(name, ignoreCase = true) && it.name != name
                    }.take(3)
                }
            }

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
    isCustomTime: Boolean,
    customStartHour: Int,
    customStartMinute: Int,
    customEndHour: Int,
    customEndMinute: Int,
    onTimeModeChange: (Boolean) -> Unit,
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
        val appThemeMode = LocalAppThemeMode.current
        if (appThemeMode == AppThemeMode.MIUIX) {
            TabRowWithContour(
                tabs = listOf("按课节", "自定义时间"),
                selectedTabIndex = if (isCustomTime) 1 else 0,
                onTabSelected = { onTimeModeChange(it == 1) }
            )
        } else {
            SegmentedControl(
                options = listOf(
                    SegmentOption(false, "按课节"),
                    SegmentOption(true, "自定义时间")
                ),
                selectedOption = isCustomTime,
                onOptionSelected = onTimeModeChange
            )
        }

        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

        CourseEditNavigationRow(
            icon = Icons.Default.Schedule,
            title = "上课时间",
            value = if (isCustomTime) {
                getCustomTimeDisplayText(dayOfWeek, customStartHour, customStartMinute, customEndHour, customEndMinute)
            } else {
                getPeriodDisplayText(dayOfWeek, startPeriod, endPeriod)
            },
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
    val appThemeMode = LocalAppThemeMode.current
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MicroTween,
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
                .clip(iconShape)
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
    val appThemeMode = LocalAppThemeMode.current
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SettingsDefaults.ItemVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SettingsDefaults.IconBackgroundSize)
                .clip(iconShape)
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
        
        val appThemeMode = LocalAppThemeMode.current
        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(
                value = location,
                onValueChange = onLocationChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.sm),
                label = "点击输入",
                useLabelAsPlaceholder = true
            )
        } else {
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
    val appThemeMode = LocalAppThemeMode.current
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

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
                    .clip(iconShape)
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
            
            val appThemeMode = LocalAppThemeMode.current
            if (appThemeMode == AppThemeMode.MIUIX) {
                TextField(
                    value = teacher,
                    onValueChange = onTeacherChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.sm),
                    label = "点击输入（可选）",
                    useLabelAsPlaceholder = true
                )
            } else {
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
        }

        AnimatedVisibility(
            visible = showSuggestions && teacherHistory.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val filteredTeachers by remember {
                derivedStateOf {
                    teacherHistory.filter {
                        it.contains(teacher, ignoreCase = true) && it != teacher
                    }.take(3)
                }
            }

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
        animationSpec = MicroTween,
        label = "suggestion_scale"
    )
    
    val appThemeMode = LocalAppThemeMode.current
    if (appThemeMode == AppThemeMode.MIUIX) {
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(BorderRadius.lg),
            color = MiuixTheme.colorScheme.surfaceVariant,
            onClick = onClick
        ) {
            top.yukonga.miuix.kmp.basic.Text(
                text = text,
                modifier = Modifier.padding(Spacing.md),
                color = MiuixTheme.colorScheme.onBackground
            )
        }
        return
    }
    
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
        animationSpec = MicroTween,
        label = "chip_scale"
    )
    
    val appThemeMode = LocalAppThemeMode.current
    if (appThemeMode == AppThemeMode.MIUIX) {
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier.scale(scale),
            shape = RoundedCornerShape(BorderRadius.lg),
            color = MiuixTheme.colorScheme.surfaceVariant,
            onClick = onClick
        ) {
            top.yukonga.miuix.kmp.basic.Text(
                text = text,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                color = MiuixTheme.colorScheme.onBackground
            )
        }
        return
    }
    
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

private fun getCustomTimeDisplayText(dayOfWeek: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): String {
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val dayText = days.getOrNull(dayOfWeek - 1) ?: "周一"
    val startTime = String.format("%02d:%02d", startHour, startMinute)
    val endTime = String.format("%02d:%02d", endHour, endMinute)
    return "$dayText $startTime-$endTime"
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CourseEditContent(
    courseId: Long?,
    initialDayOfWeek: Int? = null,
    initialPeriod: Int? = null,
    initialPersonType: PersonType? = null,
    onNavigateBack: () -> Unit,
    viewModel: CourseEditViewModel = hiltViewModel(),
    sharedElementKey: String? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val state by viewModel.state.collectAsState()
    val totalWeeks by viewModel.totalWeeks.collectAsState()
    val totalPeriods by viewModel.totalPeriods.collectAsState()
    val periodTimes by viewModel.periodTimes.collectAsState()
    val courseHistory by viewModel.courseHistory.collectAsState()
    val teacherHistory by viewModel.teacherHistory.collectAsState()
    val singleModeEnabled by viewModel.singleModeEnabled.collectAsState()

    val appSnackbarHostState = LocalAppSnackbarHostState.current
    val hapticFeedback = LocalHapticFeedback.current
    var isSaving by remember { mutableStateOf(false) }

    val weekPickerState = rememberModalBottomSheetState()
    val periodPickerState = rememberModalBottomSheetState()
    val customTimePickerState = rememberModalBottomSheetState()
    var showWeekPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    LaunchedEffect(courseId) {
        if (courseId != null && courseId > 0) {
            viewModel.loadCourse(courseId)
        } else {
            viewModel.resetForNewCourse()
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
        if (state.saved) {
            isSaving = false
            appSnackbarHostState?.showAppSnackbar("课程已保存")
            onNavigateBack()
            viewModel.resetNavigationState()
        } else if (state.deleted) {
            appSnackbarHostState?.showAppSnackbar("课程已删除")
            onNavigateBack()
            viewModel.resetNavigationState()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCourseNameSuggestions by remember { mutableStateOf(false) }
    var showTeacherSuggestions by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    val lazyListState = rememberLazyListState()

    val editContentBackdrop = kyantRememberLayerBackdrop()
    val editBackgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(editBackgroundColor)
        drawContent()
    }

    val editBlurEnabled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0

    val sharedTransitionScope = LocalSharedTransitionScope.current

    Box(
        modifier = Modifier
            .fillMaxSize()
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
    ) {
        Scaffold(
            topBar = {
                val appThemeMode = LocalAppThemeMode.current
                BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = editBlurEnabled, contentBackdrop = editContentBackdrop) {
                    SmallTopAppBar(
                        title = "编辑课程",
                        scrollBehavior = MiuixScrollBehavior(),
                        color = Color.Transparent,
                        titleColor = MiuixTheme.colorScheme.onSurface,
                        defaultWindowInsetsPadding = false,
                        navigationIcon = {
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                                }
                            } else {
                                GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
                                }
                            }
                        },
                        actions = {
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                if (state.isEditing) {
                                    top.yukonga.miuix.kmp.basic.IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.error)
                                    }
                                }
                                top.yukonga.miuix.kmp.basic.IconButton(
                                    onClick = {
                                        if (!isSaving) {
                                            isSaving = true
                                            viewModel.saveCourse()
                                        }
                                    },
                                    enabled = !isSaving
                                ) {
                                    AnimatedContent(
                                        targetState = isSaving,
                                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                        label = "save_button"
                                    ) { saving ->
                                        if (saving) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MiuixTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(Icons.Default.Check, contentDescription = "保存", tint = MiuixTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            } else {
                                if (state.isEditing) {
                                    GlassSymbolIconButton(onClick = { showDeleteDialog = true }, style = GlassSymbolButtonStyle.NonTinted) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = SemanticColors.ErrorLight)
                                    }
                                }
                                GlassSymbolIconButton(
                                    onClick = {
                                        if (!isSaving) {
                                            isSaving = true
                                            viewModel.saveCourse()
                                        }
                                    },
                                    enabled = !isSaving,
                                    style = GlassSymbolButtonStyle.Tinted
                                ) {
                                    AnimatedContent(
                                        targetState = isSaving,
                                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                        label = "save_button"
                                    ) { saving ->
                                        if (saving) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(Icons.Default.Check, contentDescription = "保存", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(editContentBackdrop).layerBackdrop(miuixBackdrop)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + Spacing.md,
                    bottom = Spacing.iOS26.groupSpacing + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
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
                    CourseNameAndPersonSection(
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
                        onDismissSuggestions = { showCourseNameSuggestions = false },
                        personType = state.personType,
                        onPersonTypeChange = viewModel::setPersonType,
                        showPersonType = !singleModeEnabled
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
                        isCustomTime = state.isCustomTime,
                        customStartHour = state.customStartHour,
                        customStartMinute = state.customStartMinute,
                        customEndHour = state.customEndHour,
                        customEndMinute = state.customEndMinute,
                        onTimeModeChange = viewModel::setTimeMode,
                        onPeriodClick = {
                            if (state.isCustomTime) {
                                showCustomTimePicker = true
                            } else {
                                showPeriodPicker = true
                            }
                        },
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

    if (showCustomTimePicker) {
        CustomTimePickerBottomSheet(
            selectedDayOfWeek = state.dayOfWeek,
            startHour = state.customStartHour,
            startMinute = state.customStartMinute,
            endHour = state.customEndHour,
            endMinute = state.customEndMinute,
            onSelectionChange = { dayOfWeek, startHour, startMinute, endHour, endMinute ->
                viewModel.setDayOfWeek(dayOfWeek)
                viewModel.setCustomTime(startHour, startMinute, endHour, endMinute)
            },
            onDismiss = { showCustomTimePicker = false },
            sheetState = customTimePickerState
        )
    }

    if (showDeleteDialog) {
        val appThemeMode = LocalAppThemeMode.current
        if (appThemeMode == AppThemeMode.MIUIX) {
            WindowDialog(
                show = true,
                title = "删除课程",
                onDismissRequest = { showDeleteDialog = false }
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = "确定要删除这门课程吗？此操作不可撤销。",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    top.yukonga.miuix.kmp.basic.Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteCourse()
                            showDeleteDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("删除")
                    }
                }
            }
        } else {
            GlassAlert(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteCourse()
                    showDeleteDialog = false
                },
                title = "删除课程",
                text = "确定要删除这门课程吗？此操作不可撤销。",
                confirmText = "删除",
                dismissText = "取消"
            )
        }
    }
}
