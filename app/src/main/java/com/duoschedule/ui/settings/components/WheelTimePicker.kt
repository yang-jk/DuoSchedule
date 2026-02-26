package com.duoschedule.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val ItemHeight = 48.dp
private const val VisibleItemsCount = 5
private const val PaddingItemsCount = 2

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, selectedIndex - PaddingItemsCount)
    )
    val coroutineScope = rememberCoroutineScope()
    
    val centerItemIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + PaddingItemsCount
        }
    }
    
    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex - PaddingItemsCount
        if (listState.firstVisibleItemIndex != targetIndex && targetIndex >= 0) {
            coroutineScope.launch {
                listState.scrollToItem(targetIndex)
            }
        }
    }
    
    LaunchedEffect(centerItemIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val actualIndex = centerItemIndex - PaddingItemsCount
            if (actualIndex in items.indices && actualIndex != selectedIndex) {
                onSelectedChange(actualIndex)
            }
        }
    }
    
    Box(
        modifier = modifier.height(ItemHeight * VisibleItemsCount)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(PaddingItemsCount) {
                WheelPickerItem(
                    text = "",
                    isSelected = false
                )
            }
            
            items.forEachIndexed { index, item ->
                item {
                    WheelPickerItem(
                        text = item,
                        isSelected = index == selectedIndex
                    )
                }
            }
            
            items(PaddingItemsCount) {
                WheelPickerItem(
                    text = "",
                    isSelected = false
                )
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(ItemHeight)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(ItemHeight * 2)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(ItemHeight * 2)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )
    }
}

@Composable
private fun WheelPickerItem(
    text: String,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ItemHeight),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = if (isSelected) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    
    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeChange(selectedHour, selectedMinute)
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = hours,
            selectedIndex = selectedHour,
            onSelectedChange = { selectedHour = it },
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        WheelPicker(
            items = minutes,
            selectedIndex = selectedMinute,
            onSelectedChange = { selectedMinute = it },
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
fun WheelTimeRangePicker(
    initialStartHour: Int,
    initialStartMinute: Int,
    initialEndHour: Int,
    initialEndMinute: Int,
    onTimeRangeChange: (startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var startMinute by remember { mutableIntStateOf(initialStartMinute) }
    var endHour by remember { mutableIntStateOf(initialEndHour) }
    var endMinute by remember { mutableIntStateOf(initialEndMinute) }
    
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    
    LaunchedEffect(startHour, startMinute, endHour, endMinute) {
        onTimeRangeChange(startHour, startMinute, endHour, endMinute)
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "开始时间",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(
                items = hours,
                selectedIndex = startHour,
                onSelectedChange = { startHour = it },
                modifier = Modifier.width(80.dp)
            )
            
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            WheelPicker(
                items = minutes,
                selectedIndex = startMinute,
                onSelectedChange = { startMinute = it },
                modifier = Modifier.width(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "结束时间",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(
                items = hours,
                selectedIndex = endHour,
                onSelectedChange = { endHour = it },
                modifier = Modifier.width(80.dp)
            )
            
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            WheelPicker(
                items = minutes,
                selectedIndex = endMinute,
                onSelectedChange = { endMinute = it },
                modifier = Modifier.width(80.dp)
            )
        }
    }
}
