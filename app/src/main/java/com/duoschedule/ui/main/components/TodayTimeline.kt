package com.duoschedule.ui.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.ui.theme.*
import com.duoschedule.ui.theme.SegmentOption
import com.duoschedule.ui.theme.SegmentedControl
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop

import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

/** 时间标签列宽度 */
private val TimeLabelWidth = 48.dp

/** 卡片最小高度 */
private val CardMinHeight = 72.dp

/** 卡片间距 */
private val CardSpacing = Spacing.sm

/** 优先级对应颜色 */
private val PriorityColorHigh = Color(0xFFFF3B30)
private val PriorityColorMedium = Color(0xFFFF9500)
private val PriorityColorLow = Color(0xFF34C759)

/** 获取优先级颜色 */
private fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> PriorityColorHigh
        Priority.MEDIUM -> PriorityColorMedium
        Priority.LOW -> PriorityColorLow
    }
}

/**
 * 显示模式选择器：Miuix 使用 TabRow，iOS 使用 SegmentedControl
 */
@Composable
private fun DisplayModeSelector(
    displayMode: TodayCourseDisplayMode,
    onDisplayModeChange: (TodayCourseDisplayMode) -> Unit,
    personAName: String = "我",
    personBName: String = "Ta",
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val nameA = personAName.ifEmpty { "我" }
    val nameB = personBName.ifEmpty { "Ta" }

    if (appThemeMode == AppThemeMode.MIUIX) {
        // Miuix 风格：TabRow
        val tabs = listOf(nameA, nameB, "全部")
        val selectedIndex = when (displayMode) {
            TodayCourseDisplayMode.SELF_ONLY -> 0
            TodayCourseDisplayMode.TA_ONLY -> 1
            TodayCourseDisplayMode.BOTH -> 2
        }
        TabRowWithContour(
            tabs = tabs,
            selectedTabIndex = selectedIndex,
            onTabSelected = { index ->
                val mode = when (index) {
                    0 -> TodayCourseDisplayMode.SELF_ONLY
                    1 -> TodayCourseDisplayMode.TA_ONLY
                    else -> TodayCourseDisplayMode.BOTH
                }
                onDisplayModeChange(mode)
            },
            modifier = modifier
        )
    } else {
        // iOS 风格：SegmentedControl
        val options = listOf(
            SegmentOption(TodayCourseDisplayMode.SELF_ONLY, nameA),
            SegmentOption(TodayCourseDisplayMode.TA_ONLY, nameB),
            SegmentOption(TodayCourseDisplayMode.BOTH, "全部")
        )
        SegmentedControl(
            options = options,
            selectedOption = displayMode,
            onOptionSelected = onDisplayModeChange,
            modifier = modifier
        )
    }
}

/**
 * 今日合并时间线组件，将课程和待办按时间统一展示。
 * 左侧为时间标签，右侧为对应卡片内容。
 */
@Composable
fun TodayTimeline(
    items: List<TodayTimelineItem>,
    onCourseClick: (Course, PersonType) -> Unit,
    onTodoClick: (Long) -> Unit,
    onToggleTodoStatus: (Long) -> Unit,
    modifier: Modifier = Modifier,
    singleModeEnabled: Boolean = false,
    currentHour: Int = 0,
    currentMinute: Int = 0,
    personAName: String = "我",
    personBName: String = "Ta",
    displayMode: TodayCourseDisplayMode = TodayCourseDisplayMode.BOTH,
    onDisplayModeChange: (TodayCourseDisplayMode) -> Unit = {},
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val appThemeMode = LocalAppThemeMode.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
    ) {
        // 区域标题：今日安排 + 数量
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xs),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = "今日安排",
                    style = MaterialTheme.typography.titleSmall,
                    color = labelsPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${items.size} 项",
                    style = MaterialTheme.typography.labelMedium,
                    color = labelsTertiary
                )
            }
        }

        // 显示模式切换控件（非单人模式下显示）
        if (!singleModeEnabled) {
            DisplayModeSelector(
                displayMode = displayMode,
                onDisplayModeChange = onDisplayModeChange,
                personAName = personAName,
                personBName = personBName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
            )
        }

        // 内容区域
        if (items.isEmpty()) {
            EmptyTimelineCard(backdrop = backdrop)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(CardSpacing)
            ) {
                items.forEach { item ->
                    TimelineRow(
                        item = item,
                        onCourseClick = onCourseClick,
                        onTodoClick = onTodoClick,
                        onToggleTodoStatus = onToggleTodoStatus,
                        singleModeEnabled = singleModeEnabled,
                        currentHour = currentHour,
                        currentMinute = currentMinute,
                        personAName = personAName,
                        personBName = personBName,
                        backdrop = backdrop
                    )
                }
            }
        }
    }
}

