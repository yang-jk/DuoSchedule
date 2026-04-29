package com.duoschedule.ui.schedule

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import kotlin.math.roundToInt

data class ContextMenuItem(
    val label: String,
    val isDestructive: Boolean = false,
    val action: () -> Unit
)

data class CellBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    val bottom: Int get() = y + height
    val right: Int get() = x + width
}

@Composable
fun CourseContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    menuItems: List<ContextMenuItem>,
    cellBounds: CellBounds,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    
    val menuHeightPx = ContextMenuDefaults.MenuHeight.value * density
    val menuGapPx = ContextMenuDefaults.Gap.value * density
    
    LaunchedEffect(expanded) {
        if (expanded) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    if (expanded) {
        val menuY = cellBounds.y - menuHeightPx.roundToInt() - menuGapPx.roundToInt()
        
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(
                cellBounds.x,
                menuY
            ),
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                focusable = true
            ),
            onDismissRequest = onDismiss
        ) {
            HorizontalContextMenuContent(
                menuItems = menuItems,
                onDismiss = onDismiss,
                backdrop = backdrop,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HorizontalContextMenuContent(
    menuItems: List<ContextMenuItem>,
    onDismiss: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val density = LocalDensity.current
    
    val cornerRadius = BorderRadius.iOS26.container
    val itemHorizontalPadding = 16.dp
    val itemVerticalPadding = 16.dp
    
    val separatorColor = if (darkTheme) {
        Color(0x26FFFFFF)
    } else {
        Color(0x14000000)
    }
    
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.4f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .wrapContentSize()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(cornerRadius) },
                effects = {
                    colorControls(
                        brightness = if (darkTheme) 0f else 0.2f,
                        saturation = 1.5f
                    )
                    blur(with(density) { 8.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 24.dp.toPx() },
                        refractionAmount = with(density) { 48.dp.toPx() },
                        chromaticAberration = true,
                        depthEffect = true
                    )
                },
                highlight = { Highlight.Plain },
                onDrawSurface = { drawRect(containerColor) }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            menuItems.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(separatorColor)
                    )
                }
                
                HorizontalContextMenuItem(
                    item = item,
                    onClick = {
                        item.action()
                        onDismiss()
                    },
                    horizontalPadding = itemHorizontalPadding,
                    verticalPadding = itemVerticalPadding
                )
            }
        }
    }
}

@Composable
private fun HorizontalContextMenuItem(
    item: ContextMenuItem,
    onClick: () -> Unit,
    horizontalPadding: Dp,
    verticalPadding: Dp
) {
    val darkTheme = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "item_scale"
    )
    
    val textColor = if (item.isDestructive) {
        Color(0xFFFF383C)
    } else {
        getLabelsVibrantPrimary()
    }
    
    val pressedBackgroundColor = if (darkTheme) {
        Color(0x33FFFFFF)
    } else {
        Color(0x14000000)
    }
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.medium))
            .background(if (isPressed) pressedBackgroundColor else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            ),
            color = textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

object ContextMenuDefaults {
    val MenuHeight = 52.dp
    val Gap = 8.dp
    val CornerRadius = 16.dp
    val ItemHorizontalPadding = 16.dp
    val ItemVerticalPadding = 16.dp
    val DestructiveColor = Color(0xFFFF383C)
}
