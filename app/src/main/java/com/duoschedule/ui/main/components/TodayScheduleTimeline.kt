package com.duoschedule.ui.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CARD_MIN_HEIGHT = 72.dp
private val CARD_SPACING = Spacing.sm
private val TIMELINE_WIDTH = 48.dp

data class TimeSlot(
    val time: LocalTime,
    val coursesA: List<CourseInfo>,
    val coursesB: List<CourseInfo>,
    val isCurrentHour: Boolean
)

data class CourseInfo(
    val course: Course,
    val isPersonA: Boolean,
    val hasEnded: Boolean,
    val isOngoing: Boolean,
    val progress: Float = 0f,
    val periodText: String = ""
)

@Composable
fun ScheduleList(
    personACourses: List<Course>,
    personBCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    periodTimesA: List<String>,
    periodTimesB: List<String>,
    onCourseClick: (Course, PersonType) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val allTimeSlots = remember(personACourses, personBCourses, displayMode, currentHour, currentMinute) {
        calculateTimeSlots(
            personACourses = personACourses,
            personBCourses = personBCourses,
            displayMode = displayMode,
            currentHour = currentHour,
            currentMinute = currentMinute,
            periodTimesA = periodTimesA,
            periodTimesB = periodTimesB
        )
    }

    val timeSlots = remember(allTimeSlots, displayMode) {
        allTimeSlots.filter { slot ->
            val hasCoursesA = slot.coursesA.isNotEmpty()
            val hasCoursesB = slot.coursesB.isNotEmpty()
            when (displayMode) {
                TodayCourseDisplayMode.BOTH -> hasCoursesA || hasCoursesB
                TodayCourseDisplayMode.TA_ONLY -> hasCoursesA
                TodayCourseDisplayMode.SELF_ONLY -> hasCoursesB
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
    ) {
        timeSlots.forEach { slot ->
            if (displayMode == TodayCourseDisplayMode.BOTH) {
                DualColumnTimeSlotRow(
                    timeSlot = slot,
                    onCourseClick = onCourseClick,
                    backdrop = backdrop
                )
            } else {
                SingleColumnTimeSlotRow(
                    timeSlot = slot,
                    displayMode = displayMode,
                    onCourseClick = onCourseClick,
                    backdrop = backdrop
                )
            }
        }
    }
}

@Composable
private fun DualColumnTimeSlotRow(
    timeSlot: TimeSlot,
    onCourseClick: (Course, PersonType) -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeText = timeSlot.time.format(timeFormatter)
    val labelsTertiary = getLabelsVibrantTertiary()
    val darkTheme = LocalDarkTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier.width(TIMELINE_WIDTH),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = labelsTertiary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
            ) {
                timeSlot.coursesB.forEach { courseInfo ->
                    TodayCourseCardItem(
                        courseInfo = courseInfo,
                        personColor = getPersonBColor(),
                        onClick = { onCourseClick(courseInfo.course, PersonType.PERSON_B) },
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = backdrop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
            ) {
                timeSlot.coursesA.forEach { courseInfo ->
                    TodayCourseCardItem(
                        courseInfo = courseInfo,
                        personColor = getPersonAColor(),
                        onClick = { onCourseClick(courseInfo.course, PersonType.PERSON_A) },
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = backdrop
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleColumnTimeSlotRow(
    timeSlot: TimeSlot,
    displayMode: TodayCourseDisplayMode,
    onCourseClick: (Course, PersonType) -> Unit,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val courses = when (displayMode) {
        TodayCourseDisplayMode.TA_ONLY -> timeSlot.coursesA
        TodayCourseDisplayMode.SELF_ONLY -> timeSlot.coursesB
        TodayCourseDisplayMode.BOTH -> emptyList()
    }
    
    val personColor = when (displayMode) {
        TodayCourseDisplayMode.TA_ONLY -> getPersonAColor()
        TodayCourseDisplayMode.SELF_ONLY -> getPersonBColor()
        TodayCourseDisplayMode.BOTH -> Color.Transparent
    }
    
    val personType = when (displayMode) {
        TodayCourseDisplayMode.TA_ONLY -> PersonType.PERSON_A
        TodayCourseDisplayMode.SELF_ONLY -> PersonType.PERSON_B
        TodayCourseDisplayMode.BOTH -> PersonType.PERSON_A
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeText = timeSlot.time.format(timeFormatter)
    val labelsTertiary = getLabelsVibrantTertiary()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier.width(TIMELINE_WIDTH),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = labelsTertiary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
        ) {
            courses.forEach { courseInfo ->
                TodayCourseCardItem(
                    courseInfo = courseInfo,
                    personColor = personColor,
                    onClick = { onCourseClick(courseInfo.course, personType) },
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = backdrop
                )
            }
        }
    }
}

@Composable
private fun TodayCourseCardItem(
    courseInfo: CourseInfo,
    personColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()
    val density = LocalDensity.current
    
    val alpha = if (courseInfo.hasEnded) 0.5f else 1f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = AnimationDuration.Micro, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val shape = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)

    Box(
        modifier = modifier
            .heightIn(min = CARD_MIN_HEIGHT)
            .alpha(alpha)
            .scale(scale)
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
                    drawRect(if (darkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f))
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(personColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = courseInfo.course.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
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
                        Text(
                            text = courseInfo.course.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsTertiary
                        )
                        
                        Text(
                            text = courseInfo.course.getEndTimeString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = labelsTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (courseInfo.periodText.isNotEmpty()) {
                        Text(
                            text = courseInfo.periodText,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelsTertiary
                        )
                    }
                }
                
                if (courseInfo.isOngoing && courseInfo.progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { courseInfo.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(ContinuousRoundedRectangle(1.5.dp)),
                        color = personColor,
                        trackColor = if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
                    .background(personColor, CircleShape)
            )
        }
    }
}

private fun calculateTimeSlots(
    personACourses: List<Course>,
    personBCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    periodTimesA: List<String>,
    periodTimesB: List<String>
): List<TimeSlot> {
    val currentTime = LocalTime.of(currentHour, currentMinute)
    val currentMinutes = currentHour * 60 + currentMinute
    
    val allCourses = mutableListOf<Pair<Course, Boolean>>()
    personACourses.forEach { allCourses.add(it to true) }
    personBCourses.forEach { allCourses.add(it to false) }
    
    val timeMap = mutableMapOf<LocalTime, MutableList<CourseInfo>>()
    
    allCourses.forEach { (course, isPersonA) ->
        val courseStartTime = LocalTime.of(course.startHour, course.startMinute)
        val courseEndTime = LocalTime.of(course.endHour, course.endMinute)
        
        val courseStartMinutes = course.startHour * 60 + course.startMinute
        val courseEndMinutes = course.endHour * 60 + course.endMinute
        
        val hasEnded = currentMinutes > courseEndMinutes
        val isOngoing = currentMinutes in courseStartMinutes until courseEndMinutes
        
        val progress = if (isOngoing) {
            (currentMinutes - courseStartMinutes).toFloat() / (courseEndMinutes - courseStartMinutes)
        } else 0f
        
        val periodTimes = if (isPersonA) periodTimesA else periodTimesB
        val periodText = getPeriodText(course, periodTimes)
        
        val courseInfo = CourseInfo(
            course = course,
            isPersonA = isPersonA,
            hasEnded = hasEnded,
            isOngoing = isOngoing,
            progress = progress,
            periodText = periodText
        )
        
        timeMap.getOrPut(courseStartTime) { mutableListOf() }.add(courseInfo)
    }
    
    return timeMap.keys.sorted().map { time ->
        val courses = timeMap[time] ?: emptyList()
        TimeSlot(
            time = time,
            coursesA = courses.filter { it.isPersonA },
            coursesB = courses.filter { !it.isPersonA },
            isCurrentHour = time.hour == currentHour
        )
    }
}

private fun getPeriodText(course: Course, periodTimes: List<String>): String {
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
