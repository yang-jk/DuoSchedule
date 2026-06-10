package com.duoschedule.ui.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.data.model.TodoTag
import com.duoschedule.ui.settings.components.SettingsDefaults
import com.duoschedule.ui.settings.components.SettingsSection
import com.duoschedule.ui.theme.BlurredBar
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.GlassAlert
import com.duoschedule.ui.theme.GlassCard
import com.duoschedule.ui.theme.GlassSelectableChip
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import com.duoschedule.ui.theme.GlassTextField
import com.duoschedule.ui.theme.IOSColors
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.SegmentOption
import com.duoschedule.ui.theme.SegmentedControl
import com.duoschedule.ui.theme.Separator
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.getFillsVibrantTertiary
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.duoschedule.ui.theme.getLabelsVibrantTertiary
import com.duoschedule.ui.theme.getRoundedCorner
import com.kyant.capsule.ContinuousRoundedRectangle
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MicroTween: TweenSpec<Float> = tween(100, easing = FastOutSlowInEasing)

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

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCoursePicker by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var showRepeatRuleDialog by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    val lazyListState = rememberLazyListState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val blurEnabled = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0

    LaunchedEffect(todoId) {
        if (todoId != null && todoId > 0) {
            viewModel.loadTodo(todoId)
        }
    }

    LaunchedEffect(initialDate, initialPersonType) {
        if (todoId == null || todoId <= 0) {
            initialDate?.let { viewModel.setInitialDate(it) }
            initialPersonType?.let { viewModel.setInitialPersonType(it) }
            if (initialStartHour >= 0 && initialStartMinute >= 0) {
                viewModel.setInitialStartTime(initialStartHour, initialStartMinute)
            }
            if (initialEndHour >= 0 && initialEndMinute >= 0) {
                viewModel.setInitialEndTime(initialEndHour, initialEndMinute)
            }
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            val appThemeMode = LocalAppThemeMode.current
            BlurredBar(hazeState, backdrop = miuixBackdrop, enabled = blurEnabled) {
                SmallTopAppBar(
                    title = if (state.isEditing) "编辑待办" else "新建待办",
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
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = viewModel::saveTodo) {
                                Icon(Icons.Default.Check, contentDescription = "保存", tint = MiuixTheme.colorScheme.primary)
                            }
                        } else {
                            if (state.isEditing) {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFFF3B30))
                                }
                            }
                            IconButton(onClick = viewModel::saveTodo) {
                                Icon(Icons.Default.Check, contentDescription = "保存", tint = Color.White)
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + Spacing.md,
                    bottom = Spacing.iOS26.groupSpacing + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // 错误提示
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

                // 标题和人物选择
                item {
                    TitleAndPersonSection(
                        title = state.title,
                        onTitleChange = viewModel::setTitle,
                        personType = state.personType,
                        onPersonTypeChange = viewModel::setPersonType
                    )
                }

                // 描述输入
                item {
                    DescriptionSection(
                        description = state.description,
                        onDescriptionChange = viewModel::setDescription
                    )
                }

                // 日期选择
                item {
                    DateSection(
                        date = state.date,
                        onDateClick = { showDatePicker = true }
                    )
                }

                // 时间设置
                item {
                    TimeSection(
                        startHour = state.startHour,
                        startMinute = state.startMinute,
                        endHour = state.endHour,
                        endMinute = state.endMinute,
                        onStartTimeClick = { showStartTimePicker = true },
                        onEndTimeClick = { showEndTimePicker = true },
                        onClearStartTime = viewModel::clearStartTime,
                        onClearEndTime = viewModel::clearEndTime
                    )
                }

                // 优先级选择
                item {
                    PrioritySection(
                        priority = state.priority,
                        onPriorityChange = viewModel::setPriority
                    )
                }

                // 标签选择
                item {
                    TagsSection(
                        allTags = allTags,
                        selectedTagIds = state.selectedTagIds,
                        onTagToggle = viewModel::toggleTag,
                        onAddTag = { showAddTagDialog = true; newTagName = "" }
                    )
                }

                // 关联课程
                item {
                    LinkedCourseSection(
                        linkedCourseSyncId = state.linkedCourseSyncId,
                        courses = courses,
                        onClick = { showCoursePicker = true }
                    )
                }

                // 重复规则
                item {
                    RepeatRuleSection(
                        repeatRule = state.repeatRule,
                        onClick = { showRepeatRuleDialog = true }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.xxl))
                }
            }
        }

        // 日期选择器
        if (showDatePicker) {
            TodoDatePickerDialog(
                currentDate = state.date,
                onDateSelected = { epochDay ->
                    viewModel.setDate(epochDay)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // 开始时间选择器
        if (showStartTimePicker) {
            TodoTimePickerDialog(
                currentHour = if (state.startHour >= 0) state.startHour else 9,
                currentMinute = if (state.startMinute >= 0) state.startMinute else 0,
                title = "开始时间",
                onTimeSelected = { hour, minute ->
                    viewModel.setStartTime(hour, minute)
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }

        // 结束时间选择器
        if (showEndTimePicker) {
            TodoTimePickerDialog(
                currentHour = if (state.endHour >= 0) state.endHour else 10,
                currentMinute = if (state.endMinute >= 0) state.endMinute else 0,
                title = "结束时间",
                onTimeSelected = { hour, minute ->
                    viewModel.setEndTime(hour, minute)
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }

        // 删除确认对话框
        if (showDeleteDialog) {
            val appThemeMode = LocalAppThemeMode.current
            if (appThemeMode == AppThemeMode.MIUIX) {
                WindowDialog(
                    show = true,
                    title = "删除待办",
                    onDismissRequest = { showDeleteDialog = false }
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
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            top.yukonga.miuix.kmp.basic.Text("取消")
                        }
                        Button(
                            onClick = {
                                viewModel.deleteTodo()
                                showDeleteDialog = false
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
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirm = {
                        viewModel.deleteTodo()
                        showDeleteDialog = false
                    },
                    title = "删除待办",
                    text = "确定要删除这条待办吗？此操作不可撤销。",
                    confirmText = "删除",
                    dismissText = "取消"
                )
            }
        }

        // 课程选择对话框
        if (showCoursePicker) {
            CoursePickerDialog(
                courses = courses,
                currentLinkedSyncId = state.linkedCourseSyncId,
                onCourseSelected = { syncId ->
                    viewModel.setLinkedCourse(syncId)
                    showCoursePicker = false
                },
                onClear = {
                    viewModel.setLinkedCourse(null)
                    showCoursePicker = false
                },
                onDismiss = { showCoursePicker = false }
            )
        }

        // 添加标签对话框
        if (showAddTagDialog) {
            AddTagDialog(
                tagName = newTagName,
                onTagNameChange = { newTagName = it },
                onConfirm = {
                    if (newTagName.isNotBlank()) {
                        viewModel.toggleTag(newTagName)
                        showAddTagDialog = false
                    }
                },
                onDismiss = { showAddTagDialog = false }
            )
        }

        // 重复规则设置对话框
        if (showRepeatRuleDialog) {
            RepeatRuleDialog(
                currentRule = state.repeatRule,
                onConfirm = { rule ->
                    viewModel.setRepeatRule(rule)
                    showRepeatRuleDialog = false
                },
                onDismiss = { showRepeatRuleDialog = false }
            )
        }
    }
}

// ==================== 子组件 ====================

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
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
    val errorColor = if (darkTheme) Color(0xFFFF453A) else Color(0xFFFF3B30)
    val fillColor = getFillsVibrantTertiary()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
            .background(fillColor)
            .padding(Spacing.lg)
    ) {
        Text(
            text = message,
            color = errorColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TitleAndPersonSection(
    title: String,
    onTitleChange: (String) -> Unit,
    personType: PersonType,
    onPersonTypeChange: (PersonType) -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current

    SettingsSection(
        title = "待办标题",
        modifier = modifier
    ) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            TabRowWithContour(
                tabs = listOf("我的待办", "Ta的待办"),
                selectedTabIndex = if (personType == PersonType.PERSON_A) 0 else 1,
                onTabSelected = { onPersonTypeChange(if (it == 0) PersonType.PERSON_A else PersonType.PERSON_B) }
            )
        } else {
            SegmentedControl(
                options = listOf(
                    SegmentOption(PersonType.PERSON_A, "我的待办"),
                    SegmentOption(PersonType.PERSON_B, "Ta的待办")
                ),
                selectedOption = personType,
                onOptionSelected = onPersonTypeChange
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = "请输入待办标题",
                useLabelAsPlaceholder = true
            )
        } else {
            GlassTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "请输入待办标题",
                transparentBackground = true
            )
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current

    SettingsSection(
        title = "备注",
        modifier = modifier
    ) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = "添加备注（可选）",
                useLabelAsPlaceholder = true,
                maxLines = 4
            )
        } else {
            GlassTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "添加备注（可选）",
                transparentBackground = true,
                singleLine = false,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun DateSection(
    date: Long,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    val dateText = remember(date) {
        val localDate = LocalDate.ofEpochDay(date)
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE")
        formatter.format(localDate)
    }

    SettingsSection(
        title = "日期",
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = MicroTween,
            label = "date_scale"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDateClick
                )
                .padding(vertical = SettingsDefaults.ItemVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(SettingsDefaults.IconBackgroundSize)
                    .clip(iconShape)
                    .background(IOSColors.Orange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsDefaults.IconSize),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Text(
                text = "日期",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = labelsPrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = dateText,
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
}

@Composable
private fun TimeSection(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onClearStartTime: () -> Unit,
    onClearEndTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    SettingsSection(
        title = "时间",
        modifier = modifier
    ) {
        // 提示文字
        if (appThemeMode == AppThemeMode.MIUIX) {
            top.yukonga.miuix.kmp.basic.Text(
                text = "不设置时间则为纯待办",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
        } else {
            Text(
                text = "不设置时间则为纯待办",
                color = labelsTertiary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
        }

        // 开始时间行
        TimeRow(
            label = "开始时间",
            hour = startHour,
            minute = startMinute,
            iconShape = iconShape,
            iconColor = IOSColors.Blue,
            onClick = onStartTimeClick,
            onClear = onClearStartTime
        )

        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

        // 结束时间行
        TimeRow(
            label = "结束时间",
            hour = endHour,
            minute = endMinute,
            iconShape = iconShape,
            iconColor = IOSColors.Purple,
            onClick = onEndTimeClick,
            onClear = onClearEndTime
        )
    }
}

@Composable
private fun TimeRow(
    label: String,
    hour: Int,
    minute: Int,
    iconShape: Shape,
    iconColor: Color,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val hasTime = hour >= 0

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MicroTween,
        label = "time_scale"
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
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(SettingsDefaults.IconSize),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = labelsPrimary,
            modifier = Modifier.weight(1f)
        )

        if (hasTime) {
            Text(
                text = String.format("%02d:%02d", hour, minute),
                style = MaterialTheme.typography.bodyMedium,
                color = labelsSecondary
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            // 清除按钮
            val appThemeMode = LocalAppThemeMode.current
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "清除",
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "清除",
                        tint = labelsSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Text(
                text = "未设置",
                style = MaterialTheme.typography.bodyMedium,
                color = labelsSecondary
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
}

@Composable
private fun PrioritySection(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current

    val priorityOptions = listOf(
        Triple(Priority.HIGH, "高", Color(0xFFFF3B30)),
        Triple(Priority.MEDIUM, "中", Color(0xFFFF9500)),
        Triple(Priority.LOW, "低", Color(0xFF34C759))
    )

    SettingsSection(
        title = "优先级",
        modifier = modifier
    ) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                priorityOptions.forEach { (p, label, color) ->
                    val isSelected = priority == p
                    top.yukonga.miuix.kmp.basic.Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPriorityChange(p) },
                        shape = RoundedCornerShape(BorderRadius.lg),
                        color = if (isSelected) color.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.md),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(
                                text = label,
                                color = if (isSelected) color else MiuixTheme.colorScheme.onBackground,
                                style = MiuixTheme.textStyles.body1.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                priorityOptions.forEach { (p, label, color) ->
                    GlassSelectableChip(
                        selected = priority == p,
                        onClick = { onPriorityChange(p) },
                        label = label,
                        selectedColor = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsSection(
    allTags: List<TodoTag>,
    selectedTagIds: Set<String>,
    onTagToggle: (String) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    SettingsSection(
        title = "标签",
        modifier = modifier
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(end = Spacing.sm)
        ) {
            items(allTags, key = { it.id }) { tag ->
                val isSelected = selectedTagIds.contains(tag.id)
                val tagColor = Color(tag.color)

                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Surface(
                        shape = RoundedCornerShape(BorderRadius.pill),
                        color = if (isSelected) tagColor.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant,
                        onClick = { onTagToggle(tag.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tagColor)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(
                                text = tag.name,
                                color = if (isSelected) tagColor else MiuixTheme.colorScheme.onBackground,
                                style = MiuixTheme.textStyles.body2.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                } else {
                    GlassSelectableChip(
                        selected = isSelected,
                        onClick = { onTagToggle(tag.id) },
                        label = tag.name,
                        selectedColor = tagColor
                    )
                }
            }

            // 添加自定义标签按钮
            item {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Surface(
                        shape = RoundedCornerShape(BorderRadius.pill),
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        onClick = onAddTag
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MiuixTheme.colorScheme.onBackgroundVariant
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(
                                text = "自定义",
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                                style = MiuixTheme.textStyles.body2
                            )
                        }
                    }
                } else {
                    GlassSelectableChip(
                        selected = false,
                        onClick = onAddTag,
                        label = "+ 自定义",
                        selectedColor = BrandColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedCourseSection(
    linkedCourseSyncId: String?,
    courses: List<Course>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    val linkedCourseName = remember(linkedCourseSyncId, courses) {
        if (linkedCourseSyncId == null) null
        else courses.find { it.syncId == linkedCourseSyncId }?.name
    }

    SettingsSection(
        title = "关联课程",
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = MicroTween,
            label = "course_scale"
        )

        Row(
            modifier = Modifier
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
                    .background(IOSColors.Teal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsDefaults.IconSize),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Text(
                text = "关联课程",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = labelsPrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = linkedCourseName ?: "未关联",
                style = MaterialTheme.typography.bodyMedium,
                color = if (linkedCourseName != null) labelsPrimary else labelsSecondary,
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
}

@Composable
private fun RepeatRuleSection(
    repeatRule: RepeatRule?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    // 根据重复规则生成显示文本
    val repeatText = remember(repeatRule) {
        when {
            repeatRule == null -> "不重复"
            repeatRule.frequency == RepeatFrequency.DAILY -> {
                if (repeatRule.interval == 1) "每天" else "每${repeatRule.interval}天"
            }
            repeatRule.frequency == RepeatFrequency.WEEKLY -> {
                val dayNames = repeatRule.daysOfWeek.split(",")
                    .filter { it.isNotBlank() }
                    .mapNotNull { d ->
                        when (d.trim().toIntOrNull()) {
                            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
                            5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> null
                        }
                    }
                if (dayNames.isEmpty()) "每周" else "每${repeatRule.interval}周 ${dayNames.joinToString("、")}"
            }
            repeatRule.frequency == RepeatFrequency.CUSTOM -> "自定义"
            else -> "不重复"
        }
    }

    SettingsSection(
        title = "重复",
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = MicroTween,
            label = "repeat_scale"
        )

        Row(
            modifier = Modifier
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
                    .background(IOSColors.Indigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(SettingsDefaults.IconSize),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Text(
                text = "重复规则",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = labelsPrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = repeatText,
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
}

// ==================== 对话框组件 ====================

/** 重复频率选项 */
private enum class RepeatFrequencyOption(val label: String) {
    NONE("不重复"),
    DAILY("每天"),
    WEEKLY("每周"),
    CUSTOM("自定义")
}

/** 周几选项 */
private val DAY_OF_WEEK_OPTIONS = listOf(
    1 to "周一", 2 to "周二", 3 to "周三", 4 to "周四",
    5 to "周五", 6 to "周六", 7 to "周日"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatRuleDialog(
    currentRule: RepeatRule?,
    onConfirm: (RepeatRule?) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    // 当前选择的频率选项
    val initialFrequencyOption = when (currentRule?.frequency) {
        RepeatFrequency.DAILY -> RepeatFrequencyOption.DAILY
        RepeatFrequency.WEEKLY -> RepeatFrequencyOption.WEEKLY
        RepeatFrequency.CUSTOM -> RepeatFrequencyOption.CUSTOM
        else -> RepeatFrequencyOption.NONE
    }
    var selectedFrequency by remember { mutableStateOf(initialFrequencyOption) }

    // 每天间隔
    var dailyInterval by remember { mutableStateOf(currentRule?.takeIf { it.frequency == RepeatFrequency.DAILY }?.interval ?: 1) }

    // 每周间隔
    var weeklyInterval by remember { mutableStateOf(currentRule?.takeIf { it.frequency == RepeatFrequency.WEEKLY }?.interval ?: 1) }

    // 选中的周几
    val initialDaysOfWeek = currentRule?.takeIf { it.frequency == RepeatFrequency.WEEKLY }?.daysOfWeek
        ?.split(",")?.filter { it.isNotBlank() }?.mapNotNull { it.trim().toIntOrNull() }?.toSet() ?: emptySet()
    var selectedDaysOfWeek by remember { mutableStateOf(initialDaysOfWeek) }

    // 自定义日期列表
    val initialCustomDates = currentRule?.takeIf { it.frequency == RepeatFrequency.CUSTOM }?.customDates
        ?.split(",")?.filter { it.isNotBlank() }?.mapNotNull { it.trim().toLongOrNull() } ?: emptyList()
    var customDates by remember { mutableStateOf(initialCustomDates) }

    // 结束日期
    var hasEndDate by remember { mutableStateOf(currentRule?.endDate != null) }
    var ruleEndDate by remember { mutableStateOf(currentRule?.endDate) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    // 构建最终的 RepeatRule
    fun buildRule(): RepeatRule? {
        return when (selectedFrequency) {
            RepeatFrequencyOption.NONE -> null
            RepeatFrequencyOption.DAILY -> RepeatRule(
                id = currentRule?.id ?: java.util.UUID.randomUUID().toString(),
                frequency = RepeatFrequency.DAILY,
                interval = dailyInterval,
                endDate = if (hasEndDate) ruleEndDate else null
            )
            RepeatFrequencyOption.WEEKLY -> {
                if (selectedDaysOfWeek.isEmpty()) return null
                RepeatRule(
                    id = currentRule?.id ?: java.util.UUID.randomUUID().toString(),
                    frequency = RepeatFrequency.WEEKLY,
                    interval = weeklyInterval,
                    daysOfWeek = selectedDaysOfWeek.sorted().joinToString(","),
                    endDate = if (hasEndDate) ruleEndDate else null
                )
            }
            RepeatFrequencyOption.CUSTOM -> {
                if (customDates.isEmpty()) return null
                RepeatRule(
                    id = currentRule?.id ?: java.util.UUID.randomUUID().toString(),
                    frequency = RepeatFrequency.CUSTOM,
                    customDates = customDates.sorted().joinToString(","),
                    endDate = if (hasEndDate) ruleEndDate else null
                )
            }
        }
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "重复规则",
            onDismissRequest = onDismiss
        ) {
            RepeatRuleDialogContent(
                selectedFrequency = selectedFrequency,
                onFrequencyChange = { selectedFrequency = it },
                dailyInterval = dailyInterval,
                onDailyIntervalChange = { dailyInterval = it },
                weeklyInterval = weeklyInterval,
                onWeeklyIntervalChange = { weeklyInterval = it },
                selectedDaysOfWeek = selectedDaysOfWeek,
                onDaysOfWeekChange = { selectedDaysOfWeek = it },
                customDates = customDates,
                onAddCustomDate = { showCustomDatePicker = true },
                onRemoveCustomDate = { date -> customDates = customDates.filter { it != date } },
                hasEndDate = hasEndDate,
                onHasEndDateChange = { hasEndDate = it },
                ruleEndDate = ruleEndDate,
                onEndDateClick = { showEndDatePicker = true }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
                Button(
                    onClick = { onConfirm(buildRule()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("确定")
                }
            }
        }
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("重复规则") },
            text = {
                RepeatRuleDialogContent(
                    selectedFrequency = selectedFrequency,
                    onFrequencyChange = { selectedFrequency = it },
                    dailyInterval = dailyInterval,
                    onDailyIntervalChange = { dailyInterval = it },
                    weeklyInterval = weeklyInterval,
                    onWeeklyIntervalChange = { weeklyInterval = it },
                    selectedDaysOfWeek = selectedDaysOfWeek,
                    onDaysOfWeekChange = { selectedDaysOfWeek = it },
                    customDates = customDates,
                    onAddCustomDate = { showCustomDatePicker = true },
                    onRemoveCustomDate = { date -> customDates = customDates.filter { it != date } },
                    hasEndDate = hasEndDate,
                    onHasEndDateChange = { hasEndDate = it },
                    ruleEndDate = ruleEndDate,
                    onEndDateClick = { showEndDatePicker = true }
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(buildRule()) }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }

    // 自定义日期选择器
    if (showCustomDatePicker) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            // Miuix 分支：使用 NumberPicker 年月日选择器
            val today = LocalDate.now()
            var customYear by remember { mutableStateOf(today.year) }
            var customMonth by remember { mutableStateOf(today.monthValue) }
            var customDay by remember { mutableStateOf(today.dayOfMonth) }

            val maxDayInMonth = remember(customYear, customMonth) {
                LocalDate.of(customYear, customMonth, 1).lengthOfMonth()
            }

            LaunchedEffect(maxDayInMonth) {
                if (customDay > maxDayInMonth) {
                    customDay = maxDayInMonth
                }
            }

            WindowBottomSheet(
                show = true,
                title = "选择日期",
                cornerRadius = getRoundedCorner(),
                onDismissRequest = { showCustomDatePicker = false },
                allowDismiss = false
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(225.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(
                        value = customYear,
                        onValueChange = { customYear = it },
                        range = 2024..2035,
                        label = { "${it}年" },
                        modifier = Modifier.weight(1f)
                    )
                    NumberPicker(
                        value = customMonth,
                        onValueChange = { customMonth = it },
                        range = 1..12,
                        label = { "${it}月" },
                        wrapAround = true,
                        modifier = Modifier.weight(0.6f)
                    )
                    NumberPicker(
                        value = customDay,
                        onValueChange = { customDay = it },
                        range = 1..maxDayInMonth,
                        label = { "${it}日" },
                        wrapAround = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCustomDatePicker = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            val epochDay = LocalDate.of(customYear, customMonth, customDay).toEpochDay()
                            if (epochDay !in customDates) {
                                customDates = (customDates + epochDay).sorted()
                            }
                            showCustomDatePicker = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("添加")
                    }
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        } else {
            // iOS 分支：使用 Material3 DatePicker
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showCustomDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val epochDay = Instant.ofEpochMilli(selected)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toEpochDay()
                            if (epochDay !in customDates) {
                                customDates = (customDates + epochDay).sorted()
                            }
                        }
                        showCustomDatePicker = false
                    }) { Text("添加") }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    // 结束日期选择器
    if (showEndDatePicker) {
        if (appThemeMode == AppThemeMode.MIUIX) {
            // Miuix 分支：使用 NumberPicker 年月日选择器
            val initialEndDate = ruleEndDate?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
            var endYear by remember { mutableStateOf(initialEndDate.year) }
            var endMonth by remember { mutableStateOf(initialEndDate.monthValue) }
            var endDay by remember { mutableStateOf(initialEndDate.dayOfMonth) }

            val maxDayInMonth = remember(endYear, endMonth) {
                LocalDate.of(endYear, endMonth, 1).lengthOfMonth()
            }

            LaunchedEffect(maxDayInMonth) {
                if (endDay > maxDayInMonth) {
                    endDay = maxDayInMonth
                }
            }

            WindowBottomSheet(
                show = true,
                title = "选择结束日期",
                cornerRadius = getRoundedCorner(),
                onDismissRequest = { showEndDatePicker = false },
                allowDismiss = false
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(225.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(
                        value = endYear,
                        onValueChange = { endYear = it },
                        range = 2024..2035,
                        label = { "${it}年" },
                        modifier = Modifier.weight(1f)
                    )
                    NumberPicker(
                        value = endMonth,
                        onValueChange = { endMonth = it },
                        range = 1..12,
                        label = { "${it}月" },
                        wrapAround = true,
                        modifier = Modifier.weight(0.6f)
                    )
                    NumberPicker(
                        value = endDay,
                        onValueChange = { endDay = it },
                        range = 1..maxDayInMonth,
                        label = { "${it}日" },
                        wrapAround = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showEndDatePicker = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("取消")
                    }
                    Button(
                        onClick = {
                            ruleEndDate = LocalDate.of(endYear, endMonth, endDay).toEpochDay()
                            showEndDatePicker = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        top.yukonga.miuix.kmp.basic.Text("确定")
                    }
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        } else {
            // iOS 分支：使用 Material3 DatePicker
            val initialMillis = ruleEndDate?.let {
                LocalDate.ofEpochDay(it).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = endDatePickerState.selectedDateMillis
                        if (selected != null) {
                            ruleEndDate = Instant.ofEpochMilli(selected)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toEpochDay()
                        }
                        showEndDatePicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("取消") }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }
    }
}

/** 重复规则对话框的内容区域 */
@Composable
private fun RepeatRuleDialogContent(
    selectedFrequency: RepeatFrequencyOption,
    onFrequencyChange: (RepeatFrequencyOption) -> Unit,
    dailyInterval: Int,
    onDailyIntervalChange: (Int) -> Unit,
    weeklyInterval: Int,
    onWeeklyIntervalChange: (Int) -> Unit,
    selectedDaysOfWeek: Set<Int>,
    onDaysOfWeekChange: (Set<Int>) -> Unit,
    customDates: List<Long>,
    onAddCustomDate: () -> Unit,
    onRemoveCustomDate: (Long) -> Unit,
    hasEndDate: Boolean,
    onHasEndDateChange: (Boolean) -> Unit,
    ruleEndDate: Long?,
    onEndDateClick: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // 频率选择
        if (appThemeMode == AppThemeMode.MIUIX) {
            TabRowWithContour(
                tabs = RepeatFrequencyOption.entries.map { it.label },
                selectedTabIndex = RepeatFrequencyOption.entries.indexOf(selectedFrequency),
                onTabSelected = { onFrequencyChange(RepeatFrequencyOption.entries[it]) }
            )
        } else {
            SegmentedControl(
                options = RepeatFrequencyOption.entries.map {
                    SegmentOption(it, it.label)
                },
                selectedOption = selectedFrequency,
                onOptionSelected = onFrequencyChange
            )
        }

        // 每天间隔设置
        if (selectedFrequency == RepeatFrequencyOption.DAILY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "每隔",
                        color = MiuixTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "每隔",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsPrimary
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

                // 间隔数字选择
                if (appThemeMode == AppThemeMode.MIUIX) {
                    TextField(
                        value = dailyInterval.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            if (n != null && n in 1..365) onDailyIntervalChange(n)
                        },
                        modifier = Modifier.width(80.dp)
                    )
                } else {
                    GlassTextField(
                        value = dailyInterval.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            if (n != null && n in 1..365) onDailyIntervalChange(n)
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "天",
                        color = MiuixTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsPrimary
                    )
                }
            }
        }

        // 每周设置
        if (selectedFrequency == RepeatFrequencyOption.WEEKLY) {
            // 间隔
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "每隔",
                        color = MiuixTheme.colorScheme.onSurface
                    )
                } else {
                    Text("每隔", style = MaterialTheme.typography.bodyMedium, color = labelsPrimary)
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

                if (appThemeMode == AppThemeMode.MIUIX) {
                    TextField(
                        value = weeklyInterval.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            if (n != null && n in 1..52) onWeeklyIntervalChange(n)
                        },
                        modifier = Modifier.width(80.dp)
                    )
                } else {
                    GlassTextField(
                        value = weeklyInterval.toString(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            if (n != null && n in 1..52) onWeeklyIntervalChange(n)
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "周",
                        color = MiuixTheme.colorScheme.onSurface
                    )
                } else {
                    Text("周", style = MaterialTheme.typography.bodyMedium, color = labelsPrimary)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // 周几选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                DAY_OF_WEEK_OPTIONS.forEach { (dayNum, dayName) ->
                    val isSelected = dayNum in selectedDaysOfWeek
                    if (appThemeMode == AppThemeMode.MIUIX) {
                        top.yukonga.miuix.kmp.basic.Surface(
                            shape = RoundedCornerShape(BorderRadius.pill),
                            color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant,
                            onClick = {
                                onDaysOfWeekChange(
                                    if (isSelected) selectedDaysOfWeek - dayNum else selectedDaysOfWeek + dayNum
                                )
                            }
                        ) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = dayName,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                                color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                                style = MiuixTheme.textStyles.body2.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    } else {
                        GlassSelectableChip(
                            selected = isSelected,
                            onClick = {
                                onDaysOfWeekChange(
                                    if (isSelected) selectedDaysOfWeek - dayNum else selectedDaysOfWeek + dayNum
                                )
                            },
                            label = dayName,
                            selectedColor = BrandColors.Primary
                        )
                    }
                }
            }
        }

        // 自定义日期设置
        if (selectedFrequency == RepeatFrequencyOption.CUSTOM) {
            // 已选日期列表
            if (customDates.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    customDates.forEach { epochDay ->
                        val dateText = remember(epochDay) {
                            val localDate = LocalDate.ofEpochDay(epochDay)
                            val formatter = DateTimeFormatter.ofPattern("yyyy/M/d")
                            formatter.format(localDate)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (appThemeMode == AppThemeMode.MIUIX) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = dateText,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                top.yukonga.miuix.kmp.basic.IconButton(onClick = { onRemoveCustomDate(epochDay) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = labelsPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onRemoveCustomDate(epochDay) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        tint = labelsSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 添加日期按钮
            if (appThemeMode == AppThemeMode.MIUIX) {
                top.yukonga.miuix.kmp.basic.Surface(
                    shape = RoundedCornerShape(BorderRadius.lg),
                    color = MiuixTheme.colorScheme.surfaceVariant,
                    onClick = onAddCustomDate
                ) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "+ 添加日期",
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                        color = MiuixTheme.colorScheme.primary,
                        style = MiuixTheme.textStyles.body2
                    )
                }
            } else {
                GlassSelectableChip(
                    selected = false,
                    onClick = onAddCustomDate,
                    label = "+ 添加日期",
                    selectedColor = BrandColors.Primary
                )
            }
        }

        // 结束日期设置
        if (selectedFrequency != RepeatFrequencyOption.NONE) {
            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "结束日期",
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Text(
                        text = "结束日期",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 永不 / 选择日期 切换
                if (appThemeMode == AppThemeMode.MIUIX) {
                    TabRowWithContour(
                        tabs = listOf("永不", "选择日期"),
                        selectedTabIndex = if (hasEndDate) 1 else 0,
                        onTabSelected = { onHasEndDateChange(it == 1) }
                    )
                } else {
                    SegmentedControl(
                        options = listOf(
                            SegmentOption(false, "永不"),
                            SegmentOption(true, "选择日期")
                        ),
                        selectedOption = hasEndDate,
                        onOptionSelected = onHasEndDateChange
                    )
                }
            }

            if (hasEndDate) {
                val endDateText = ruleEndDate?.let {
                    remember(it) {
                        val localDate = LocalDate.ofEpochDay(it)
                        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
                        formatter.format(localDate)
                    }
                } ?: "请选择"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEndDateClick)
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appThemeMode == AppThemeMode.MIUIX) {
                        top.yukonga.miuix.kmp.basic.Text(
                            text = endDateText,
                            color = MiuixTheme.colorScheme.primary,
                            style = MiuixTheme.textStyles.body2
                        )
                    } else {
                        Text(
                            text = endDateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandColors.Primary
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = labelsSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoDatePickerDialog(
    currentDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        val initialDate = LocalDate.ofEpochDay(currentDate)
        var currentYear by remember { mutableStateOf(initialDate.year) }
        var currentMonth by remember { mutableStateOf(initialDate.monthValue) }
        var currentDay by remember { mutableStateOf(initialDate.dayOfMonth) }

        // 根据年月计算当月最大天数
        val maxDayInMonth = remember(currentYear, currentMonth) {
            LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth()
        }

        // 确保日期不超过当月最大天数
        LaunchedEffect(maxDayInMonth) {
            if (currentDay > maxDayInMonth) {
                currentDay = maxDayInMonth
            }
        }

        WindowBottomSheet(
            show = true,
            title = "选择日期",
            cornerRadius = getRoundedCorner(),
            onDismissRequest = onDismiss,
            allowDismiss = false
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(225.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(
                    value = currentYear,
                    onValueChange = { currentYear = it },
                    range = 2024..2035,
                    label = { "${it}年" },
                    modifier = Modifier.weight(1f)
                )
                NumberPicker(
                    value = currentMonth,
                    onValueChange = { currentMonth = it },
                    range = 1..12,
                    label = { "${it}月" },
                    wrapAround = true,
                    modifier = Modifier.weight(0.6f)
                )
                NumberPicker(
                    value = currentDay,
                    onValueChange = { currentDay = it },
                    range = 1..maxDayInMonth,
                    label = { "${it}日" },
                    wrapAround = true,
                    modifier = Modifier.weight(0.6f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
                Button(
                    onClick = {
                        val epochDay = LocalDate.of(currentYear, currentMonth, currentDay).toEpochDay()
                        onDateSelected(epochDay)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("确定")
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    } else {
        val initialDate = LocalDate.ofEpochDay(currentDate)
        val initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    if (selected != null) {
                        val epochDay = Instant.ofEpochMilli(selected)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toEpochDay()
                        onDateSelected(epochDay)
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoTimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    title: String,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        var hour by remember { mutableStateOf(currentHour) }
        var minute by remember { mutableStateOf(currentMinute) }

        WindowBottomSheet(
            show = true,
            title = title,
            cornerRadius = getRoundedCorner(),
            onDismissRequest = onDismiss,
            allowDismiss = false
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(225.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(
                    value = hour,
                    onValueChange = { hour = it },
                    range = 0..23,
                    label = { it.toString().padStart(2, '0') },
                    wrapAround = true,
                    modifier = Modifier.weight(1f)
                )
                top.yukonga.miuix.kmp.basic.Text(
                    text = ":",
                    color = MiuixTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                NumberPicker(
                    value = minute,
                    onValueChange = { minute = it },
                    range = 0..59,
                    label = { it.toString().padStart(2, '0') },
                    wrapAround = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
                Button(
                    onClick = { onTimeSelected(hour, minute) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("确定")
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    } else {
        val timePickerState = rememberTimePickerState(
            initialHour = currentHour,
            initialMinute = currentMinute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CoursePickerDialog(
    courses: List<Course>,
    currentLinkedSyncId: String?,
    onCourseSelected: (String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    // 按人物分组去重
    val uniqueCourses = remember(courses) {
        courses.distinctBy { it.name }.sortedBy { it.name }
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "关联课程",
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(BorderRadius.lg))
                    .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = Spacing.sm)
            ) {
                // 未关联选项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClear() }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "未关联",
                        color = if (currentLinkedSyncId == null) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (currentLinkedSyncId == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                uniqueCourses.forEach { course ->
                    val isSelected = course.syncId == currentLinkedSyncId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCourseSelected(course.syncId) }
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        top.yukonga.miuix.kmp.basic.Text(
                            text = course.name,
                            color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
            }
        }
    } else {
        val darkTheme = LocalDarkTheme.current
        val optionBackground = if (darkTheme) Color(0x29EBEBF5) else Color(0x29787880)

        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("关联课程") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                        .background(optionBackground)
                        .padding(vertical = Spacing.sm)
                ) {
                    // 未关联选项
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClear() }
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "未关联",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (currentLinkedSyncId == null) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (currentLinkedSyncId == null) BrandColors.Primary else labelsPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentLinkedSyncId == null) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = BrandColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    uniqueCourses.forEach { course ->
                        val isSelected = course.syncId == currentLinkedSyncId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCourseSelected(course.syncId) }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) BrandColors.Primary else labelsPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BrandColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("完成")
                }
            }
        )
    }
}

@Composable
private fun AddTagDialog(
    tagName: String,
    onTagNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "添加标签",
            onDismissRequest = onDismiss
        ) {
            TextField(
                value = tagName,
                onValueChange = onTagNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = "标签名称",
                useLabelAsPlaceholder = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("添加")
                }
            }
        }
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("添加标签") },
            text = {
                GlassTextField(
                    value = tagName,
                    onValueChange = onTagNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "输入标签名称",
                    transparentBackground = true
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}
