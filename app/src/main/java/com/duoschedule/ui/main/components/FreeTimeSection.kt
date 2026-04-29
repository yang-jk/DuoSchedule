package com.duoschedule.ui.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.model.FreeTimeSlot
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTimeCard(
    freeTimeSlots: List<FreeTimeSlot>,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    var selectedSlot by remember { mutableStateOf<FreeTimeSlot?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allSlotsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDetailSheet by remember { mutableStateOf(false) }
    var showAllSlotsSheet by remember { mutableStateOf(false) }
    
    val selectedColor = getPersonBColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.large))
            .background(
                if (darkTheme) Color.White.copy(alpha = 0.06f)
                else Color.Black.copy(alpha = 0.02f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = freeTimeSlots.isNotEmpty()
            ) {
                showAllSlotsSheet = true
            }
            .padding(Spacing.md)
    ) {
        if (freeTimeSlots.isEmpty()) {
            EmptyFreeTimeContent()
        } else {
            val nearestSlot = freeTimeSlots.first()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(alpha = if (darkTheme) 0.2f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = selectedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "${freeTimeSlots.size} 个空闲时段",
                            style = MaterialTheme.typography.bodyLarge,
                            color = labelsPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "点击查看全部",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelsTertiary
                        )
                    }
                }
                
                FreeTimeMiniChip(
                    slot = nearestSlot,
                    selectedColor = selectedColor,
                    onClick = {
                        selectedSlot = nearestSlot
                        showDetailSheet = true
                    }
                )
            }
        }
    }

    if (showDetailSheet && selectedSlot != null) {
        FreeTimeDetailSheet(
            slot = selectedSlot!!,
            sheetState = detailSheetState,
            onDismiss = {
                showDetailSheet = false
                selectedSlot = null
            },
            backdrop = backdrop
        )
    }

    if (showAllSlotsSheet && freeTimeSlots.isNotEmpty()) {
        AllFreeTimeSlotsSheet(
            slots = freeTimeSlots,
            sheetState = allSlotsSheetState,
            onSlotClick = { slot ->
                showAllSlotsSheet = false
                selectedSlot = slot
                showDetailSheet = true
            },
            onDismiss = {
                showAllSlotsSheet = false
            },
            backdrop = backdrop
        )
    }
}

@Composable
private fun EmptyFreeTimeContent(
    modifier: Modifier = Modifier
) {
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.EventBusy,
            contentDescription = null,
            tint = labelsSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = "今天没有共同空闲时间",
            style = MaterialTheme.typography.bodyMedium,
            color = labelsSecondary
        )
    }
}

@Composable
private fun FreeTimeMiniChip(
    slot: FreeTimeSlot,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Quick, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val darkTheme = LocalDarkTheme.current
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.small))
            .background(selectedColor.copy(alpha = if (darkTheme) 0.15f else 0.1f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = slot.getTimeString(),
            style = MaterialTheme.typography.labelSmall,
            color = selectedColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FreeTimeDetailSheet(
    slot: FreeTimeSlot,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val selectedColor = getPersonBColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val darkTheme = LocalDarkTheme.current

    GlassBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        backdrop = backdrop,
        darkTheme = darkTheme
    ) { bottomSheetBackdrop ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(top = 12.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = if (darkTheme) 0.2f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = slot.getTimeString(),
                style = MaterialTheme.typography.headlineMedium,
                color = labelsPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "共 ${slot.getDurationString()}",
                style = MaterialTheme.typography.bodyLarge,
                color = selectedColor,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.large))
                    .background(
                        if (darkTheme) Color.White.copy(alpha = 0.06f)
                        else Color.Black.copy(alpha = 0.03f)
                    )
                    .padding(Spacing.lg)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "你们这段时间都空闲",
                        style = MaterialTheme.typography.bodyMedium,
                        color = labelsSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "可以安排见面",
                        style = MaterialTheme.typography.bodyMedium,
                        color = selectedColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LiquidGlassButton(
                text = "知道了",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                backdrop = bottomSheetBackdrop,
                style = LiquidGlassButtonStyle.Tinted
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllFreeTimeSlotsSheet(
    slots: List<FreeTimeSlot>,
    sheetState: SheetState,
    onSlotClick: (FreeTimeSlot) -> Unit,
    onDismiss: () -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val selectedColor = getPersonBColor()
    val labelsPrimary = getLabelsVibrantPrimary()
    val darkTheme = LocalDarkTheme.current

    GlassBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        backdrop = backdrop,
        darkTheme = darkTheme
    ) { bottomSheetBackdrop ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(top = 12.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(selectedColor.copy(alpha = if (darkTheme) 0.2f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "今天 ${slots.size} 个空闲时段",
                    style = MaterialTheme.typography.titleLarge,
                    color = labelsPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(slots) { slot ->
                    FreeTimeSlotItem(
                        slot = slot,
                        selectedColor = selectedColor,
                        onClick = { onSlotClick(slot) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LiquidGlassButton(
                text = "关闭",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                backdrop = bottomSheetBackdrop,
                style = LiquidGlassButtonStyle.Tinted
            )
        }
    }
}

@Composable
private fun FreeTimeSlotItem(
    slot: FreeTimeSlot,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Quick, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.medium))
            .background(
                if (darkTheme) Color.White.copy(alpha = 0.06f)
                else Color.Black.copy(alpha = 0.03f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = slot.getTimeString(),
                style = MaterialTheme.typography.bodyLarge,
                color = labelsPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "共 ${slot.getDurationString()}",
                style = MaterialTheme.typography.bodySmall,
                color = labelsSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.small))
                .background(selectedColor.copy(alpha = if (darkTheme) 0.15f else 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "查看",
                style = MaterialTheme.typography.labelMedium,
                color = selectedColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
