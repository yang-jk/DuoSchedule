package com.duoschedule.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

object GlassEffectDefaults {
    val CardBlurRadius = 40.dp
    val CardLensRefractionHeight = 24.dp
    val CardLensRefractionAmount = 48.dp

    val ButtonBlurRadius = 2.dp
    val ButtonLensRefractionHeight = 12.dp
    val ButtonLensRefractionAmount = 24.dp
}

fun BackdropEffectScope.glassCardEffects(
    darkTheme: Boolean,
    density: androidx.compose.ui.unit.Density,
    blurRadius: Dp = GlassEffectDefaults.CardBlurRadius,
    lensRefractionHeight: Dp = GlassEffectDefaults.CardLensRefractionHeight,
    lensRefractionAmount: Dp = GlassEffectDefaults.CardLensRefractionAmount
) {
    colorControls(
        brightness = if (darkTheme) 0f else 0.2f,
        saturation = 1.5f
    )
    blur(with(density) { blurRadius.toPx() })
    lens(
        refractionHeight = with(density) { lensRefractionHeight.toPx() },
        refractionAmount = with(density) { lensRefractionAmount.toPx() },
        chromaticAberration = true,
        depthEffect = true
    )
}

fun BackdropEffectScope.glassButtonEffects(
    density: androidx.compose.ui.unit.Density,
    blurRadius: Dp = GlassEffectDefaults.ButtonBlurRadius,
    lensRefractionHeight: Dp = GlassEffectDefaults.ButtonLensRefractionHeight,
    lensRefractionAmount: Dp = GlassEffectDefaults.ButtonLensRefractionAmount
) {
    vibrancy()
    blur(with(density) { blurRadius.toPx() })
    lens(
        refractionHeight = with(density) { lensRefractionHeight.toPx() },
        refractionAmount = with(density) { lensRefractionAmount.toPx() }
    )
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.glassCardSurfaceColors(darkTheme: Boolean) {
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
    drawRect(layer1Tint.copy(alpha = layer1Alpha))
    drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
    drawRect(glassEffect)
}

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

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    glassCardEffects(darkTheme, density)
                },
                highlight = { Highlight.Plain },
                onDrawSurface = {
                    glassCardSurfaceColors(darkTheme)
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
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()

    val isTinted = style == LiquidGlassButtonStyle.Tinted
    val tintColor = LiquidGlassColors.Button.Tinted.TintColor

    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light
    val textColor = if (darkTheme) nonTinted.TextColorDark else nonTinted.TextColorLight

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    Row(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    glassButtonEffects(density)
                },
                layerBlock = {
                    val width = size.width
                    val height = size.height

                    val progress = interactiveHighlight.pressProgress
                    val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                    val maxOffset = size.minDimension
                    val initialDerivative = 0.05f
                    val offset = interactiveHighlight.offset
                    translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                    translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                    val maxDragScale = 4f.dp.toPx() / size.height
                    val offsetAngle = atan2(offset.y, offset.x)
                    scaleX =
                        scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                    scaleY =
                        scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
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
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                enabled = enabled,
                onClick = onClick
            )
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier)
            .height(ComponentSize.LiquidGlassButton.TextButtonHeight)
            .padding(horizontal = 16f.dp),
        horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (isTinted)
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
    val density = LocalDensity.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()
    val animationScope = rememberCoroutineScope()

    val containerColor = if (darkTheme)
        LiquidGlassColors.Button.BackgroundDark
    else
        LiquidGlassColors.Button.BackgroundLight

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    val shape = ContinuousRoundedRectangle(BorderRadius.pill)

    Box(
        modifier = modifier
            .height(ComponentSize.LiquidGlassButton.TextButtonHeight)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    glassButtonEffects(
                        density = density,
                        blurRadius = 2.dp,
                        lensRefractionHeight = 8.dp,
                        lensRefractionAmount = 16.dp
                    )
                },
                highlight = { Highlight.Default },
                shadow = { Shadow(radius = 8f.dp, color = Color.Black.copy(alpha = 0.05f)) },
                layerBlock = {
                    val progress = interactiveHighlight.pressProgress
                    val scale = lerp(1f, 1f + 2f.dp.toPx() / size.height, progress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    drawRect(containerColor)
                    if (selected) {
                        drawRect(selectedColor, blendMode = BlendMode.Hue)
                        drawRect(selectedColor.copy(alpha = 0.15f))
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier)
            .padding(horizontal = 12f.dp),
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
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmText: String = "确认",
    dismissText: String = "取消"
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val backdrop = LocalBackdrop.current ?: emptyBackdrop()

    val contentColor = if (darkTheme) Color.White else Color.Black
    val containerColor =
        if (darkTheme) Color(0xFF121212).copy(0.4f)
        else Color(0xFFFAFAFA).copy(0.6f)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .padding(horizontal = 40f.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(48f.dp) },
                    effects = {
                        glassCardEffects(darkTheme, density)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    title,
                    Modifier.padding(28f.dp, 24f.dp, 28f.dp, 12f.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )

                Text(
                    text,
                    Modifier
                        .then(
                            if (darkTheme) {
                                Modifier.graphicsLayer(blendMode = BlendMode.Plus)
                            } else {
                                Modifier
                            }
                        )
                        .padding(24f.dp, 12f.dp, 24f.dp, 12f.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(0.68f),
                )

                Row(
                    Modifier
                        .padding(24f.dp, 12f.dp, 24f.dp, 24f.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16f.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .clip(Capsule())
                            .background(containerColor.copy(0.2f))
                            .clickable { onDismissRequest() }
                            .height(48f.dp)
                            .weight(1f)
                            .padding(horizontal = 16f.dp),
                        horizontalArrangement = Arrangement.spacedBy(4f.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dismissText,
                            color = contentColor.copy(0.68f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Row(
                        Modifier
                            .clip(Capsule())
                            .background(if (darkTheme) IOS26Colors.TintBlue else IOSColors.Blue)
                            .clickable { onConfirm() }
                            .height(48f.dp)
                            .weight(1f)
                            .padding(horizontal = 16f.dp),
                        horizontalArrangement = Arrangement.spacedBy(4f.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            confirmText,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()

    val containerColor = if (darkTheme)
        LiquidGlassColors.Button.BackgroundDark
    else
        LiquidGlassColors.Button.BackgroundLight

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    val shape = ContinuousRoundedRectangle(50)

    Box(
        modifier = modifier
            .size(ComponentSize.LiquidGlassButton.IconButtonSize)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    glassButtonEffects(
                        density = density,
                        blurRadius = 2.dp,
                        lensRefractionHeight = 8.dp,
                        lensRefractionAmount = 16.dp
                    )
                },
                highlight = { Highlight.Default },
                shadow = { Shadow(radius = 8f.dp, color = Color.Black.copy(alpha = 0.05f)) },
                layerBlock = {
                    val progress = interactiveHighlight.pressProgress
                    val scale = lerp(1f, 1f + 2f.dp.toPx() / size.height, progress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    drawRect(containerColor)
                }
            )
            .clip(shape)
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier),
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
    buttonSize: Dp = ComponentSize.LiquidGlassButton.IconButtonSize
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val isTinted = style == GlassSymbolButtonStyle.Tinted
    val tintColor = LiquidGlassColors.Button.Tinted.TintColor

    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light
    val textColor = if (darkTheme) nonTinted.TextColorDark else nonTinted.TextColorLight

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    Row(
        modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    glassButtonEffects(density)
                },
                layerBlock = {
                    val width = size.width
                    val height = size.height

                    val progress = interactiveHighlight.pressProgress
                    val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                    val maxOffset = size.minDimension
                    val initialDerivative = 0.05f
                    val offset = interactiveHighlight.offset
                    translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                    translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                    val maxDragScale = 4f.dp.toPx() / size.height
                    val offsetAngle = atan2(offset.y, offset.x)
                    scaleX =
                        scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                    scaleY =
                        scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
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
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                enabled = enabled,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier)
            .size(buttonSize)
            .padding(horizontal = 4f.dp),
        horizontalArrangement = Arrangement.spacedBy(4f.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 19.sp,
                fontWeight = FontWeight(590),
                lineHeight = 22.sp
            ),
            color = if (isTinted) Color.White else textColor
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
    buttonSize: Dp = ComponentSize.LiquidGlassButton.IconButtonSize,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    icon: @Composable () -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val isTinted = style == GlassSymbolButtonStyle.Tinted
    val tintColor = LiquidGlassColors.Button.Tinted.TintColor

    val nonTinted = LiquidGlassColors.Button.NonTinted
    val grayTint = if (darkTheme) nonTinted.GrayTintDark else nonTinted.GrayTintLight
    val fillLayer3 = if (darkTheme) nonTinted.FillLayer3Dark else nonTinted.FillLayer3Light

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    Box(
        modifier = modifier
            .padding(contentPadding)
    ) {
        Box(
            modifier = Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        glassButtonEffects(density)
                    },
                    layerBlock = {
                        val width = size.width
                        val height = size.height

                        val progress = interactiveHighlight.pressProgress
                        val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactiveHighlight.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = 4f.dp.toPx() / size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX =
                            scale +
                                    maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                    (width / height).fastCoerceAtMost(1f)
                        scaleY =
                            scale +
                                    maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                    (height / width).fastCoerceAtMost(1f)
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
                .clickable(
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                    enabled = enabled,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                )
                .then(interactiveHighlight.modifier)
                .then(interactiveHighlight.gestureModifier)
                .size(buttonSize)
                .padding(horizontal = 4f.dp),
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

            CompositionLocalProvider(LocalContentColor provides iconColor) {
                icon()
            }
        }
    }
}
