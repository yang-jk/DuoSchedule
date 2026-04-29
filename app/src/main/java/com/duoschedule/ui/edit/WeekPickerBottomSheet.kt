package com.duoschedule.ui.edit

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPickerBottomSheet(
    totalWeeks: Int,
    selectedWeeks: Set<Int>,
    onWeeksChange: (Set<Int>) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    var currentSelectedWeeks by remember(selectedWeeks) { mutableStateOf(selectedWeeks) }
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

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
                text = "选择周数",
                style = MaterialTheme.typography.titleLarge,
                color = getLabelsVibrantPrimary()
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
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    darkTheme = darkTheme
                )
                QuickSelectButton(
                    label = "双周",
                    onClick = {
                        currentSelectedWeeks = (1..totalWeeks).filter { it % 2 == 0 }.toSet()
                    },
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    darkTheme = darkTheme
                )
                QuickSelectButton(
                    label = "全部",
                    onClick = {
                        currentSelectedWeeks = (1..totalWeeks).toSet()
                    },
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    darkTheme = darkTheme
                )
                QuickSelectButton(
                    label = "清空",
                    onClick = {
                        currentSelectedWeeks = emptySet()
                    },
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    isDestructive = true,
                    darkTheme = darkTheme
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
                    },
                    backdrop = bottomSheetBackdrop,
                    darkTheme = darkTheme
                )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassButton(
                    text = "取消",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    style = LiquidGlassButtonStyle.NonTinted
                )

                LiquidGlassButton(
                    text = "确定",
                    onClick = {
                        onWeeksChange(currentSelectedWeeks)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    style = LiquidGlassButtonStyle.Tinted
                )
            }
        }
    }
}

@Composable
private fun QuickSelectButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    darkTheme: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "button_scale"
    )
    
    val tintColor = if (isDestructive) {
        SemanticColors.ErrorLight
    } else if (darkTheme) {
        LiquidGlassColors.Button.TintColorDark
    } else {
        LiquidGlassColors.Button.TintColorLight
    }
    
    val backgroundColor = if (darkTheme)
        LiquidGlassColors.Button.BackgroundDark
    else
        LiquidGlassColors.Button.BackgroundLight
    
    val textColor = if (isDestructive) {
        SemanticColors.ErrorLight
    } else if (darkTheme) {
        LiquidGlassColors.Button.TextColorDark
    } else {
        LabelsLight.Primary
    }
    
    val shadowColor = LiquidGlassColors.Button.ShadowColor
    val shape = ContinuousRoundedRectangle(BorderRadius.pill)

    Box(
        modifier = modifier
            .height(ComponentSize.LiquidGlassButton.TextButtonHeight)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 40.dp.toPx()
                this.shape = shape
                clip = false
                ambientShadowColor = Color.Transparent
                spotShadowColor = shadowColor
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(4.dp.toPx())
                    lens(16.dp.toPx(), 32.dp.toPx())
                },
                onDrawSurface = {
                    if (!isDestructive) {
                        drawRect(backgroundColor)
                    }
                    drawRect(Color.White.copy(alpha = 0.65f))
                }
            )
            .border(
                width = 1.dp,
                color = tintColor.copy(alpha = 0.5f),
                shape = shape
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
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

@Composable
private fun WeekGridItem(
    week: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop(),
    darkTheme: Boolean
) {
    val density = LocalDensity.current
    
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        getFillsVibrantTertiary()
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

    val shape = ContinuousRoundedRectangle(BorderRadius.md)

    if (!isSelected) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(4.dp.toPx())
                        lens(8.dp.toPx(), 16.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(backgroundColor.copy(alpha = 0.8f))
                    }
                )
                .border(1.dp, borderColor, shape)
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
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .background(backgroundColor)
                .border(1.dp, borderColor, shape)
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
}
