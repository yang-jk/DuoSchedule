package com.duoschedule.ui.edit

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

private val WheelItemHeight = 52.dp
private const val WheelVisibleItems = 3

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomTimePickerBottomSheet(
    selectedDayOfWeek: Int,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onSelectionChange: (dayOfWeek: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    var currentDayOfWeek by remember(selectedDayOfWeek) { mutableIntStateOf(selectedDayOfWeek) }
    var currentStartHour by remember(startHour) { mutableIntStateOf(startHour) }
    var currentStartMinute by remember(startMinute) { mutableIntStateOf(startMinute) }
    var currentEndHour by remember(endHour) { mutableIntStateOf(endHour) }
    var currentEndMinute by remember(endMinute) { mutableIntStateOf(endMinute) }

    val durationMinutes = remember(currentStartHour, currentStartMinute, currentEndHour, currentEndMinute) {
        val total = (currentEndHour * 60 + currentEndMinute) - (currentStartHour * 60 + currentStartMinute)
        if (total > 0) total else 0
    }

    val isValid = remember(currentStartHour, currentStartMinute, currentEndHour, currentEndMinute) {
        (currentEndHour * 60 + currentEndMinute) > (currentStartHour * 60 + currentStartMinute)
    }

    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()

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
                .padding(top = GlassBottomSheetDefaults.ContentTopPadding, bottom = GlassBottomSheetDefaults.ContentBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "选择自定义时间",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = labelsPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isValid) "${durationMinutes}分钟" else "结束时间必须晚于开始时间",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isValid) labelsSecondary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight * WheelVisibleItems),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayOfWeekWheelPicker(
                    selectedDay = currentDayOfWeek,
                    onDaySelected = { currentDayOfWeek = it },
                    modifier = Modifier.weight(1f)
                )

                TimeWheelPicker(
                    selectedHour = currentStartHour,
                    selectedMinute = currentStartMinute,
                    onTimeChange = { h, m -> currentStartHour = h; currentStartMinute = m },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "-",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelsPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                TimeWheelPicker(
                    selectedHour = currentEndHour,
                    selectedMinute = currentEndMinute,
                    onTimeChange = { h, m -> currentEndHour = h; currentEndMinute = m },
                    modifier = Modifier.weight(1f)
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
                            onSelectionChange(currentDayOfWeek, currentStartHour, currentStartMinute, currentEndHour, currentEndMinute)
                            onDismiss()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayOfWeekWheelPicker(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val selectedColor = IOS26Colors.TintBlue
    val unselectedColor = getLabelsVibrantSecondary()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedDay - 1).coerceIn(0, days.size - 1)
    )

    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                selectedDay - 1
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
            .height(WheelItemHeight * WheelVisibleItems)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(top = WheelItemHeight, bottom = WheelItemHeight)
        ) {
            items(days.size) { index ->
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
                        .height(WheelItemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = days[index],
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
        if (centerItemIndex in days.indices) {
            onDaySelected(centerItemIndex + 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeWheelPicker(
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
        WheelPicker(
            items = hours,
            selectedIndex = currentHour,
            onSelectedChange = { currentHour = it },
            modifier = Modifier.width(48.dp),
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

        WheelPicker(
            items = minutes,
            selectedIndex = currentMinute,
            onSelectedChange = { currentMinute = it },
            modifier = Modifier.width(48.dp),
            selectedColor = selectedColor,
            unselectedColor = unselectedColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color,
    unselectedColor: Color
) {
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
            .height(WheelItemHeight * WheelVisibleItems)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(top = WheelItemHeight, bottom = WheelItemHeight)
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
                        .height(WheelItemHeight),
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
