package com.duoschedule.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.BlendMode

private val LocalDialogBackdrop = compositionLocalOf<Backdrop?> { null }

@Composable
fun TextInputAlert(
    backdrop: Backdrop,
    title: String,
    label: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        GlassDialogContainer(backdrop = backdrop) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            IOSTextField(
                value = value,
                onValueChange = { value = it },
                label = label,
                placeholder = placeholder,
                singleLine = singleLine,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            IOSDialogButtons(
                onDismiss = onDismiss,
                onConfirm = { onConfirm(value) },
                confirmEnabled = value.isNotBlank()
            )
        }
    }
}

@Composable
fun NumberInputAlert(
    backdrop: Backdrop,
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
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        GlassDialogContainer(backdrop = backdrop) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            IOSTextField(
                value = value,
                onValueChange = {
                    value = it
                    val num = it.toIntOrNull()
                    isError = num == null || num < minValue || num > maxValue
                },
                label = label,
                placeholder = placeholder,
                singleLine = true,
                isError = isError,
                errorMessage = if (isError) "请输入 $minValue-$maxValue 之间的数字" else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            IOSDialogButtons(
                onDismiss = onDismiss,
                onConfirm = {
                    val num = value.toIntOrNull()
                    if (num != null && num in minValue..maxValue) {
                        onConfirm(num)
                    }
                },
                confirmEnabled = !isError && value.toIntOrNull() != null
            )
        }
    }
}

@Composable
fun NumberInputAlert(
    backdrop: Backdrop,
    title: String,
    label: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    range: IntRange,
    placeholder: String = ""
) {
    NumberInputAlert(
        backdrop = backdrop,
        title = title,
        label = label,
        initialValue = initialValue,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        minValue = range.first,
        maxValue = range.last,
        placeholder = placeholder
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeAlert(
    backdrop: Backdrop,
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
    
    var startHour by remember { mutableIntStateOf(startParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var startMinute by remember { mutableIntStateOf(startParts.getOrElse(1) { "00" }.toIntOrNull() ?: 0) }
    var endHour by remember { mutableIntStateOf(endParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var endMinute by remember { mutableIntStateOf(endParts.getOrElse(1) { "45" }.toIntOrNull() ?: 45) }
    
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

    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        GlassTimePickerDialogContainer(backdrop = backdrop) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                Spacer(modifier = Modifier.height(Spacing.xs))
                
                Text(
                    text = "第${periodIndex + 1}节课",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                TimeRangeWheelPicker(
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
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "结束时间必须晚于开始时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                IOSDialogButtons(
                    onDismiss = onDismiss,
                    onConfirm = { onConfirm(timeRange) },
                    confirmEnabled = isValid
                )
            }
        }
    }
}

@Composable
fun GlassDialogContainer(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val dialogBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .width(300.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(24.dp) },
                effects = {
                    colorControls(
                        brightness = if (darkTheme) 0f else 0.2f,
                        saturation = 1.5f
                    )
                    blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                    lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                },
                highlight = { Highlight.Plain },
                exportedBackdrop = dialogBackdrop,
                onDrawSurface = { drawRect(containerColor) }
            )
    ) {
        CompositionLocalProvider(
            LocalDialogBackdrop provides dialogBackdrop
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
private fun IOSAlertDialogContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    val dialogBackground = getDialogBackgroundColor()

    Box(
        modifier = Modifier
            .width(300.dp)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
            .background(dialogBackground)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun GlassTimePickerDialogContainer(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val dialogBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(24.dp) },
                effects = {
                    colorControls(
                        brightness = if (darkTheme) 0f else 0.2f,
                        saturation = 1.5f
                    )
                    blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                    lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                },
                highlight = { Highlight.Plain },
                exportedBackdrop = dialogBackdrop,
                onDrawSurface = { drawRect(containerColor) }
            )
            .padding(vertical = Spacing.lg, horizontal = Spacing.lg)
    ) {
        CompositionLocalProvider(
            LocalDialogBackdrop provides dialogBackdrop
        ) {
            content()
        }
    }
}

@Composable
private fun TimePickerDialogContainer(
    content: @Composable () -> Unit
) {
    val dialogBackground = getDialogBackgroundColor()

    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
            .background(dialogBackground)
            .padding(vertical = Spacing.lg, horizontal = Spacing.lg)
    ) {
        content()
    }
}

@Composable
private fun IOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    val fieldBackground = if (darkTheme) {
        Color(0x29EBEBF5)
    } else {
        Color(0x29787880)
    }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelsSecondary,
            modifier = Modifier.padding(bottom = Spacing.xs)
        )
        
        Box(
            modifier = modifier
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(fieldBackground)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                placeholder = {
                    if (placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = labelsSecondary
                        )
                    }
                },
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = labelsPrimary,
                    unfocusedTextColor = labelsPrimary,
                    cursorColor = BrandColors.Primary
                ),
                shape = ContinuousRoundedRectangle(BorderRadius.iOS26.container)
            )
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
    }
}

