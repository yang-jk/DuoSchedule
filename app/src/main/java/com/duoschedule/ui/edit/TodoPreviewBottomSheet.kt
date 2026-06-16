package com.duoschedule.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.Priority
import com.duoschedule.data.model.Todo
import com.duoschedule.data.model.TodoStatus
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoPreviewBottomSheet(
    todo: Todo,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val appThemeMode = LocalAppThemeMode.current
    val darkTheme = LocalDarkTheme.current
    val cardBackgroundColor = getFillsVibrantTertiary()

    val dateFormatter = DateTimeFormatter.ofPattern("M月d日")
    val dateString = try {
        LocalDate.ofEpochDay(todo.date).format(dateFormatter)
    } catch (e: Exception) {
        ""
    }

    val priorityText = when (todo.priority) {
        Priority.HIGH -> "高"
        Priority.MEDIUM -> "中"
        Priority.LOW -> "低"
    }

    val statusText = when (todo.status) {
        TodoStatus.PENDING -> "待完成"
        TodoStatus.COMPLETED -> "已完成"
    }

    if (appThemeMode == AppThemeMode.MIUIX) {
        WindowBottomSheet(
            show = true,
            title = todo.title,
            cornerRadius = getRoundedCorner(),
            onDismissRequest = onDismiss
        ) {
            Surface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(getRoundedCorner())
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (todo.description.isNotEmpty()) {
                        TodoInfoRowMiuix(label = "描述", value = todo.description)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                        )
                    }
                    TodoInfoRowMiuix(label = "日期", value = dateString)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                    )
                    if (todo.hasTimeRange() || todo.hasStartTime()) {
                        TodoInfoRowMiuix(label = "时间", value = todo.getTimeString())
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                        )
                    } else if (todo.isDeadlineOnly()) {
                        TodoInfoRowMiuix(label = "截止时间", value = todo.getEndTimeString())
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                        )
                    }
                    TodoInfoRowMiuix(label = "优先级", value = priorityText)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                    )
                    TodoInfoRowMiuix(label = "状态", value = statusText)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("删除")
                }
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    top.yukonga.miuix.kmp.basic.Text("编辑")
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
        return
    }

    GlassBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        backdrop = backdrop,
        darkTheme = darkTheme
    ) { bottomSheetBackdrop ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlassBottomSheetDefaults.ContentHorizontalPadding)
                .padding(top = GlassBottomSheetDefaults.ContentTopPadding, bottom = GlassBottomSheetDefaults.ContentBottomPadding)
        ) {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = getLabelsVibrantPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ContinuousRoundedRectangle(BorderRadius.xl))
                    .background(cardBackgroundColor)
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (todo.description.isNotEmpty()) {
                        TodoInfoRow(
                            label = "描述",
                            value = todo.description
                        )
                        Separator()
                    }
                    TodoInfoRow(
                        label = "日期",
                        value = dateString
                    )
                    Separator()
                    if (todo.hasTimeRange() || todo.hasStartTime()) {
                        TodoInfoRow(
                            label = "时间",
                            value = todo.getTimeString()
                        )
                        Separator()
                    } else if (todo.isDeadlineOnly()) {
                        TodoInfoRow(
                            label = "截止时间",
                            value = todo.getEndTimeString()
                        )
                        Separator()
                    }
                    TodoInfoRow(
                        label = "优先级",
                        value = priorityText
                    )
                    Separator()
                    TodoInfoRow(
                        label = "状态",
                        value = statusText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassButton(
                    text = "删除",
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    style = LiquidGlassButtonStyle.NonTinted
                )

                LiquidGlassButton(
                    text = "编辑",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    style = LiquidGlassButtonStyle.Tinted
                )
            }
        }
    }
}

@Composable
private fun TodoInfoRow(
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
            color = getLabelsVibrantSecondary()
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = getLabelsVibrantPrimary()
        )
    }
}

@Composable
private fun TodoInfoRowMiuix(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        top.yukonga.miuix.kmp.basic.Text(
            text = label,
            color = MiuixTheme.colorScheme.onBackgroundVariant
        )
        top.yukonga.miuix.kmp.basic.Text(
            text = value,
            color = MiuixTheme.colorScheme.onBackground
        )
    }
}
