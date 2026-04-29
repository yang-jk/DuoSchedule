package com.duoschedule.ui.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.ui.theme.*
import kotlin.math.abs

private val WheelItemHeight = 48.dp
private const val WheelVisibleItems = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = getPersonBColor()
) {
    val itemHeight = WheelItemHeight
    val visibleItems = WheelVisibleItems
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.size - 1)
    )

    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                selectedIndex
            } else {
                val viewportCenter = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset
                val centerOffset = viewportCenter / 2

                var closestItem = layoutInfo.visibleItemsInfo.first()
                var closestDistance = abs(centerOffset - (closestItem.offset + closestItem.size / 2))

                for (item in layoutInfo.visibleItemsInfo) {
                    val itemCenter = item.offset + item.size / 2
                    val distance = abs(centerOffset - itemCenter)
                    if (distance < closestDistance) {
                        closestDistance = distance
                        closestItem = item
                    }
                }
                closestItem.index
            }
        }
    }

    val darkTheme = LocalDarkTheme.current
    val selectionBackground = if (darkTheme) {
        Color(0x20FFFFFF)
    } else {
        Color(0x15000000)
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .clip(ContinuousRoundedRectangle(12.dp))
                .background(selectionBackground)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(top = itemHeight, bottom = itemHeight)
        ) {
            items(items.size) { index ->
                val isSelected = index == centerItemIndex

                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) selectedColor else unselectedColor,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "text_color"
                )

                val animatedFontSize by animateIntAsState(
                    targetValue = if (isSelected) 20 else 16,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "font_size"
                )

                val animatedFontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = animatedFontSize.sp,
                        fontWeight = animatedFontWeight,
                        color = animatedColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(centerItemIndex) {
        if (centerItemIndex in items.indices && centerItemIndex != selectedIndex) {
            onSelectedChange(centerItemIndex)
        }
    }

    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex.coerceIn(0, items.size - 1)
        if (centerItemIndex != targetIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }
}

@Composable
fun TimeWheelPicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = getPersonBColor()
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var currentHour by remember(selectedHour) { mutableIntStateOf(selectedHour) }
    var currentMinute by remember(selectedMinute) { mutableIntStateOf(selectedMinute) }

    LaunchedEffect(currentHour, currentMinute) {
        onTimeChange(currentHour, currentMinute)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = hours,
            selectedIndex = currentHour,
            onSelectedChange = { currentHour = it },
            modifier = Modifier.width(72.dp),
            selectedColor = selectedColor
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        WheelPicker(
            items = minutes,
            selectedIndex = currentMinute,
            onSelectedChange = { currentMinute = it },
            modifier = Modifier.width(72.dp),
            selectedColor = selectedColor
        )
    }
}

@Composable
fun TimeRangeWheelPicker(
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

    val startColor = getPersonBColor()
    val endColor = getPersonAColor()

    LaunchedEffect(startHour, startMinute, endHour, endMinute) {
        onTimeRangeChange(startHour, startMinute, endHour, endMinute)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "开始",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimeWheelPicker(
                    selectedHour = startHour,
                    selectedMinute = startMinute,
                    onTimeChange = { h, m -> startHour = h; startMinute = m },
                    selectedColor = startColor
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "结束",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimeWheelPicker(
                    selectedHour = endHour,
                    selectedMinute = endMinute,
                    onTimeChange = { h, m -> endHour = h; endMinute = m },
                    selectedColor = endColor
                )
            }
        }

        val durationMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
        val isValid = durationMinutes > 0

        if (isValid) {
            val durationText = when {
                durationMinutes >= 60 -> {
                    val hours = durationMinutes / 60
                    val mins = durationMinutes % 60
                    if (mins > 0) "${hours}小时${mins}分钟" else "${hours}小时"
                }
                else -> "${durationMinutes}分钟"
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = ContinuousRoundedRectangle(8.dp)
            ) {
                Text(
                    text = "时长: $durationText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
