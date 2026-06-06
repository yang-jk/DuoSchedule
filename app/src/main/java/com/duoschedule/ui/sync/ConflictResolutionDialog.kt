package com.duoschedule.ui.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.sync.*
import com.duoschedule.ui.settings.components.GlassConfirmDialog
import com.duoschedule.ui.theme.getRoundedCorner
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun ConflictResolutionDialog(
    conflictItems: List<ConflictItem>,
    onResolve: (ConflictResolution) -> Unit,
    onDismiss: () -> Unit
) {
    val appThemeMode = LocalAppThemeMode.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    val resolutions = remember { mutableStateMapOf<String, ConflictChoice>() }

    LaunchedEffect(conflictItems) {
        conflictItems.forEach { item ->
            if (item.conflictType != ConflictType.BOTH_DELETED && !resolutions.containsKey(item.courseKey)) {
                resolutions[item.courseKey] = ConflictChoice.KEEP_LOCAL
            }
        }
    }

    val allItemsHaveSelection = remember(resolutions.size, conflictItems.size) {
        conflictItems.all { item ->
            item.conflictType == ConflictType.BOTH_DELETED || resolutions.containsKey(item.courseKey)
        }
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowDialog(
            show = true,
            title = "同步冲突",
            onDismissRequest = onDismiss
        ) {
            top.yukonga.miuix.kmp.basic.Text(
                text = "以下课程被双方同时修改，请选择保留哪个版本：",
                color = MiuixTheme.colorScheme.onBackgroundVariant
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                conflictItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(MiuixTheme.colorScheme.dividerLine))
                    }
                    ConflictItemSection(
                        item = item,
                        selectedChoice = resolutions[item.courseKey],
                        onChoiceSelected = { choice -> resolutions[item.courseKey] = choice },
                        isMiuix = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors()) {
                    top.yukonga.miuix.kmp.basic.Text("取消")
                }
                Button(
                    onClick = {
                        val finalResolutions = conflictItems.associate { item ->
                            val choice = resolutions[item.courseKey] ?: ConflictChoice.KEEP_LOCAL
                            item.courseKey to choice
                        }
                        onResolve(ConflictResolution(resolutions = finalResolutions))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = allItemsHaveSelection,
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("确认")
                }
            }
        }
        return
    }

    GlassConfirmDialog(
        backdrop = backdrop,
        title = "同步冲突",
        message = "以下课程被双方同时修改，请选择保留哪个版本：",
        confirmText = "确认",
        dismissText = "取消",
        onConfirm = {
            val finalResolutions = conflictItems.associate { item ->
                val choice = resolutions[item.courseKey] ?: ConflictChoice.KEEP_LOCAL
                item.courseKey to choice
            }
            onResolve(ConflictResolution(resolutions = finalResolutions))
        },
        onDismiss = onDismiss,
        confirmEnabled = allItemsHaveSelection
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            conflictItems.forEachIndexed { index, item ->
                if (index > 0) {
                    Separator()
                }
                ConflictItemSection(
                    item = item,
                    selectedChoice = resolutions[item.courseKey],
                    onChoiceSelected = { choice ->
                        resolutions[item.courseKey] = choice
                    }
                )
            }
        }
    }
}

