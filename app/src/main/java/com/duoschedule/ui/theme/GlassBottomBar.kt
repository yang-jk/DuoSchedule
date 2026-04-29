package com.duoschedule.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule

object GlassBottomBarSpec {
    val Height = 64.dp
    val HorizontalPadding = 16.dp
    val ItemSpacing = 8.dp
    val IconSize = 24.dp

    // iOS 26 颜色系统
    val SelectedColorLight = Color(0xFF007AFF)
    val SelectedColorDark = Color(0xFF0A84FF)
    val UnselectedColorLight = Color(0xFF8E8E93)
    val UnselectedColorDark = Color(0xFF8E8E93)

    // iOS 26 选中状态渐变色
    val SelectedGradientStartLight = Color(0xFF007AFF)
    val SelectedGradientEndLight = Color(0xFF0055D4)
    val SelectedGradientStartDark = Color(0xFF0A84FF)
    val SelectedGradientEndDark = Color(0xFF0060E0)

    val PillCornerRadius = 100.dp

    // iOS 26 动画参数 - 参考todo项目
    const val AnimationDuration = 400
    const val ScaleAnimationDuration = 400
    const val IndicatorSlideDuration = 400

    // iOS 26 视觉效果参数
    val BlurRadius = 12.dp
    val LensRefractionHeight = 14.dp
    val LensRefractionAmount = 20.dp
    val SurfaceColorAlpha = 0.65f
    val SelectedIndicatorAlpha = 0.85f

    // iOS 26 缓动曲线
    val IOSEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val IOSPressEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    // iOS 26 效果参数
    val ShadowElevation = 8.dp
    val ShadowBlur = 24.dp
    val IndicatorScaleFactor = 1.05f
    val PressScaleFactor = 0.85f
    const val IconScaleSelected = 1.1f
    const val IconScaleUnselected = 1.0f
}

@Composable
fun GlassBottomBar(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.(onItemSizeChanged: (Int, Int) -> Unit) -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

    val trackBackdrop = rememberLayerBackdrop()

    var totalWidth by remember { mutableFloatStateOf(0f) }
    var itemWidthPx by remember { mutableIntStateOf(0) }
    var itemHeightPx by remember { mutableIntStateOf(0) }

    val animatedOffset = remember { Animatable(0f) }
    var hasInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, itemWidthPx, totalWidth) {
        if (itemWidthPx > 0 && totalWidth > 0) {
            val itemCount = (totalWidth / itemWidthPx).toInt().coerceAtLeast(1)
            val totalSpacing = (itemCount - 1) * GlassBottomBarSpec.ItemSpacing.value
            val availableWidth = totalWidth - (itemWidthPx * itemCount)
            val startPadding = availableWidth / 2

            val targetOffset = startPadding + (selectedIndex * (itemWidthPx + GlassBottomBarSpec.ItemSpacing.value))

            if (!hasInitialized) {
                animatedOffset.snapTo(targetOffset)
                hasInitialized = true
            } else {
                animatedOffset.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = GlassBottomBarSpec.IndicatorSlideDuration,
                        easing = GlassBottomBarSpec.IOSEasing
                    )
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(GlassBottomBarSpec.Height)
            .padding(horizontal = GlassBottomBarSpec.HorizontalPadding)
            .drawBackdrop(
                backdrop = trackBackdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(GlassBottomBarSpec.BlurRadius.toPx())
                }
            )
    ) {
        val maxWidth = maxWidth

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(GlassBottomBarSpec.Height)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GlassBottomBarSpec.Height)
                    .onSizeChanged {
                        totalWidth = it.width.toFloat()
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content { width, height ->
                    if (itemWidthPx == 0) {
                        itemWidthPx = width
                    }
                    if (itemHeightPx == 0) {
                        itemHeightPx = height
                    }
                }
            }

            if (itemWidthPx > 0 && itemHeightPx > 0 && totalWidth > 0) {
                val tintColor = if (darkTheme) {
                    GlassBottomBarSpec.SelectedColorDark
                } else {
                    GlassBottomBarSpec.SelectedColorLight
                }

                Box(
                    modifier = Modifier
                        .zIndex(0.5f)
                        .offset { IntOffset(animatedOffset.value.toInt(), 0) }
                        .align(Alignment.CenterStart)
                        .drawBackdrop(
                            backdrop = trackBackdrop,
                            shape = { Capsule() },
                            effects = {
                                // 玻璃模糊效果
                                vibrancy()
                                blur(GlassBottomBarSpec.BlurRadius.toPx())
                                lens(
                                    refractionHeight = GlassBottomBarSpec.LensRefractionHeight.toPx(),
                                    refractionAmount = GlassBottomBarSpec.LensRefractionAmount.toPx(),
                                    chromaticAberration = true
                                )
                            },
                            onDrawSurface = {
                                // 选中指示器使用Hue混合模式
                                drawRect(tintColor, blendMode = BlendMode.Hue)
                                // 然后叠加半透明层
                                drawRect(
                                    tintColor.copy(alpha = GlassBottomBarSpec.SelectedIndicatorAlpha)
                                )
                            }
                        )
                        .size(
                            width = with(density) { itemWidthPx.toDp() },
                            height = with(density) { itemHeightPx.toDp() }
                        )
                )
            }
        }
    }
}

@Composable
fun RowScope.GlassBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    onSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val hapticFeedback = LocalHapticFeedback.current

    val selectedColor = if (darkTheme) {
        GlassBottomBarSpec.SelectedColorDark
    } else {
        GlassBottomBarSpec.SelectedColorLight
    }

    val unselectedColor = if (darkTheme) {
        GlassBottomBarSpec.UnselectedColorDark
    } else {
        GlassBottomBarSpec.UnselectedColorLight
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // iOS 26 风格动画：更长的持续时间和更自然的缓动
    val scale by animateFloatAsState(
        targetValue = if (isPressed) GlassBottomBarSpec.PressScaleFactor else 1.0f,
        animationSpec = tween(
            durationMillis = GlassBottomBarSpec.ScaleAnimationDuration,
            easing = GlassBottomBarSpec.IOSPressEasing
        ),
        label = "tabContainerScale"
    )

    // iOS 26 风格：图标缩放动画
    val iconScale by animateFloatAsState(
        targetValue = if (selected) GlassBottomBarSpec.IconScaleSelected else GlassBottomBarSpec.IconScaleUnselected,
        animationSpec = tween(
            durationMillis = GlassBottomBarSpec.AnimationDuration,
            easing = GlassBottomBarSpec.IOSEasing
        ),
        label = "iconScale"
    )

    // iOS 26 风格：文本透明度动画
    val textAlpha by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.6f,
        animationSpec = tween(
            durationMillis = GlassBottomBarSpec.AnimationDuration,
            easing = GlassBottomBarSpec.IOSEasing
        ),
        label = "textAlpha"
    )

    // iOS 26 风格：字体粗细动画
    val fontWeightValue by animateIntAsState(
        targetValue = if (selected) 600 else 500,
        animationSpec = tween(
            durationMillis = GlassBottomBarSpec.AnimationDuration,
            easing = GlassBottomBarSpec.IOSEasing
        ),
        label = "fontWeight"
    )

    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onSizeChanged {
                onSizeChanged(it.width, it.height)
            }
            .clip(RoundedCornerShape(GlassBottomBarSpec.PillCornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) selectedColor else unselectedColor,
                modifier = Modifier
                    .size(GlassBottomBarSpec.IconSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight(weight = fontWeightValue.coerceIn(400, 700)),
                    letterSpacing = (-0.1).sp
                ),
                color = if (selected) selectedColor else unselectedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
