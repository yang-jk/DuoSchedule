package com.duoschedule.ui.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.model.FreeTimeSlot
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

private val CARD_MIN_HEIGHT = 72.dp
private val CARD_SPACING = Spacing.sm

data class CourseInfo(
    val course: Course,
    val isPersonA: Boolean,
    val hasEnded: Boolean,
    val isOngoing: Boolean,
    val progress: Float = 0f,
    val periodText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleList(
    personACourses: List<Course>,
    personBCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    periodTimesA: List<String>,
    periodTimesB: List<String>,
    personAName: String,
    personBName: String,
    freeTimeSlots: List<FreeTimeSlot> = emptyList(),
    onCourseClick: (Course, PersonType) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val courseInfos = remember(personACourses, personBCourses, displayMode, currentHour, currentMinute) {
        calculateCourseInfos(
            personACourses = personACourses,
            personBCourses = personBCourses,
            displayMode = displayMode,
            currentHour = currentHour,
            currentMinute = currentMinute,
            periodTimesA = periodTimesA,
            periodTimesB = periodTimesB
        )
    }

    var showAllSlotsSheet by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<FreeTimeSlot?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    val allSlotsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
    ) {
        if (freeTimeSlots.isNotEmpty()) {
            FreeTimeSummaryChip(
                freeTimeSlots = freeTimeSlots,
                onClick = { showAllSlotsSheet = true },
                backdrop = backdrop
            )
        }

        courseInfos.forEach { courseInfo ->
            val personColor = if (courseInfo.isPersonA) getPersonAColor() else getPersonBColor()
            val personName = if (courseInfo.isPersonA) personAName.ifEmpty { "我" } else personBName.ifEmpty { "Ta" }
            TodayCourseCardItem(
                courseInfo = courseInfo,
                personColor = personColor,
                personName = personName,
                onClick = { onCourseClick(courseInfo.course, courseInfo.course.personType) },
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            )
        }
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
}

@Composable
private fun FreeTimeSummaryChip(
    freeTimeSlots: List<FreeTimeSlot>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val selectedColor = getPersonAColor()
    val labelsSecondary = getLabelsVibrantSecondary()
    val nearestSlot = freeTimeSlots.first()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.small) },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(selectedColor.copy(alpha = if (darkTheme) 0.2f else 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🕐",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Text(
            text = "${freeTimeSlots.size}个空闲时段 · 最近 ${nearestSlot.getTimeString()}",
            style = MaterialTheme.typography.bodySmall,
            color = labelsSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TodayCourseCardItem(
    courseInfo: CourseInfo,
    personColor: Color,
    personName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val density = LocalDensity.current

    val textPrimaryColor = if (courseInfo.hasEnded) labelsTertiary else labelsPrimary
    val textSecondaryColor = if (courseInfo.hasEnded) labelsTertiary else labelsSecondary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    Box(
        modifier = modifier
            .heightIn(min = CARD_MIN_HEIGHT)
            .scale(pressScale)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 8.dp.toPx() },
                        refractionAmount = with(density) { 16.dp.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer1_Tint.copy(alpha = 0.5f)
                        else LiquidGlassColors.BottomSheet.Light.Layer1_Tint.copy(alpha = 0.55f)
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.Layer2_Base
                        else LiquidGlassColors.BottomSheet.Light.Layer2_Base,
                        blendMode = BlendMode.ColorDodge
                    )
                    drawRect(
                        if (darkTheme) LiquidGlassColors.BottomSheet.Dark.GlassEffect
                        else LiquidGlassColors.BottomSheet.Light.GlassEffect
                    )
                    if (courseInfo.isOngoing) {
                        drawRect(personColor.copy(alpha = 0.04f))
                    }
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(personColor, CircleShape)
                )
                Text(
                    text = personName,
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondaryColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = courseInfo.course.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textPrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = courseInfo.course.getTimeString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = textSecondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val locationText = courseInfo.course.location
                    val teacherText = courseInfo.course.teacher

                    if (locationText.isNotBlank() && teacherText.isNotBlank()) {
                        Text(
                            text = "$locationText · $teacherText",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (locationText.isNotBlank()) {
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (teacherText.isNotBlank()) {
                        Text(
                            text = teacherText,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (courseInfo.periodText.isNotEmpty()) {
                    Text(
                        text = courseInfo.periodText,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelsTertiary
                    )
                }
            }
        }

        if (courseInfo.isOngoing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(6.dp)
                    .scale(pulseScale)
                    .background(personColor, CircleShape)
            )
        }
    }
}

private fun calculateCourseInfos(
    personACourses: List<Course>,
    personBCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    periodTimesA: List<String>,
    periodTimesB: List<String>
): List<CourseInfo> {
    val currentMinutes = currentHour * 60 + currentMinute

    val allCourses = mutableListOf<Pair<Course, Boolean>>()

    when (displayMode) {
        TodayCourseDisplayMode.SELF_ONLY -> {
            personACourses.forEach { allCourses.add(it to true) }
        }
        TodayCourseDisplayMode.TA_ONLY -> {
            personBCourses.forEach { allCourses.add(it to false) }
        }
        TodayCourseDisplayMode.BOTH -> {
            personACourses.forEach { allCourses.add(it to true) }
            personBCourses.forEach { allCourses.add(it to false) }
        }
    }

    return allCourses.map { (course, isPersonA) ->
        val courseStartMinutes = course.startHour * 60 + course.startMinute
        val courseEndMinutes = course.endHour * 60 + course.endMinute

        val hasEnded = currentMinutes > courseEndMinutes
        val isOngoing = currentMinutes in courseStartMinutes until courseEndMinutes

        val progress = if (isOngoing) {
            (currentMinutes - courseStartMinutes).toFloat() / (courseEndMinutes - courseStartMinutes)
        } else 0f

        val periodTimes = if (isPersonA) periodTimesA else periodTimesB
        val periodText = getPeriodText(course, periodTimes)

        CourseInfo(
            course = course,
            isPersonA = isPersonA,
            hasEnded = hasEnded,
            isOngoing = isOngoing,
            progress = progress,
            periodText = periodText
        )
    }.sortedBy { it.course.startHour * 60 + it.course.startMinute }
}

private fun getPeriodText(course: Course, periodTimes: List<String>): String {
    if (course.isCustomTime) {
        return ""
    }
    if (course.startPeriod > 0 && course.endPeriod > 0) {
        return if (course.startPeriod == course.endPeriod) {
            "第${course.startPeriod}节"
        } else {
            "第${course.startPeriod}-${course.endPeriod}节"
        }
    }

    if (periodTimes.isEmpty()) return ""

    val courseStartMinutes = course.startHour * 60 + course.startMinute
    val courseEndMinutes = course.endHour * 60 + course.endMinute

    var startPeriod = -1
    var endPeriod = -1

    for ((index, periodTime) in periodTimes.withIndex()) {
        val parts = periodTime.split("-")
        if (parts.size == 2) {
            val startParts = parts[0].split(":")
            val endParts = parts[1].split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val periodStartMinutes = (startParts[0].toIntOrNull() ?: 0) * 60 +
                    (startParts[1].toIntOrNull() ?: 0)
                val periodEndMinutes = (endParts[0].toIntOrNull() ?: 0) * 60 +
                    (endParts[1].toIntOrNull() ?: 0)

                if (startPeriod == -1 && courseStartMinutes >= periodStartMinutes && courseStartMinutes < periodEndMinutes) {
                    startPeriod = index + 1
                }
                if (courseEndMinutes > periodStartMinutes && courseEndMinutes <= periodEndMinutes) {
                    endPeriod = index + 1
                }
            }
        }
    }

    return if (startPeriod > 0 && endPeriod > 0) {
        if (startPeriod == endPeriod) {
            "第${startPeriod}节"
        } else {
            "第${startPeriod}-${endPeriod}节"
        }
    } else {
        ""
    }
}
