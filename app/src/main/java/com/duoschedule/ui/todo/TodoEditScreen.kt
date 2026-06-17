package com.duoschedule.ui.todo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.PersonType
import com.duoschedule.AddButtonReveal
import com.duoschedule.ui.theme.BlurredBar
import com.duoschedule.ui.theme.GlassAlert
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.LocalAppSnackbarHostState
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LocalSharedTransitionScope
import com.duoschedule.ui.theme.Spacing
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    todoId: Long?,
    initialDate: Long? = null,
    initialPersonType: PersonType? = null,
    initialStartHour: Int = -1,
    initialStartMinute: Int = -1,
    initialEndHour: Int = -1,
    initialEndMinute: Int = -1,
    onNavigateBack: () -> Unit,
    viewModel: TodoEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val courses by viewModel.courses.collectAsState()

    val appSnackbarHostState = LocalAppSnackbarHostState.current
    val hapticFeedback = LocalHapticFeedback.current

    var currentDialog by remember { mutableStateOf<TodoEditDialog?>(null) }

    val hazeState = rememberHazeState()
    val lazyListState = rememberLazyListState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(todoId, initialDate, initialPersonType) {
        viewModel.initialize(
            todoId = todoId,
            initialDate = initialDate,
            initialPersonType = initialPersonType,
            initialStartHour = initialStartHour,
            initialStartMinute = initialStartMinute,
            initialEndHour = initialEndHour,
            initialEndMinute = initialEndMinute
        )
    }

    // 一镜到底返回：若从添加按钮进入则反向收缩回按钮位置，否则直接返回
    val enteredViaReveal = remember { AddButtonReveal.shouldReveal }
    val handleBack: () -> Unit = {
        if (enteredViaReveal && AddButtonReveal.sourceBounds != androidx.compose.ui.geometry.Rect.Zero) {
            AddButtonReveal.startConceal(AddButtonReveal.sourceBounds) {
                onNavigateBack()
            }
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        handleBack()
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            appSnackbarHostState?.showAppSnackbar("待办已保存")
            handleBack()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            appSnackbarHostState?.showAppSnackbar("待办已删除")
            handleBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            // ViewModel now handles isSaving reset
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TodoEditTopBar(
                    isEditing = state.isEditing,
                    isSaving = state.isSaving,
                    onNavigateBack = handleBack,
                    onDeleteClick = { currentDialog = TodoEditDialog.DeleteConfirm },
                    onSaveClick = { if (!state.isSaving) viewModel.saveTodo() },
                    hazeState = hazeState,
                    contentBackdrop = contentBackdrop,
                    miuixBackdrop = miuixBackdrop
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
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
                        BasicInfoSection(
                            title = state.title,
                            onTitleChange = viewModel::setTitle,
                            personType = state.personType,
                            onPersonTypeChange = viewModel::setPersonType,
                            description = state.description,
                            onDescriptionChange = viewModel::setDescription
                        )
                    }

                    item {
                        DateTimeSection(
                            date = state.date,
                            onDateClick = { currentDialog = TodoEditDialog.DatePicker },
                            startHour = state.startHour,
                            startMinute = state.startMinute,
                            endHour = state.endHour,
                            endMinute = state.endMinute,
                            onStartTimeClick = { currentDialog = TodoEditDialog.StartTimePicker },
                            onEndTimeClick = { currentDialog = TodoEditDialog.EndTimePicker },
                            onClearStartTime = viewModel::clearStartTime,
                            onClearEndTime = viewModel::clearEndTime
                        )
                    }

                    item {
                        CategorySection(
                            priority = state.priority,
                            onPriorityChange = viewModel::setPriority,
                            allTags = allTags,
                            selectedTagIds = state.selectedTagIds,
                            onTagToggle = viewModel::toggleTag,
                            onAddTag = { currentDialog = TodoEditDialog.AddTag }
                        )
                    }

                    item {
                        AdvancedSection(
                            linkedCourseSyncId = state.linkedCourseSyncId,
                            courses = courses,
                            onCourseClick = { currentDialog = TodoEditDialog.CoursePicker },
                            repeatRule = state.repeatRule,
                            onRepeatRuleClick = { currentDialog = TodoEditDialog.RepeatRule }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }

            when (val dialog = currentDialog) {
                is TodoEditDialog.DatePicker -> TodoDatePickerDialog(
                    currentDate = state.date,
                    onDateSelected = { epochDay ->
                        viewModel.setDate(epochDay)
                        currentDialog = null
                    },
                    onDismiss = { currentDialog = null }
                )

                is TodoEditDialog.StartTimePicker -> TodoTimePickerDialog(
                    currentHour = if (state.startHour >= 0) state.startHour else 9,
                    currentMinute = if (state.startMinute >= 0) state.startMinute else 0,
                    title = "开始时间",
                    onTimeSelected = { hour, minute ->
                        viewModel.setStartTime(hour, minute)
                        currentDialog = null
                    },
                    onDismiss = { currentDialog = null }
                )

                is TodoEditDialog.EndTimePicker -> TodoTimePickerDialog(
                    currentHour = if (state.endHour >= 0) state.endHour else 10,
                    currentMinute = if (state.endMinute >= 0) state.endMinute else 0,
                    title = "结束时间",
                    onTimeSelected = { hour, minute ->
                        viewModel.setEndTime(hour, minute)
                        currentDialog = null
                    },
                    onDismiss = { currentDialog = null }
                )

                is TodoEditDialog.DeleteConfirm -> {
                    val appThemeMode = LocalAppThemeMode.current
                    if (appThemeMode == AppThemeMode.MIUIX) {
                        WindowDialog(
                            show = true,
                            title = "删除待办",
                            onDismissRequest = { currentDialog = null }
                        ) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = "确定要删除这条待办吗？此操作不可撤销。",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(bottom = Spacing.md)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                Button(
                                    onClick = { currentDialog = null },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    top.yukonga.miuix.kmp.basic.Text("取消")
                                }
                                Button(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteTodo()
                                        currentDialog = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors()
                                ) {
                                    top.yukonga.miuix.kmp.basic.Text("删除")
                                }
                            }
                        }
                    } else {
                        GlassAlert(
                            onDismissRequest = { currentDialog = null },
                            onConfirm = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteTodo()
                                currentDialog = null
                            },
                            title = "删除待办",
                            text = "确定要删除这条待办吗？此操作不可撤销。",
                            confirmText = "删除",
                            dismissText = "取消"
                        )
                    }
                }

                is TodoEditDialog.CoursePicker -> CoursePickerDialog(
                    courses = courses,
                    currentLinkedSyncId = state.linkedCourseSyncId,
                    onCourseSelected = { syncId ->
                        viewModel.setLinkedCourse(syncId)
                        currentDialog = null
                    },
                    onClear = {
                        viewModel.setLinkedCourse(null)
                        currentDialog = null
                    },
                    onDismiss = { currentDialog = null }
                )

                is TodoEditDialog.AddTag -> AddTagDialog(
                    onConfirm = { name ->
                        if (name.isNotBlank()) {
                            viewModel.toggleTag(name)
                            currentDialog = null
                        }
                    },
                    onDismiss = { currentDialog = null }
                )

                is TodoEditDialog.RepeatRule -> RepeatRuleDialog(
                    currentRule = state.repeatRule,
                    onConfirm = { rule ->
                        viewModel.setRepeatRule(rule)
                        currentDialog = null
                    },
                    onDismiss = { currentDialog = null }
                )

                null -> {}
            }
        }
    }
}


// ==================== TopBar ====================

@Composable
private fun TodoEditTopBar(
    isEditing: Boolean,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    hazeState: dev.chrisbanes.haze.HazeState,
    contentBackdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    miuixBackdrop: top.yukonga.miuix.kmp.blur.LayerBackdrop
) {
    val appThemeMode = LocalAppThemeMode.current
    BlurredBar(hazeState, backdrop = miuixBackdrop, contentBackdrop = contentBackdrop) {
        SmallTopAppBar(
            title = if (isEditing) "编辑待办" else "新建待办",
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
                    if (isEditing) {
                        top.yukonga.miuix.kmp.basic.IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.error)
                        }
                    }
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onSaveClick, enabled = !isSaving) {
                        AnimatedContent(
                            targetState = isSaving,
                            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                            label = "save_button_miuix"
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
                    if (isEditing) {
                        GlassSymbolIconButton(onClick = onDeleteClick, style = GlassSymbolButtonStyle.NonTinted) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFFF3B30))
                        }
                    }
                    GlassSymbolIconButton(
                        onClick = onSaveClick,
                        enabled = isSaving.not(),
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
}
