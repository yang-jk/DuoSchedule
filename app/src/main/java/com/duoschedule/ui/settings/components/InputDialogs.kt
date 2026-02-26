package com.duoschedule.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.theme.getDialogBackgroundColor

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                shape = MaterialTheme.shapes.medium,
                placeholder = if (placeholder.isNotEmpty()) {
                    { Text(placeholder) }
                } else null
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
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

@Composable
fun NumberInputDialog(
    title: String,
    label: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
    placeholder: String = ""
) {
    var value by remember(initialValue) { mutableStateOf(initialValue.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        val num = it.toIntOrNull()
                        isError = num == null || num < minValue || num > maxValue
                    },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = if (placeholder.isNotEmpty()) {
                        { Text(placeholder) }
                    } else null,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("请输入 $minValue-$maxValue 之间的数字") }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val num = value.toIntOrNull()
                    if (num != null && num in minValue..maxValue) {
                        onConfirm(num)
                    }
                },
                enabled = !isError && value.toIntOrNull() != null
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeInputDialog(
    title: String,
    periodIndex: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialValue.split("-")
    val startTime = parts.getOrElse(0) { "08:00" }
    val endTime = parts.getOrElse(1) { "08:45" }
    
    val startParts = startTime.split(":")
    val endParts = endTime.split(":")
    
    var startHour by remember { mutableStateOf(startParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var startMinute by remember { mutableStateOf(startParts.getOrElse(1) { "00" }.toIntOrNull() ?: 0) }
    var endHour by remember { mutableStateOf(endParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var endMinute by remember { mutableStateOf(endParts.getOrElse(1) { "45" }.toIntOrNull() ?: 45) }
    
    val timeRange = remember(startHour, startMinute, endHour, endMinute) {
        val start = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
        val end = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"
        "$start-$end"
    }
    
    val isValid = remember(startHour, startMinute, endHour, endMinute) {
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute
        endTotal > startTotal
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    "第${periodIndex + 1}节课的时间",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                WheelTimeRangePicker(
                    initialStartHour = startHour,
                    initialStartMinute = startMinute,
                    initialEndHour = endHour,
                    initialEndMinute = endMinute,
                    onTimeRangeChange = { sh, sm, eh, em ->
                        startHour = sh
                        startMinute = sm
                        endHour = eh
                        endMinute = em
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (!isValid) {
                    Text(
                        "结束时间必须晚于开始时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timeRange)
                },
                enabled = isValid
            ) {
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

@Composable
fun SliderInputDialog(
    title: String,
    label: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 100,
    step: Int = 1
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = getDialogBackgroundColor(),
        shape = MaterialTheme.shapes.large,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$minValue")
                    Text(
                        "$value",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("$maxValue")
                }
                Slider(
                    value = value.toFloat(),
                    onValueChange = { value = it.toInt() },
                    valueRange = minValue.toFloat()..maxValue.toFloat(),
                    steps = (maxValue - minValue) / step - 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
