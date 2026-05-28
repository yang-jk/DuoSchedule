package com.duoschedule.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoschedule.data.sync.*
import com.duoschedule.ui.settings.components.GlassConfirmDialog
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

@Composable
fun ConflictResolutionDialog(
    conflictItems: List<ConflictItem>,
    onResolve: (ConflictResolution) -> Unit,
    onDismiss: () -> Unit
) {
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    val resolutions = remember { mutableStateMapOf<String, ConflictChoice>() }

    LaunchedEffect(conflictItems) {
        conflictItems.forEach { item ->
            if (item.conflictType != ConflictType.BOTH_DELETED && !resolutions.containsKey(item.courseName)) {
                resolutions[item.courseName] = ConflictChoice.KEEP_LOCAL
            }
        }
    }

    val allItemsHaveSelection = remember(resolutions.size, conflictItems.size) {
        conflictItems.all { item ->
            item.conflictType == ConflictType.BOTH_DELETED || resolutions.containsKey(item.courseName)
        }
    }

    GlassConfirmDialog(
        backdrop = backdrop,
        title = "同步冲突",
        message = "以下课程被双方同时修改，请选择保留哪个版本：",
        confirmText = "确认",
        dismissText = "取消",
        onConfirm = {
            val finalResolutions = conflictItems.associate { item ->
                val choice = resolutions[item.courseName] ?: ConflictChoice.KEEP_LOCAL
                item.courseName to choice
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
                    selectedChoice = resolutions[item.courseName],
                    onChoiceSelected = { choice ->
                        resolutions[item.courseName] = choice
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
    onChoiceSelected: (ConflictChoice) -> Unit
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = item.courseName,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = labelsPrimary
        )

        when (item.conflictType) {
            ConflictType.BOTH_MODIFIED -> {
                Text(
                    text = "双方均修改了此课程",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
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
                CourseSummaryText(
                    localVersion = item.localVersion,
                    cloudVersion = item.cloudVersion,
                    selectedChoice = selectedChoice
                )
            }
            ConflictType.LOCAL_DELETED_CLOUD_MODIFIED -> {
                Text(
                    text = "本地已删除，对方已修改",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
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
                if (selectedChoice == ConflictChoice.KEEP_CLOUD && item.cloudVersion != null) {
                    Text(
                        text = formatCourseSummary(item.cloudVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
            ConflictType.LOCAL_MODIFIED_CLOUD_DELETED -> {
                Text(
                    text = "本地已修改，对方已删除",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
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
                if (selectedChoice == ConflictChoice.KEEP_LOCAL && item.localVersion != null) {
                    Text(
                        text = formatCourseSummary(item.localVersion),
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsSecondary
                    )
                }
            }
            ConflictType.BOTH_DELETED -> {
                Text(
                    text = "双方均已删除",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary
                )
            }
        }
    }
}

@Composable
private fun CourseSummaryText(
    localVersion: CloudCourse?,
    cloudVersion: CloudCourse?,
    selectedChoice: ConflictChoice?
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    when (selectedChoice) {
        ConflictChoice.KEEP_LOCAL -> {
            if (localVersion != null) {
                Text(
                    text = formatCourseSummary(localVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )
            }
        }
        ConflictChoice.KEEP_CLOUD -> {
            if (cloudVersion != null) {
                Text(
                    text = formatCourseSummary(cloudVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )
            }
        }
        ConflictChoice.KEEP_BOTH -> {
            if (localVersion != null) {
                Text(
                    text = "本地: ${formatCourseSummary(localVersion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )
            }
            if (cloudVersion != null) {
                Text(
                    text = "云端: ${formatCourseSummary(cloudVersion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary
                )
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
