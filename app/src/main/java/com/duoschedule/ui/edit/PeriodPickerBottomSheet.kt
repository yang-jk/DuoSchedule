package com.duoschedule.ui.edit

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PeriodPickerBottomSheet(
    totalPeriods: Int,
    selectedDayOfWeek: Int,
    selectedStartPeriod: Int,
    selectedEndPeriod: Int,
    onSelectionChange: (dayOfWeek: Int, startPeriod: Int, endPeriod: Int) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    var currentDayOfWeek by remember(selectedDayOfWeek) { mutableIntStateOf(selectedDayOfWeek) }
    var currentStartPeriod by remember(selectedStartPeriod) { mutableIntStateOf(selectedStartPeriod) }
    var currentEndPeriod by remember(selectedEndPeriod) { mutableIntStateOf(selectedEndPeriod) }

    LaunchedEffect(currentStartPeriod) {
        if (currentEndPeriod < currentStartPeriod) {
            currentEndPeriod = currentStartPeriod
        }
    }

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
                .padding(horizontal = GlassBottomSheetDefaults.ContentHorizontalPadding)
                .padding(top = GlassBottomSheetDefaults.ContentTopPadding, bottom = GlassBottomSheetDefaults.ContentBottomPadding)
        ) {
            Text(
                text = "选择上课时间",
                style = MaterialTheme.typography.titleLarge,
                color = getLabelsVibrantPrimary()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayOfWeekPicker(
                    selectedDay = currentDayOfWeek,
                    onDaySelected = { currentDayOfWeek = it },
                    modifier = Modifier.weight(1f)
                )

                PeriodPicker(
                    selectedPeriod = currentStartPeriod,
                    totalPeriods = totalPeriods,
                    onPeriodSelected = { currentStartPeriod = it },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PeriodPicker(
                    selectedPeriod = currentEndPeriod,
                    totalPeriods = totalPeriods,
                    minPeriod = currentStartPeriod,
                    onPeriodSelected = { currentEndPeriod = it },
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
                        onSelectionChange(currentDayOfWeek, currentStartPeriod, currentEndPeriod)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayOfWeekPicker(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val itemHeight = 52.dp
    val visibleItems = 3
    val selectedColor = getPersonBColor()
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

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
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(top = itemHeight, bottom = itemHeight)
        ) {
            items(days.size) { index ->
                val isSelected = index == centerItemIndex
                
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) selectedColor else unselectedColor,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "text_color"
                )
                
                val animatedFontSize by animateIntAsState(
                    targetValue = if (isSelected) 18 else 14,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "font_size"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = days[index],
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = animatedFontSize.sp,
                        color = animatedColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(centerItemIndex) {
        onDaySelected(centerItemIndex + 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeriodPicker(
    selectedPeriod: Int,
    totalPeriods: Int,
    minPeriod: Int = 1,
    onPeriodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 52.dp
    val visibleItems = 3
    val selectedColor = getPersonBColor()
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedPeriod - 1).coerceIn(0, totalPeriods - 1)
    )

    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                selectedPeriod - 1
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
            .fillMaxWidth()
            .height(itemHeight * visibleItems)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(top = itemHeight, bottom = itemHeight)
        ) {
            items(totalPeriods) { index ->
                val period = index + 1
                val isEnabled = period >= minPeriod
                val isSelected = index == centerItemIndex && isEnabled
                
                val animatedColor by animateColorAsState(
                    targetValue = when {
                        !isEnabled -> unselectedColor.copy(alpha = 0.3f)
                        isSelected -> selectedColor
                        else -> unselectedColor
                    },
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "text_color"
                )
                
                val animatedFontSize by animateIntAsState(
                    targetValue = if (isSelected) 18 else 14,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "font_size"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "第${period}节",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = animatedFontSize.sp,
                        color = animatedColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(centerItemIndex) {
        val newPeriod = centerItemIndex + 1
        if (newPeriod >= minPeriod) {
            onPeriodSelected(newPeriod)
        }
    }
}
