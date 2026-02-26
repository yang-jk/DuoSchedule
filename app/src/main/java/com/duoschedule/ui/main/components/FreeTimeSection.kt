package com.duoschedule.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.model.FreeTimeSlot
import com.duoschedule.ui.theme.FlatCard
import androidx.compose.material3.MaterialTheme
import com.duoschedule.ui.theme.getDialogBackgroundColor
import com.duoschedule.ui.theme.getPersonBColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTimeSection(
    freeTimeSlots: List<FreeTimeSlot>,
    modifier: Modifier = Modifier
) {
    var selectedSlot by remember { mutableStateOf<FreeTimeSlot?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDetailSheet by remember { mutableStateOf(false) }
    
    val selectedColor = getPersonBColor()

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "共同空闲时间",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (freeTimeSlots.isEmpty()) {
            EmptyFreeTimeCard()
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(freeTimeSlots) { slot ->
                    FreeTimeChip(
                        slot = slot,
                        selectedColor = selectedColor,
                        onClick = {
                            selectedSlot = slot
                            showDetailSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showDetailSheet && selectedSlot != null) {
        FreeTimeDetailBottomSheet(
            slot = selectedSlot!!,
            sheetState = sheetState,
            onDismiss = {
                showDetailSheet = false
                selectedSlot = null
            }
        )
    }
}

@Composable
private fun EmptyFreeTimeCard(
    modifier: Modifier = Modifier
) {
    FlatCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "今天没有共同空闲时间",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FreeTimeChip(
    slot: FreeTimeSlot,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "scale"
    )

    FlatCard(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slot.getTimeString(),
                style = MaterialTheme.typography.titleSmall,
                color = selectedColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = slot.getDurationString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FreeTimeDetailBottomSheet(
    slot: FreeTimeSlot,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val selectedColor = getPersonBColor()

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
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slot.getTimeString(),
                style = MaterialTheme.typography.headlineMedium,
                color = selectedColor,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "共 ${slot.getDurationString()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = selectedColor.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "你们这段时间都空闲，可以安排见面~",
                    style = MaterialTheme.typography.bodyMedium,
                    color = selectedColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor
                )
            ) {
                Text("知道了")
            }
        }
    }
}
