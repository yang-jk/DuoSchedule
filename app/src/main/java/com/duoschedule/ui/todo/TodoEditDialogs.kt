package com.duoschedule.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.RepeatFrequency
import com.duoschedule.data.model.RepeatRule
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.BrandColors
import com.duoschedule.ui.theme.GlassSelectableChip
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import com.duoschedule.ui.theme.GlassTextField
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.SegmentOption
import com.duoschedule.ui.theme.SegmentedControl
import com.duoschedule.ui.theme.Separator
import com.duoschedule.ui.theme.Spacing
import com.duoschedule.ui.theme.getLabelsVibrantPrimary
import com.duoschedule.ui.theme.getLabelsVibrantSecondary
import com.duoschedule.ui.theme.getRoundedCorner
import com.kyant.capsule.ContinuousRoundedRectangle
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal sealed class TodoEditDialog {
    data object DatePicker : TodoEditDialog()
    data object StartTimePicker : TodoEditDialog()
    data object EndTimePicker : TodoEditDialog()
    data object DeleteConfirm : TodoEditDialog()
    data object CoursePicker : TodoEditDialog()
    data object AddTag : TodoEditDialog()
    data object RepeatRule : TodoEditDialog()
}

/** 重复频率选项 */
internal enum class RepeatFrequencyOption(val label: String) {
    NONE("不重复"),
    DAILY("每天"),
    WEEKLY("每周"),
    CUSTOM("自定义")
}

/** 周几选项 */
internal val DAY_OF_WEEK_OPTIONS = listOf(
    1 to "周一", 2 to "周二", 3 to "周三", 4 to "周四",
    5 to "周五", 6 to "周六", 7 to "周日"
)

@Composable
internal fun MiuixDatePickerBottomSheet(
    title: String,
    initialDate: LocalDate,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var currentYear by remember { mutableStateOf(initialDate.year) }
    var currentMonth by remember { mutableStateOf(initialDate.monthValue) }
    var currentDay by remember { mutableStateOf(initialDate.dayOfMonth) }
    val maxDayInMonth = remember(currentYear, currentMonth) {
        LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth()
    }
    LaunchedEffect(maxDayInMonth) {
        if (currentDay > maxDayInMonth) currentDay = maxDayInMonth
    }
    WindowBottomSheet(show = true, title = title, cornerRadius = getRoundedCorner(), onDismissRequest = onDismiss, allowDismiss = false) {
        Row(modifier = Modifier.fillMaxWidth().height(225.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(value = currentYear, onValueChange = { currentYear = it }, range = 2024..2035, label = { "${it}年" }, modifier = Modifier.weight(1f))
            NumberPicker(value = currentMonth, onValueChange = { currentMonth = it }, range = 1..12, label = { "${it}月" }, wrapAround = true, modifier = Modifier.weight(0.6f))
            NumberPicker(value = currentDay, onValueChange = { currentDay = it }, range = 1..maxDayInMonth, label = { "${it}日" }, wrapAround = true, modifier = Modifier.weight(0.6f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) { top.yukonga.miuix.kmp.basic.Text("取消") }
            Button(onClick = { onDateSelected(LocalDate.of(currentYear, currentMonth, currentDay).toEpochDay()) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) { top.yukonga.miuix.kmp.basic.Text("确定") }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
internal fun MiuixTimePickerBottomSheet(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    WindowBottomSheet(show = true, title = title, cornerRadius = getRoundedCorner(), onDismissRequest = onDismiss, allowDismiss = false) {
        Row(modifier = Modifier.fillMaxWidth().height(225.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(value = hour, onValueChange = { hour = it }, range = 0..23, label = { it.toString().padStart(2, '0') }, wrapAround = true, modifier = Modifier.weight(1f))
            top.yukonga.miuix.kmp.basic.Text(text = ":", color = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 2.dp))
            NumberPicker(value = minute, onValueChange = { minute = it }, range = 0..59, label = { it.toString().padStart(2, '0') }, wrapAround = true, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) { top.yukonga.miuix.kmp.basic.Text("取消") }
            Button(onClick = { onTimeSelected(hour, minute) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColorsPrimary()) { top.yukonga.miuix.kmp.basic.Text("确定") }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RepeatRuleDialog(
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
            MiuixDatePickerBottomSheet(
                title = "选择日期",
                initialDate = LocalDate.now(),
                onDateSelected = { epochDay ->
                    if (epochDay !in customDates) {
                        customDates = (customDates + epochDay).sorted()
                    }
                    showCustomDatePicker = false
                },
                onDismiss = { showCustomDatePicker = false }
            )
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
            MiuixDatePickerBottomSheet(
                title = "选择结束日期",
                initialDate = ruleEndDate?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now(),
                onDateSelected = { epochDay ->
                    ruleEndDate = epochDay
                    showEndDatePicker = false
                },
                onDismiss = { showEndDatePicker = false }
            )
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
internal fun RepeatRuleDialogContent(
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
                                GlassSymbolIconButton(onClick = { onRemoveCustomDate(epochDay) }, style = GlassSymbolButtonStyle.NonTinted) {
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
internal fun TodoDatePickerDialog(
    currentDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        MiuixDatePickerBottomSheet(
            title = "选择日期",
            initialDate = LocalDate.ofEpochDay(currentDate),
            onDateSelected = onDateSelected,
            onDismiss = onDismiss
        )
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
internal fun TodoTimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    title: String,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        MiuixTimePickerBottomSheet(
            title = title,
            initialHour = currentHour,
            initialMinute = currentMinute,
            onTimeSelected = onTimeSelected,
            onDismiss = onDismiss
        )
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
internal fun CoursePickerDialog(
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
internal fun AddTagDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "添加标签",
            onDismissRequest = onDismiss
        ) {
            TextField(
                value = tagName,
                onValueChange = { tagName = it },
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
                    onClick = { onConfirm(tagName) },
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
                    onValueChange = { tagName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "输入标签名称",
                    transparentBackground = true
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(tagName) }) {
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