/**
 * 时间线单行：左侧时间标签 + 右侧卡片
 */
@Composable
private fun TimelineRow(
    item: TodayTimelineItem,
    onCourseClick: (Course, PersonType) -> Unit,
    onTodoClick: (Long) -> Unit,
    onToggleTodoStatus: (Long) -> Unit,
    singleModeEnabled: Boolean,
    currentHour: Int,
    currentMinute: Int,
    personAName: String,
    personBName: String,
    backdrop: Backdrop
) {
    val labelsTertiary = getLabelsVibrantTertiary()

    Row(
        verticalAlignment = Alignment.Top
    ) {
        // 左侧时间标签
        val timeText = when (item) {
            is TodayTimelineItem.CourseItem -> {
                String.format(Locale.ROOT, "%02d:%02d", item.course.startHour, item.course.startMinute)
            }
            is TodayTimelineItem.TimedTodoItem -> item.timeLabel
            is TodayTimelineItem.UntimedTodoItem -> ""
        }

        Box(
            modifier = Modifier
                .width(TimeLabelWidth)
                .padding(top = Spacing.sm),
            contentAlignment = Alignment.TopCenter
        ) {
            if (timeText.isNotEmpty()) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = labelsTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.xs))

        // 右侧卡片内容
        when (item) {
            is TodayTimelineItem.CourseItem -> {
                TimelineCourseCard(
                    course = item.course,
                    personType = item.personType,
                    onClick = onCourseClick,
                    currentHour = currentHour,
                    currentMinute = currentMinute,
                    personAName = personAName,
                    personBName = personBName,
                    singleModeEnabled = singleModeEnabled,
                    backdrop = backdrop
                )
            }
            is TodayTimelineItem.TimedTodoItem -> {
                TimelineTodoCard(
                    todo = item.todo,
                    onToggleStatus = onToggleTodoStatus,
                    onTodoClick = onTodoClick,
                    singleModeEnabled = singleModeEnabled,
                    backdrop = backdrop
                )
            }
            is TodayTimelineItem.UntimedTodoItem -> {
                TimelineTodoCard(
                    todo = item.todo,
                    onToggleStatus = onToggleTodoStatus,
                    onTodoClick = onTodoClick,
                    singleModeEnabled = singleModeEnabled,
                    backdrop = backdrop
                )
            }
        }
    }
}

/**
 * 时间线中的课程卡片，复用 TodayScheduleTimeline 的视觉风格
 */
