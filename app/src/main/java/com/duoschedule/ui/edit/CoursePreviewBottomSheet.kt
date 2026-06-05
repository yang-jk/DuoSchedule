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
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.WeekType
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePreviewBottomSheet(
    course: Course,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val appThemeMode = LocalAppThemeMode.current
    val darkTheme = LocalDarkTheme.current
    val cardBackgroundColor = getFillsVibrantTertiary()

    if (appThemeMode == AppThemeMode.MIUIX) {
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

        WindowBottomSheet(
            show = true,
            title = course.name,
            onDismissRequest = onDismiss
        ) {
            Surface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CourseInfoRowMiuix(label = "教室地点", value = course.location.ifEmpty { "未设置" })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                    )
                    CourseInfoRowMiuix(label = "上课老师", value = course.teacher.ifEmpty { "未设置" })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.1f))
                    )
                    CourseInfoRowMiuix(label = "上课周数", value = weekText)
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
                text = course.name,
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
                    CourseInfoRow(
                        label = "教室地点",
                        value = course.location.ifEmpty { "未设置" }
                    )

                    Separator()

                    CourseInfoRow(
                        label = "上课老师",
                        value = course.teacher.ifEmpty { "未设置" }
                    )

                    Separator()

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
private fun CourseInfoRowMiuix(
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
