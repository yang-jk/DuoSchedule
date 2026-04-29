package com.duoschedule.ui.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimeRangeBottomSheet(
    periodIndex: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val parts = initialValue.split("-")
    val startTime = parts.getOrElse(0) { "08:00" }
    val endTime = parts.getOrElse(1) { "08:45" }
    
    val startParts = startTime.split(":")
    val endParts = endTime.split(":")
    
    var startHour by remember { mutableIntStateOf(startParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var startMinute by remember { mutableIntStateOf(startParts.getOrElse(1) { "00" }.toIntOrNull() ?: 0) }
    var endHour by remember { mutableIntStateOf(endParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8) }
    var endMinute by remember { mutableIntStateOf(endParts.getOrElse(1) { "45" }.toIntOrNull() ?: 45) }
    
    val durationMinutes = remember(startHour, startMinute, endHour, endMinute) {
        val total = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
        if (total > 0) total else 0
    }
    
    val timeRange = remember(startHour, startMinute, endHour, endMinute) {
        val start = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
        val end = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"
        "$start-$end"
    }
    
    val isValid = remember(startHour, startMinute, endHour, endMinute) {
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute
        endTotal > startTotal
    }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

    GlassBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        backdrop = backdrop,
        darkTheme = darkTheme,
        enableDismissOnSwipe = false
    ) { bottomSheetBackdrop ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlassBottomSheetDefaults.ContentHorizontalPadding)
                .padding(top = GlassBottomSheetDefaults.ContentTopPadding, bottom = GlassBottomSheetDefaults.ContentBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "请调节第${periodIndex + 1}节课的时间",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "本节${durationMinutes}分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = labelsSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeWheelPickerNew(
                    selectedHour = startHour,
                    selectedMinute = startMinute,
                    onTimeChange = { h, m -> startHour = h; startMinute = m },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "-",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelsPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                TimeWheelPickerNew(
                    selectedHour = endHour,
                    selectedMinute = endMinute,
                    onTimeChange = { h, m -> endHour = h; endMinute = m },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (!isValid) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "结束时间必须晚于开始时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
                        if (isValid) {
                            onConfirm(timeRange)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    backdrop = bottomSheetBackdrop,
                    style = LiquidGlassButtonStyle.Tinted,
                    enabled = isValid
                )
            }
        }
    }
}

private val WheelItemHeight = 52.dp
private const val WheelVisibleItems = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeWheelPickerNew(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var currentHour by remember(selectedHour) { mutableIntStateOf(selectedHour) }
    var currentMinute by remember(selectedMinute) { mutableIntStateOf(selectedMinute) }

    LaunchedEffect(currentHour, currentMinute) {
        onTimeChange(currentHour, currentMinute)
    }
    
    val selectedColor = IOS26Colors.TintBlue
    val unselectedColor = getLabelsVibrantSecondary()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPickerNew(
            items = hours,
            selectedIndex = currentHour,
            onSelectedChange = { currentHour = it },
            modifier = Modifier.width(64.dp),
            selectedColor = selectedColor,
            unselectedColor = unselectedColor
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = getLabelsVibrantPrimary(),
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        WheelPickerNew(
            items = minutes,
            selectedIndex = currentMinute,
            onSelectedChange = { currentMinute = it },
            modifier = Modifier.width(64.dp),
            selectedColor = selectedColor,
            unselectedColor = unselectedColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPickerNew(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color,
    unselectedColor: Color
) {
    val itemHeight = WheelItemHeight
    val visibleItems = WheelVisibleItems

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

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
    ) {
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
