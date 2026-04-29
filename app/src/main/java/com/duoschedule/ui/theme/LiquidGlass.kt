package com.duoschedule.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = ContinuousRoundedRectangle(BorderRadius.iOS26.container),
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    content: @Composable ColumnScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

    val layer1Tint = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Tint
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Tint
    }

    val layer1Alpha = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Alpha
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Alpha
    }

    val layer2Base = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer2_Base
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer2_Base
    }

    val glassEffect = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.GlassEffect
    } else {
        LiquidGlassColors.BottomSheet.Light.GlassEffect
    }

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(with(density) { GlassBottomSheetDefaults.BlurRadius.toPx() })
                    lens(
                        refractionHeight = with(density) { GlassBottomSheetDefaults.LensRefractionHeight.toPx() },
                        refractionAmount = with(density) { GlassBottomSheetDefaults.LensRefractionAmount.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(layer1Tint.copy(alpha = layer1Alpha))
                    drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                    drawRect(glassEffect)
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

enum class LiquidGlassButtonStyle {
    Tinted,
    NonTinted
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    enabled: Boolean = true,
    style: LiquidGlassButtonStyle = LiquidGlassButtonStyle.Tinted,
    width: Dp = ComponentSize.LiquidGlassButton.TextButtonWidth
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "button_scale"
    )

    val tintColor = LiquidGlassColors.Button.Tinted.TintColor
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    
    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light
    val textColor = if (darkTheme) nonTinted.TextColorDark else nonTinted.TextColorLight

    Box(
        modifier = modifier
            .width(width)
            .height(ComponentSize.LiquidGlassButton.TextButtonHeight)
            .scale(scale)
            .shadow(
                elevation = 40.dp,
                shape = CircleShape,
                ambientColor = Color.Transparent,
                spotColor = shadowColor
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4.dp.toPx())
                    lens(
                        refractionHeight = 16.dp.toPx(),
                        refractionAmount = 32.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    if (style == LiquidGlassButtonStyle.Tinted) {
                        drawRect(tintColor, blendMode = BlendMode.Hue)
                        drawRect(tintColor.copy(alpha = 0.75f))
                    } else {
                        drawRect(grayTint, blendMode = BlendMode.Hue)
                        drawRect(fillLayer3)
                    }
                }
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (style == LiquidGlassButtonStyle.Tinted) 
                LiquidGlassColors.Button.Tinted.TextColor 
            else 
                textColor
        )
    }
}

@Composable
fun Toggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = labelsPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = labelsSecondary
            )
        }
        LiquidToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            backdrop = backdrop,
            enabled = enabled
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    transparentBackground: Boolean = false
) {
    val backgroundColor = if (transparentBackground) {
        Color.Transparent
    } else {
        getFillsVibrantTertiary()
    }
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val darkTheme = LocalDarkTheme.current
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            color = if (enabled) labelsPrimary else labelsTertiary
        ),
        keyboardOptions = KeyboardOptions.Default,
        keyboardActions = KeyboardActions.Default,
        cursorBrush = Brush.verticalGradient(
            colors = listOf(
                if (darkTheme) IOS26Colors.TintBlue else IOSColors.Blue,
                if (darkTheme) IOS26Colors.TintBlue else IOSColors.Blue
            )
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .then(
                        if (!transparentBackground) {
                            Modifier.background(
                                backgroundColor,
                                ContinuousRoundedRectangle(BorderRadius.iOS26.container)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = labelsTertiary
                        )
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}

@Composable
fun GlassSelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "chip_scale"
    )
    
    val tintColor = if (darkTheme) 
        LiquidGlassColors.Button.TintColorDark 
    else 
        LiquidGlassColors.Button.TintColorLight
    
    val backgroundColor = if (darkTheme)
        LiquidGlassColors.Button.BackgroundDark
    else
        LiquidGlassColors.Button.BackgroundLight
    
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    
    val shape = ContinuousRoundedRectangle(BorderRadius.pill)

    Box(
        modifier = modifier
            .height(ComponentSize.LiquidGlassButton.TextButtonHeight)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 40.dp.toPx()
                this.shape = shape
                clip = true
                ambientShadowColor = Color.Transparent
                spotShadowColor = shadowColor
            }
            .background(backgroundColor)
            .then(
                if (!darkTheme) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tintColor.copy(alpha = 0.15f),
                                tintColor.copy(alpha = 0.08f)
                            )
                        )
                    )
                } else Modifier
            )
            .background(LiquidGlassColors.Button.GlassEffectOverlay)
            .then(
                if (selected) {
                    Modifier.background(selectedColor.copy(alpha = 0.15f))
                } else Modifier
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = selectedColor,
                        shape = shape
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) selectedColor else getLabelsVibrantPrimary()
        )
    }
}