@Composable
private fun IOSDialogButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    dismissText: String = "取消",
    confirmText: String = "确定"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        LiquidGlassButton(
            onClick = onDismiss,
            text = dismissText,
            style = LiquidGlassButtonStyle.NonTinted,
            modifier = Modifier.weight(1f)
        )
        
        LiquidGlassButton(
            onClick = onConfirm,
            text = confirmText,
            style = LiquidGlassButtonStyle.Tinted,
            enabled = confirmEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GlassAlertDialog(
    backdrop: Backdrop,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    width: Dp = 300.dp
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val dialogBackdrop = rememberLayerBackdrop()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .width(width)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = labelsPrimary
                    )
                    
                    if (message != null) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelsSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
fun IOSAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    width: Dp = 300.dp
) {
    val dialogBackground = getDialogBackgroundColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .width(width)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(dialogBackground)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                if (message != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun GlassLoadingDialog(
    backdrop: Backdrop,
    message: String,
    onDismiss: () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val dialogBackdrop = rememberLayerBackdrop()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = BrandColors.Primary
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = labelsPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun IOSLoadingDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val dialogBackground = getDialogBackgroundColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(dialogBackground)
        ) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = BrandColors.Primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = labelsPrimary
                )
            }
        }
    }
}

@Composable
fun GlassConfirmDialog(
    backdrop: Backdrop,
    title: String,
    message: String? = null,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val dialogBackdrop = rememberLayerBackdrop()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = labelsPrimary
                    )
                    
                    if (message != null) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelsSecondary
                        )
                    }
                    
                    if (content != null) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            content = content
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        LiquidGlassButton(
                            onClick = onDismiss,
                            text = dismissText,
                            style = LiquidGlassButtonStyle.NonTinted,
                            modifier = Modifier.weight(1f)
                        )
                        
                        LiquidGlassButton(
                            onClick = onConfirm,
                            text = confirmText,
                            style = LiquidGlassButtonStyle.Tinted,
                            enabled = confirmEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IOSConfirmDialog(
    title: String,
    message: String? = null,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val dialogBackground = getDialogBackgroundColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(dialogBackground)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                if (message != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsSecondary
                    )
                }
                
                if (content != null) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = dismissText,
                        style = LiquidGlassButtonStyle.NonTinted,
                        modifier = Modifier.weight(1f)
                    )
                    
                    LiquidGlassButton(
                        onClick = onConfirm,
                        text = confirmText,
                        style = LiquidGlassButtonStyle.Tinted,
                        enabled = confirmEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSuccessDialog(
    backdrop: Backdrop,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionText: String = "查看",
    dismissText: String = "返回"
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val successColor = if (darkTheme) IOS26Colors.TintGreen else IOSColors.Green
    val dialogBackdrop = rememberLayerBackdrop()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = successColor,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = labelsPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    if (onAction != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            LiquidGlassButton(
                                onClick = onDismiss,
                                text = dismissText,
                                style = LiquidGlassButtonStyle.NonTinted,
                                modifier = Modifier.weight(1f)
                            )
                            
                            LiquidGlassButton(
                                onClick = onAction,
                                text = actionText,
                                style = LiquidGlassButtonStyle.Tinted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        LiquidGlassButton(
                            onClick = onDismiss,
                            text = dismissText,
                            style = LiquidGlassButtonStyle.Tinted,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IOSSuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionText: String = "查看",
    dismissText: String = "返回"
) {
    val dialogBackground = getDialogBackgroundColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val successColor = if (LocalDarkTheme.current) IOS26Colors.TintGreen else IOSColors.Green
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(dialogBackground)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = successColor,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary
                )
                
                Spacer(modifier = Modifier.height(Spacing.sm))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                if (onAction != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        LiquidGlassButton(
                            onClick = onDismiss,
                            text = dismissText,
                            style = LiquidGlassButtonStyle.NonTinted,
                            modifier = Modifier.weight(1f)
                        )
                        
                        LiquidGlassButton(
                            onClick = onAction,
                            text = actionText,
                            style = LiquidGlassButtonStyle.Tinted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = dismissText,
                        style = LiquidGlassButtonStyle.Tinted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun GlassErrorDialog(
    backdrop: Backdrop,
    title: String = "导入失败",
    message: String? = null,
    errors: List<String> = emptyList(),
    onDismiss: () -> Unit,
    dismissText: String = "确定"
) {
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val errorColor = if (darkTheme) SemanticColors.ErrorDark else SemanticColors.ErrorLight
    val dialogBackdrop = rememberLayerBackdrop()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (darkTheme) 0f else 0.2f,
                            saturation = 1.5f
                        )
                        blur(if (darkTheme) 8.dp.toPx() else 16.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    exportedBackdrop = dialogBackdrop,
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            CompositionLocalProvider(
                LocalDialogBackdrop provides dialogBackdrop
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = errorColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = labelsPrimary
                        )
                    }
                    
                    if (message != null) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelsSecondary
                        )
                    }
                    
                    if (errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        
                        val optionBackground = if (LocalDarkTheme.current) {
                            Color(0x29EBEBF5)
                        } else {
                            Color(0x29787880)
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                                .background(optionBackground)
                                .padding(Spacing.sm)
                        ) {
                            errors.take(5).forEach { error ->
                                Text(
                                    text = "• $error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = errorColor,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            if (errors.size > 5) {
                                Text(
                                    text = "还有 ${errors.size - 5} 条错误...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelsSecondary,
                                    modifier = Modifier.padding(top = Spacing.xs)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    
                    LiquidGlassButton(
                        onClick = onDismiss,
                        text = dismissText,
                        style = LiquidGlassButtonStyle.Tinted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun IOSErrorDialog(
    title: String = "导入失败",
    message: String? = null,
    errors: List<String> = emptyList(),
    onDismiss: () -> Unit,
    dismissText: String = "确定"
) {
    val dialogBackground = getDialogBackgroundColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val errorColor = if (LocalDarkTheme.current) SemanticColors.ErrorDark else SemanticColors.ErrorLight
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                .background(dialogBackground)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = errorColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = labelsPrimary
                    )
                }
                
                if (message != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsSecondary
                    )
                }
                
                if (errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    val optionBackground = if (LocalDarkTheme.current) {
                        Color(0x29EBEBF5)
                    } else {
                        Color(0x29787880)
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.container))
                            .background(optionBackground)
                            .padding(Spacing.sm)
                    ) {
                        errors.take(5).forEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = errorColor,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        if (errors.size > 5) {
                            Text(
                                text = "还有 ${errors.size - 5} 条错误...",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelsSecondary,
                                modifier = Modifier.padding(top = Spacing.xs)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                LiquidGlassButton(
                    onClick = onDismiss,
                    text = dismissText,
                    style = LiquidGlassButtonStyle.Tinted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