@Composable
private fun TimelineCourseCard(
    course: Course,
    personType: PersonType,
    onClick: (Course, PersonType) -> Unit,
    currentHour: Int,
    currentMinute: Int,
    personAName: String,
    personBName: String,
    singleModeEnabled: Boolean,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val density = LocalDensity.current
    val appThemeMode = LocalAppThemeMode.current

    val personColor = if (personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()
    val personName = if (personType == PersonType.PERSON_A) personAName.ifEmpty { "我" } else personBName.ifEmpty { "Ta" }

    val currentMinutes = currentHour * 60 + currentMinute
    val courseStartMinutes = course.startHour * 60 + course.startMinute
    val courseEndMinutes = course.endHour * 60 + course.endMinute
    val hasEnded = currentMinutes > courseEndMinutes
    val isOngoing = currentMinutes in courseStartMinutes until courseEndMinutes

    val textPrimaryColor = if (hasEnded) labelsTertiary else labelsPrimary
    val textSecondaryColor = if (hasEnded) labelsTertiary else labelsSecondary

    if (appThemeMode == AppThemeMode.MIUIX) {
        val miuixTextPrimaryColor = if (hasEnded) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onSurface
        val miuixTextSecondaryColor = if (hasEnded) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Surface(
            color = MiuixTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(BorderRadius.lg),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = CardMinHeight)
                .clickable(onClick = { onClick(course, personType) })
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                ) {
                    // 人物标签（单人模式下隐藏）
                    if (!singleModeEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(personColor, CircleShape)
                            )
                            top.yukonga.miuix.kmp.basic.Text(
                                text = personName,
                                color = miuixTextSecondaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        top.yukonga.miuix.kmp.basic.Text(
                            text = course.name,
                            color = miuixTextPrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        top.yukonga.miuix.kmp.basic.Text(
                            text = course.getTimeString(),
                            color = miuixTextSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val locationText = course.location
                            val teacherText = course.teacher

                            if (locationText.isNotBlank() && teacherText.isNotBlank()) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = "$locationText · $teacherText",
                                    color = miuixTextSecondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (locationText.isNotBlank()) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = locationText,
                                    color = miuixTextSecondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (teacherText.isNotBlank()) {
                                top.yukonga.miuix.kmp.basic.Text(
                                    text = teacherText,
                                    color = miuixTextSecondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 进行中脉冲指示点
                if (isOngoing) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(6.dp)
                            .scale(pulseScale)
                            .background(personColor, CircleShape)
                    )
                }
            }
        }
        return
    }

    // iOS / Liquid Glass 风格
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .scale(pressScale)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                    // 进行中课程叠加人物色调
                    if (isOngoing) {
                        drawRect(personColor.copy(alpha = 0.04f))
                    }
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(course, personType) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            // 人物标签（单人模式下隐藏）
            if (!singleModeEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(personColor, CircleShape)
                    )
                    Text(
                        text = personName,
                        style = MaterialTheme.typography.labelMedium,
                        color = textSecondaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textPrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = course.getTimeString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = textSecondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val locationText = course.location
                    val teacherText = course.teacher

                    if (locationText.isNotBlank() && teacherText.isNotBlank()) {
                        Text(
                            text = "$locationText · $teacherText",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (locationText.isNotBlank()) {
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (teacherText.isNotBlank()) {
                        Text(
                            text = teacherText,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 进行中脉冲指示点
        if (isOngoing) {
            val infiniteTransition = rememberInfiniteTransition()
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(6.dp)
                    .scale(pulseScale)
                    .background(personColor, CircleShape)
            )
        }
    }
}

/**
 * 时间线中的待办卡片，复用 TodayTodoSection 的视觉风格（虚线边框 + 复选框）
 */
@Composable
private fun TimelineTodoCard(
    todo: Todo,
    onToggleStatus: (Long) -> Unit,
    onTodoClick: (Long) -> Unit,
    singleModeEnabled: Boolean,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val density = LocalDensity.current
    val appThemeMode = LocalAppThemeMode.current

    val isCompleted = todo.status == TodoStatus.COMPLETED
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1f,
        animationSpec = tween(300),
        label = "todo_content_alpha"
    )
    val strikethroughProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(300),
        label = "todo_strikethrough"
    )

    val personColor = if (todo.personType == PersonType.PERSON_A) getPersonAColor() else getPersonBColor()
    val personLabel = if (todo.personType == PersonType.PERSON_A) "A" else "B"

    // 人物色调叠加
    val personTint = if (todo.personType == PersonType.PERSON_A) {
        if (darkTheme) Color(0xFF4789FE).copy(alpha = 0.06f) else Color(0xFF4789FE).copy(alpha = 0.04f)
    } else {
        if (darkTheme) Color(0xFFFFB74D).copy(alpha = 0.06f) else Color(0xFFFFB74D).copy(alpha = 0.04f)
    }

    val priorityColor = getPriorityColor(todo.priority)

    // 虚线边框参数
    val dashWidth = 3.dp
    val dashGap = 3.dp

    if (appThemeMode == AppThemeMode.MIUIX) {
        val miuixTextPrimary = if (isCompleted) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onSurface
        val miuixTextSecondary = if (isCompleted) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Surface(
            color = MiuixTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(BorderRadius.lg),
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onTodoClick(todo.id) }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 复选框
                TimelineTodoCheckbox(
                    isCompleted = isCompleted,
                    personColor = personColor,
                    onClick = { onToggleStatus(todo.id) },
                    modifier = Modifier.alpha(contentAlpha)
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                // 内容区域
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 优先级指示点
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(priorityColor, CircleShape)
                        )

                        // 标题
                        top.yukonga.miuix.kmp.basic.Text(
                            text = todo.title,
                            color = miuixTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer { alpha = contentAlpha }
                                .drawWithContent {
                                    drawContent()
                                    if (strikethroughProgress > 0f) {
                                        drawLine(
                                            color = miuixTextPrimary,
                                            start = Offset(0f, size.height / 2),
                                            end = Offset(size.width * strikethroughProgress, size.height / 2),
                                            strokeWidth = 1.5.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                        )

                        // 时间文本
                        val timeText = formatTimelineTodoTime(todo)
                        if (timeText.isNotEmpty()) {
                            top.yukonga.miuix.kmp.basic.Text(
                                text = timeText,
                                color = miuixTextSecondary,
                                maxLines = 1,
                                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 人物标签（单人模式下隐藏）
                        if (!singleModeEnabled) {
                            TimelinePersonLabel(
                                label = personLabel,
                                color = personColor,
                                modifier = Modifier.alpha(contentAlpha)
                            )
                        }

                        // 标签
                        if (todo.tags.isNotEmpty()) {
                            todo.tags.split(",").firstOrNull()?.let { tagId ->
                                TimelineTagPill(
                                    tagId = tagId,
                                    modifier = Modifier.alpha(contentAlpha)
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // iOS / Liquid Glass 风格
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                    // 人物色调叠加
                    drawRect(personTint)
                }
            )
            // 左侧虚线边框
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val cornerRadius = with(density) { BorderRadius.iOS26.medium.toPx() }
                drawRoundRect(
                    color = personColor.copy(alpha = if (isCompleted) 0.3f else 0.6f),
                    topLeft = Offset.Zero,
                    size = Size(strokeWidth, size.height),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(
                                with(density) { dashWidth.toPx() },
                                with(density) { dashGap.toPx() }
                            )
                        )
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTodoClick(todo.id) }
            )
            .padding(start = Spacing.sm + dashWidth)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 复选框
            TimelineTodoCheckbox(
                isCompleted = isCompleted,
                personColor = personColor,
                onClick = { onToggleStatus(todo.id) },
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            // 内容区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 优先级指示点
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(priorityColor, CircleShape)
                    )

                    // 标题
                    val titleColor = if (isCompleted) labelsTertiary else labelsPrimary
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { alpha = contentAlpha }
                            .drawWithContent {
                                drawContent()
                                if (strikethroughProgress > 0f) {
                                    drawLine(
                                        color = titleColor,
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width * strikethroughProgress, size.height / 2),
                                        strokeWidth = 1.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                    )

                    // 时间文本
                    val timeText = formatTimelineTodoTime(todo)
                    if (timeText.isNotEmpty()) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isCompleted) labelsTertiary else labelsSecondary,
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 人物标签（单人模式下隐藏）
                    if (!singleModeEnabled) {
                        TimelinePersonLabel(
                            label = personLabel,
                            color = personColor,
                            modifier = Modifier.alpha(contentAlpha)
                        )
                    }

                    // 标签
                    if (todo.tags.isNotEmpty()) {
                        todo.tags.split(",").firstOrNull()?.let { tagId ->
                            TimelineTagPill(
                                tagId = tagId,
                                modifier = Modifier.alpha(contentAlpha)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 待办复选框（圆形） */
@Composable
private fun TimelineTodoCheckbox(
    isCompleted: Boolean,
    personColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) personColor
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (isCompleted) personColor else {
                    if (darkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f)
                },
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 人物标签 */
@Composable
private fun TimelinePersonLabel(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current

    Box(
        modifier = modifier
            .background(
                color.copy(alpha = if (darkTheme) 0.2f else 0.12f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            color = color
        )
    }
}

/** 标签小药丸 */
@Composable
private fun TimelineTagPill(
    tagId: String,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val labelsTertiary = getLabelsVibrantTertiary()

    Box(
        modifier = modifier
            .background(
                if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tagId,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = labelsTertiary
        )
    }
}

/** 空时间线卡片 */
@Composable
private fun EmptyTimelineCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val density = LocalDensity.current
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        Surface(
            color = MiuixTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(BorderRadius.lg),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalCafe,
                    contentDescription = null,
                    tint = labelsSecondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "今日暂无安排",
                    style = MaterialTheme.typography.titleMedium,
                    color = labelsSecondary
                )
                Text(
                    text = "点击 + 添加新安排",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.large) },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                }
            )
            .padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalCafe,
            contentDescription = null,
            tint = labelsSecondary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "今日暂无安排",
            style = MaterialTheme.typography.titleMedium,
            color = labelsSecondary
        )
        Text(
            text = "点击 + 添加新安排",
            style = MaterialTheme.typography.bodySmall,
            color = labelsTertiary
        )
    }
}

/** 格式化待办时间显示 */
private fun formatTimelineTodoTime(todo: Todo): String {
    return when {
        todo.hasTimeRange() -> "${todo.getStartTimeString()}-${todo.getEndTimeString()}"
        todo.isDeadlineOnly() -> "截止 ${todo.getEndTimeString()}"
        todo.hasStartTime() -> todo.getStartTimeString()
        else -> ""
    }
}