@Composable
fun GlassAlert(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val backgroundColor = if (darkTheme) {
        LiquidGlassColors.GlassBackgroundDark
    } else {
        LiquidGlassColors.GlassBackgroundLight
    }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        shape = ContinuousRoundedRectangle(BorderRadius.xxl),
        title = {
            Text(
                text = title,
                color = getLabelsVibrantPrimary(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = text,
                color = getLabelsVibrantSecondary(),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "icon_button_scale"
    )
    
    val backgroundColor = if (darkTheme)
        LiquidGlassColors.Button.BackgroundDark
    else
        LiquidGlassColors.Button.BackgroundLight
    
    val tintColor = if (darkTheme)
        LiquidGlassColors.Button.TintColorDark
    else
        LiquidGlassColors.Button.TintColorLight
    
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    val shape = ContinuousRoundedRectangle(50)
    
    Box(
        modifier = modifier
            .size(ComponentSize.LiquidGlassButton.IconButtonSize)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 40.dp.toPx()
                this.shape = shape
                clip = true
                ambientShadowColor = Color.Transparent
                spotShadowColor = shadowColor
            }
            .background(backgroundColor, shape)
            .then(
                if (!darkTheme) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tintColor.copy(alpha = 0.15f),
                                tintColor.copy(alpha = 0.08f)
                            )
                        ),
                        shape = shape
                    )
                } else Modifier
            )
            .background(LiquidGlassColors.Button.GlassEffectOverlay, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

enum class GlassSymbolButtonStyle {
    Tinted,
    NonTinted
}

@Composable
fun GlassSymbolButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    enabled: Boolean = true,
    style: GlassSymbolButtonStyle = GlassSymbolButtonStyle.Tinted,
    size: Dp = ComponentSize.LiquidGlassButton.IconButtonSize
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "symbol_button_scale"
    )

    val isTinted = style == GlassSymbolButtonStyle.Tinted
    val tintColor = LiquidGlassColors.Button.Tinted.TintColor
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    
    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light
    val textColor = if (darkTheme) nonTinted.TextColorDark else nonTinted.TextColorLight

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = 40.dp,
                shape = CircleShape,
                ambientColor = Color.Transparent,
                spotColor = shadowColor
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4.dp.toPx())
                    lens(
                        refractionHeight = 16.dp.toPx(),
                        refractionAmount = 32.dp.toPx(),
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    if (isTinted) {
                        drawRect(tintColor, blendMode = BlendMode.Hue)
                        drawRect(tintColor.copy(alpha = 0.75f))
                    } else {
                        drawRect(grayTint, blendMode = BlendMode.Hue)
                        drawRect(fillLayer3)
                    }
                }
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight(590),
                lineHeight = 22.sp
            ),
            color = if (isTinted) Color.White else textColor,
            modifier = if (!isTinted) Modifier.graphicsLayer { 
                this.renderEffect = null
            } else Modifier
        )
    }
}

@Composable
fun GlassSymbolIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    enabled: Boolean = true,
    style: GlassSymbolButtonStyle = GlassSymbolButtonStyle.NonTinted,
    size: Dp = ComponentSize.LiquidGlassButton.IconButtonSize,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    icon: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "symbol_icon_button_scale"
    )

    val isTinted = style == GlassSymbolButtonStyle.Tinted
    val tintColor = LiquidGlassColors.Button.Tinted.TintColor
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    
    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light

    Box(
        modifier = modifier
            .padding(contentPadding)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .shadow(
                    elevation = 40.dp,
                    shape = CircleShape,
                    ambientColor = Color.Transparent,
                    spotColor = shadowColor
                )
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4.dp.toPx())
                        lens(
                            refractionHeight = 16.dp.toPx(),
                            refractionAmount = 32.dp.toPx(),
                            chromaticAberration = true
                        )
                    },
                    onDrawSurface = {
                        if (isTinted) {
                            drawRect(tintColor, blendMode = BlendMode.Hue)
                            drawRect(tintColor.copy(alpha = 0.75f))
                        } else {
                            drawRect(grayTint, blendMode = BlendMode.Hue)
                            drawRect(fillLayer3)
                        }
                    }
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val iconColor = when {
                !enabled -> {
                    if (isTinted) 
                        Color.White.copy(alpha = 0.5f)
                    else 
                        Color(0xFF1A1A1A).copy(alpha = 0.5f)
                }
                isTinted -> Color.White
                else -> Color(0xFF1A1A1A)
            }
            
            Box(
                modifier = Modifier.graphicsLayer {
                    if (!isTinted) {
                        renderEffect = null
                    }
                }
            ) {
                CompositionLocalProvider(LocalContentColor provides iconColor) {
                    icon()
                }
            }
        }
    }
}
