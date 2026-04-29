package com.duoschedule.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun <T> SegmentedControl(
    options: List<SegmentOption<T>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

    val backgroundColor = if (transparentBackground) {
        Color.Transparent
    } else if (darkTheme) {
        Color(0x1F767680)
    } else {
        Color(0x1E767680)
    }

    var selectedIndex by remember(selectedOption) {
        mutableIntStateOf(options.indexOfFirst { it.value == selectedOption }.coerceAtLeast(0))
    }

    var itemWidthPx by remember { mutableIntStateOf(0) }
    val itemWidthDp = with(density) { itemWidthPx.toDp() }
    val spacingPx = with(density) { 4.dp.toPx().toInt() }

    val animatedOffset = remember { Animatable(0f) }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, itemWidthPx) {
        if (itemWidthPx > 0) {
            val targetOffset = selectedIndex * (itemWidthPx + spacingPx).toFloat()
            if (!hasInitialized) {
                animatedOffset.snapTo(targetOffset)
                hasInitialized = true
            } else {
                animatedOffset.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(ContinuousRoundedRectangle(100.dp))
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        if (itemWidthPx > 0 && options.isNotEmpty()) {
            val surfaceColor = if (darkTheme) {
                Color(0xFF636366)
            } else {
                Color(0xFFE5E5EA)
            }
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .offset { IntOffset(animatedOffset.value.toInt(), 0) }
                    .width(itemWidthDp)
                    .fillMaxHeight()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousRoundedRectangle(20.dp) },
                        effects = {
                            vibrancy()
                            blur(2.dp.toPx())
                            lens(
                                refractionHeight = 8.dp.toPx(),
                                refractionAmount = 16.dp.toPx(),
                                chromaticAberration = false
                            )
                        },
                        onDrawSurface = {
                            drawRect(surfaceColor.copy(alpha = 0.85f))
                        }
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option.value == selectedOption

                SegmentedControlItem(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = {
                        selectedIndex = index
                        onOptionSelected(option.value)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { itemWidthPx = it.width },
                    transparentBackground = transparentBackground
                )
            }
        }
    }
}

@Composable
private fun SegmentedControlItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false
) {
    val darkTheme = LocalDarkTheme.current

    val textColor = if (darkTheme) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val fontWeight = if (isSelected) FontWeight(590) else FontWeight(510)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(ContinuousRoundedRectangle(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.333.sp,
            lineHeight = 18.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.08).sp,
            maxLines = 1
        )
    }
}

data class SegmentOption<T>(
    val value: T,
    val label: String
)

inline fun <reified T : Enum<T>> enumToSegmentOptions(
    labelProvider: (T) -> String
): List<SegmentOption<T>> {
    return enumValues<T>().map { value ->
        SegmentOption(value, labelProvider(value))
    }
}
