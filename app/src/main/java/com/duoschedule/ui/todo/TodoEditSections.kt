package com.duoschedule.ui.todo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.data.model.TodoTag
import com.duoschedule.ui.settings.components.SettingsDefaults
import com.duoschedule.ui.settings.components.SettingsSection
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.BrandColors
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
import com.kyant.capsule.ContinuousRoundedRectangle
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal val MicroTween: TweenSpec<Float> = tween(100, easing = FastOutSlowInEasing)

@Composable
internal fun SettingsNavigationRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MicroTween,
        label = "nav_row_scale"
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
                imageVector = icon,
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
internal fun ErrorMessageCard(
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
internal fun BasicInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    personType: PersonType,
    onPersonTypeChange: (PersonType) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    SettingsSection(title = "基本信息", modifier = modifier) {
        // Person type selector
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
        // Title text field
        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), label = "请输入待办标题", useLabelAsPlaceholder = true)
        } else {
            GlassTextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), placeholder = "请输入待办标题", transparentBackground = true)
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        // Description text field
        if (appThemeMode == AppThemeMode.MIUIX) {
            TextField(value = description, onValueChange = onDescriptionChange, modifier = Modifier.fillMaxWidth(), label = "添加备注（可选）", useLabelAsPlaceholder = true, maxLines = 4)
        } else {
            GlassTextField(value = description, onValueChange = onDescriptionChange, modifier = Modifier.fillMaxWidth(), placeholder = "添加备注（可选）", transparentBackground = true, singleLine = false, maxLines = 4)
        }
    }
}

@Composable
internal fun DateTimeSection(
    date: Long,
    onDateClick: () -> Unit,
    startHour: Int, startMinute: Int,
    endHour: Int, endMinute: Int,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onClearStartTime: () -> Unit,
    onClearEndTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val labelsTertiary = getLabelsVibrantTertiary()
    val dateText = remember(date) {
        val localDate = LocalDate.ofEpochDay(date)
        DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE").format(localDate)
    }
    SettingsSection(title = "时间", modifier = modifier) {
        // Hint text
        if (appThemeMode == AppThemeMode.MIUIX) {
            top.yukonga.miuix.kmp.basic.Text(text = "不设置时间则为纯待办", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.body2, modifier = Modifier.padding(bottom = Spacing.sm))
        } else {
            Text(text = "不设置时间则为纯待办", color = labelsTertiary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = Spacing.sm))
        }
        // Date row
        SettingsNavigationRow(icon = Icons.Default.CalendarMonth, iconColor = IOSColors.Orange, label = "日期", value = dateText, onClick = onDateClick)
        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
        // Start time row
        TimeRow(label = "开始时间", hour = startHour, minute = startMinute, iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon), iconColor = IOSColors.Blue, onClick = onStartTimeClick, onClear = onClearStartTime)
        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
        // End time row
        TimeRow(label = "结束时间", hour = endHour, minute = endMinute, iconShape = if (appThemeMode == AppThemeMode.MIUIX) RoundedCornerShape(BorderRadius.md) else ContinuousRoundedRectangle(BorderRadius.iOS26.icon), iconColor = IOSColors.Purple, onClick = onEndTimeClick, onClear = onClearEndTime)
    }
}

@Composable
internal fun CategorySection(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    allTags: List<TodoTag>,
    selectedTagIds: Set<String>,
    onTagToggle: (String) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appThemeMode = LocalAppThemeMode.current
    val priorityOptions = listOf(
        Triple(Priority.HIGH, "高", Color(0xFFFF3B30)),
        Triple(Priority.MEDIUM, "中", Color(0xFFFF9500)),
        Triple(Priority.LOW, "低", Color(0xFF34C759))
    )
    SettingsSection(title = "分类", modifier = modifier) {
        // Priority selector
        if (appThemeMode == AppThemeMode.MIUIX) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                priorityOptions.forEach { (p, label, color) ->
                    val isSelected = priority == p
                    top.yukonga.miuix.kmp.basic.Surface(modifier = Modifier.weight(1f).clickable { onPriorityChange(p) }, shape = RoundedCornerShape(BorderRadius.lg), color = if (isSelected) color.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(text = label, color = if (isSelected) color else MiuixTheme.colorScheme.onBackground, style = MiuixTheme.textStyles.body1.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal))
                        }
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                priorityOptions.forEach { (p, label, color) ->
                    GlassSelectableChip(selected = priority == p, onClick = { onPriorityChange(p) }, label = label, selectedColor = color, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        // Tags selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), contentPadding = PaddingValues(end = Spacing.sm)) {
            items(allTags, key = { it.id }) { tag ->
                val isSelected = selectedTagIds.contains(tag.id)
                val tagColor = Color(tag.color)
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Surface(shape = RoundedCornerShape(BorderRadius.pill), color = if (isSelected) tagColor.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant, onClick = { onTagToggle(tag.id) }) {
                        Row(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(tagColor))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(text = tag.name, color = if (isSelected) tagColor else MiuixTheme.colorScheme.onBackground, style = MiuixTheme.textStyles.body2.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal))
                        }
                    }
                } else {
                    GlassSelectableChip(selected = isSelected, onClick = { onTagToggle(tag.id) }, label = tag.name, selectedColor = tagColor)
                }
            }
            item {
                if (appThemeMode == AppThemeMode.MIUIX) {
                    top.yukonga.miuix.kmp.basic.Surface(shape = RoundedCornerShape(BorderRadius.pill), color = MiuixTheme.colorScheme.surfaceVariant, onClick = onAddTag) {
                        Row(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MiuixTheme.colorScheme.onBackgroundVariant)
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            top.yukonga.miuix.kmp.basic.Text(text = "自定义", color = MiuixTheme.colorScheme.onBackgroundVariant, style = MiuixTheme.textStyles.body2)
                        }
                    }
                } else {
                    GlassSelectableChip(selected = false, onClick = onAddTag, label = "+ 自定义", selectedColor = BrandColors.Primary)
                }
            }
        }
    }
}

@Composable
internal fun AdvancedSection(
    linkedCourseSyncId: String?,
    courses: List<Course>,
    onCourseClick: () -> Unit,
    repeatRule: RepeatRule?,
    onRepeatRuleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val linkedCourseName = remember(linkedCourseSyncId, courses) {
        if (linkedCourseSyncId == null) null
        else courses.find { it.syncId == linkedCourseSyncId }?.name
    }
    val repeatText = remember(repeatRule) {
        when {
            repeatRule == null -> "不重复"
            repeatRule.frequency == RepeatFrequency.DAILY -> {
                if (repeatRule.interval == 1) "每天" else "每${repeatRule.interval}天"
            }
            repeatRule.frequency == RepeatFrequency.WEEKLY -> {
                val dayNames = repeatRule.daysOfWeek.split(",").filter { it.isNotBlank() }.mapNotNull { d ->
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
    SettingsSection(title = "高级", modifier = modifier) {
        SettingsNavigationRow(icon = Icons.Default.Link, iconColor = IOSColors.Teal, label = "关联课程", value = linkedCourseName ?: "未关联", onClick = onCourseClick)
        Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
        SettingsNavigationRow(icon = Icons.Default.Repeat, iconColor = IOSColors.Indigo, label = "重复规则", value = repeatText, onClick = onRepeatRuleClick)
    }
}

@Composable
internal fun TimeRow(
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
                GlassSymbolIconButton(onClick = onClear, style = GlassSymbolButtonStyle.NonTinted) {
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
