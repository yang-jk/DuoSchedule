package com.duoschedule.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.WeekType
import com.duoschedule.ui.theme.getDialogBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePreviewBottomSheet(
    course: Course,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = getDialogBackgroundColor(),
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .then(
                        Modifier
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            CourseInfoRow(
                label = "教室地点",
                value = course.location.ifEmpty { "未设置" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CourseInfoRow(
                label = "上课老师",
                value = course.teacher.ifEmpty { "未设置" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val weekText = when (course.weekType) {
                WeekType.ALL -> "第${course.startWeek}-${course.endWeek}周"
                WeekType.ODD -> "单周 (第${course.startWeek}-${course.endWeek}周)"
                WeekType.EVEN -> "双周 (第${course.startWeek}-${course.endWeek}周)"
                WeekType.CUSTOM -> {
                    if (course.customWeeks.isNotEmpty()) {
                        "第${course.customWeeks}周"
                    } else {
                        "自定义周次"
                    }
                }
            }
            CourseInfoRow(
                label = "上课周数",
                value = weekText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除")
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑")
                }
            }
        }
    }
}

@Composable
private fun CourseInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