@Composable
private fun ConflictItemSection(
    item: ConflictItem,
    selectedChoice: ConflictChoice?,
    onChoiceSelected: (ConflictChoice) -> Unit,
    isMiuix: Boolean = false
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (isMiuix) {
            top.yukonga.miuix.kmp.basic.Text(
                text = item.courseName,
                style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.SemiBold),
                color = MiuixTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = item.courseName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )
        }

        when (item.conflictType) {
            ConflictType.BOTH_MODIFIED -> {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "双方均修改了此课程",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "双方均修改了此课程",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsTertiary
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (isMiuix) {
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保留本地"
                        )
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "保留云端"
                        )
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_BOTH,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_BOTH) },
                            label = "保留两者"
                        )
                    } else {
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保留本地",
                            selectedColor = BrandColors.Primary
                        )
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "保留云端",
                            selectedColor = BrandColors.Primary
                        )
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_BOTH,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_BOTH) },
                            label = "保留两者",
                            selectedColor = BrandColors.Primary
                        )
                    }
                }
                CourseSummaryText(
                    localVersion = item.localVersion,
                    cloudVersion = item.cloudVersion,
                    selectedChoice = selectedChoice,
                    isMiuix = isMiuix
                )
            }
            ConflictType.LOCAL_DELETED_CLOUD_MODIFIED -> {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "本地已删除，对方已修改",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "本地已删除，对方已修改",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsTertiary
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (isMiuix) {
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保持删除"
                        )
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "恢复对方版本"
                        )
                    } else {
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保持删除",
                            selectedColor = BrandColors.Primary
                        )
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "恢复对方版本",
                            selectedColor = BrandColors.Primary
                        )
                    }
                }
                if (selectedChoice == ConflictChoice.KEEP_CLOUD && item.cloudVersion != null) {
                    if (isMiuix) {
                        top.yukonga.miuix.kmp.basic.Text(
                            text = formatCourseSummary(item.cloudVersion),
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    } else {
                        Text(
                            text = formatCourseSummary(item.cloudVersion),
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                }
            }
            ConflictType.LOCAL_MODIFIED_CLOUD_DELETED -> {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "本地已修改，对方已删除",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "本地已修改，对方已删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsTertiary
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (isMiuix) {
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保留我的版本"
                        )
                        MiuixSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "同意删除"
                        )
                    } else {
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_LOCAL,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_LOCAL) },
                            label = "保留我的版本",
                            selectedColor = BrandColors.Primary
                        )
                        GlassSelectableChip(
                            selected = selectedChoice == ConflictChoice.KEEP_CLOUD,
                            onClick = { onChoiceSelected(ConflictChoice.KEEP_CLOUD) },
                            label = "同意删除",
                            selectedColor = BrandColors.Primary
                        )
                    }
                }
                if (selectedChoice == ConflictChoice.KEEP_LOCAL && item.localVersion != null) {
                    if (isMiuix) {
                        top.yukonga.miuix.kmp.basic.Text(
                            text = formatCourseSummary(item.localVersion),
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    } else {
                        Text(
                            text = formatCourseSummary(item.localVersion),
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary
                        )
                    }
                }
            }
            ConflictType.BOTH_DELETED -> {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "双方均已删除",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "双方均已删除",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixSelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        color = if (selected) MiuixTheme.colorScheme.primary.copy(alpha = 0.12f) else MiuixTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(getRoundedCorner())
    ) {
        top.yukonga.miuix.kmp.basic.Text(
            text = label,
            modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CourseSummaryText(
    localVersion: CloudCourse?,
    cloudVersion: CloudCourse?,
    selectedChoice: ConflictChoice?,
    isMiuix: Boolean = false
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    when (selectedChoice) {
        ConflictChoice.KEEP_LOCAL -> {
            if (localVersion != null) {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = formatCourseSummary(localVersion),
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = formatCourseSummary(localVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
        }
        ConflictChoice.KEEP_CLOUD -> {
            if (cloudVersion != null) {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = formatCourseSummary(cloudVersion),
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = formatCourseSummary(cloudVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
        }
        ConflictChoice.KEEP_BOTH -> {
            if (localVersion != null) {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "本地: ${formatCourseSummary(localVersion)}",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "本地: ${formatCourseSummary(localVersion)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
            if (cloudVersion != null) {
                if (isMiuix) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = "云端: ${formatCourseSummary(cloudVersion)}",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    Text(
                        text = "云端: ${formatCourseSummary(cloudVersion)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
        }
        null -> {}
    }
}

private fun formatCourseSummary(course: CloudCourse?): String {
    if (course == null) return "已删除"
    val dayOfWeekStr = when (course.dayOfWeek) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
        5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
    }
    val timeStr = if (course.isCustomTime) {
        "${course.startHour}:${course.startMinute.toString().padStart(2, '0')}-${course.endHour}:${course.endMinute.toString().padStart(2, '0')}"
    } else {
        "${course.startPeriod}-${course.endPeriod}节"
    }
    val locationStr = if (course.location.isNotBlank()) " ${course.location}" else ""
    return "$dayOfWeekStr $timeStr$locationStr"
}
