package com.duoschedule.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.theme.getDialogBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPickerBottomSheet(
    totalWeeks: Int,
    selectedWeeks: Set<Int>,
    onWeeksChange: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var currentSelectedWeeks by remember(selectedWeeks) { mutableStateOf(selectedWeeks) }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择周数",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSelectButton(
                    label = "单周",
                    onClick = {
                        currentSelectedWeeks = (1..totalWeeks).filter { it % 2 == 1 }.toSet()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickSelectButton(
                    label = "双周",
                    onClick = {
                        currentSelectedWeeks = (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickSelectButton(
                    label = "全部",
                    onClick = {
                        currentSelectedWeeks = (1..totalWeeks).toSet()
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickSelectButton(
                    label = "清空",
                    onClick = {
                        currentSelectedWeeks = emptySet()
                    },
                    modifier = Modifier.weight(1f),
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val weeks = remember(totalWeeks) { (1..totalWeeks).toList() }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weeks) { week ->
                    WeekGridItem(
                        week = week,
                        isSelected = currentSelectedWeeks.contains(week),
                        onClick = {
                            currentSelectedWeeks = if (currentSelectedWeeks.contains(week)) {
                                currentSelectedWeeks - week
                            } else {
                                currentSelectedWeeks + week
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("取消")
                }

                Button(
                    onClick = {
                        onWeeksChange(currentSelectedWeeks)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun QuickSelectButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = if (isDestructive) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun WeekGridItem(
    week: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$week",
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
