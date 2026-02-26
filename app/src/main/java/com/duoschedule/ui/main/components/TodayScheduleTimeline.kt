package com.duoschedule.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import com.duoschedule.data.model.TodayCourseDisplayMode
import com.duoschedule.ui.theme.CourseEndedColor
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CARD_HEIGHT = 84.dp
private val CARD_SPACING = 8.dp
private val CARD_CORNER_RADIUS = 14.dp

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
fun TodayScheduleTimeline(
    personACourses: List<Course>,
    personBCourses: List<Course>,
    displayMode: TodayCourseDisplayMode,
    currentHour: Int,
    currentMinute: Int,
    periodTimesA: List<String>,
    periodTimesB: List<String>,
    onCourseClick: (Course, PersonType) -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 450.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
    ) {
        timeSlots.forEach { slot ->
            if (displayMode == TodayCourseDisplayMode.BOTH) {
                DualColumnTimeSlotRow(
                    timeSlot = slot,
                    onCourseClick = onCourseClick
                )
            } else {
                SingleColumnTimeSlotRow(
                    timeSlot = slot,
                    displayMode = displayMode,
                    onCourseClick = onCourseClick
                )
            }
        }
    }
}

@Composable
private fun DualColumnTimeSlotRow(
    timeSlot: TimeSlot,
    onCourseClick: (Course, PersonType) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val hasCoursesA = timeSlot.coursesA.isNotEmpty()
    val hasCoursesB = timeSlot.coursesB.isNotEmpty()

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TimeScaleColumn(
            time = timeSlot.time.format(timeFormatter),
            isCurrentHour = timeSlot.isCurrentHour
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
            ) {
                timeSlot.coursesB.forEach { courseInfo ->
                    UnifiedCourseCard(
                        courseInfo = courseInfo,
                        onClick = {
                            onCourseClick(courseInfo.course, PersonType.PERSON_B)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
            ) {
                timeSlot.coursesA.forEach { courseInfo ->
                    UnifiedCourseCard(
                        courseInfo = courseInfo,
                        onClick = {
                            onCourseClick(courseInfo.course, PersonType.PERSON_A)
                        }
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
    onCourseClick: (Course, PersonType) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val courses = if (displayMode == TodayCourseDisplayMode.TA_ONLY) {
        timeSlot.coursesA
    } else {
        timeSlot.coursesB
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TimeScaleColumn(
            time = timeSlot.time.format(timeFormatter),
            isCurrentHour = timeSlot.isCurrentHour
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
        ) {
            courses.forEach { courseInfo ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + slideInHorizontally(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        initialOffsetX = { it / 4 }
                    )
                ) {
                    UnifiedCourseCard(
                        courseInfo = courseInfo,
                        onClick = {
                            val personType = if (courseInfo.isPersonA) PersonType.PERSON_A else PersonType.PERSON_B
                            onCourseClick(courseInfo.course, personType)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeScaleColumn(
    time: String,
    isCurrentHour: Boolean
) {
    Column(
        modifier = Modifier.width(48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isCurrentHour) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
        if (isCurrentHour) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(24.dp)
                    .height(2.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun UnifiedCourseCard(
    courseInfo: CourseInfo,
    onClick: () -> Unit
) {
    val personColor = if (courseInfo.isPersonA) getPersonAColor() else getPersonBColor()
    val effectiveColor = if (courseInfo.hasEnded) CourseEndedColor else personColor
    val blockAlpha = if (courseInfo.hasEnded) 0.5f else 1f
    val isOngoing = courseInfo.isOngoing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .alpha(blockAlpha)
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .background(
                if (isOngoing) {
                    effectiveColor.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            )
            .then(
                if (isOngoing) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = effectiveColor,
                            style = Stroke(width = 2.dp.toPx()),
                            cornerRadius = CornerRadius(CARD_CORNER_RADIUS.toPx())
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = CARD_CORNER_RADIUS, bottomStart = CARD_CORNER_RADIUS))
                    .background(effectiveColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = courseInfo.course.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isOngoing) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .background(
                                    effectiveColor.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "进行中",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = effectiveColor
                            )
                        }
                    }
                }

                Text(
                    text = courseInfo.course.location.ifEmpty { "未设置地点" },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = courseInfo.course.getTimeString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = effectiveColor,
                        maxLines = 1
                    )
                    
                    if (courseInfo.periodText.isNotEmpty()) {
                        Text(
                            text = courseInfo.periodText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeTimeSlotCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Coffee,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "空闲",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
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
    val slots = mutableListOf<TimeSlot>()
    val currentMinutes = currentHour * 60 + currentMinute

    val startHour = 6
    val endHour = 22

    for (hour in startHour..endHour) {
        val time = LocalTime.of(hour, 0)
        val isCurrentHour = hour == currentHour

        val coursesA = personACourses
            .filter { course ->
                val courseStartHour = course.startHour
                courseStartHour == hour
            }
            .map { course ->
                createCourseInfo(course, true, currentHour, currentMinute, currentMinutes, periodTimesA)
            }
            .distinctBy { it.course.id }

        val coursesB = personBCourses
            .filter { course ->
                val courseStartHour = course.startHour
                courseStartHour == hour
            }
            .map { course ->
                createCourseInfo(course, false, currentHour, currentMinute, currentMinutes, periodTimesB)
            }
            .distinctBy { it.course.id }

        slots.add(TimeSlot(
            time = time,
            coursesA = coursesA,
            coursesB = coursesB,
            isCurrentHour = isCurrentHour
        ))
    }

    return slots
}

private fun createCourseInfo(
    course: Course,
    isPersonA: Boolean,
    currentHour: Int,
    currentMinute: Int,
    currentMinutes: Int,
    periodTimes: List<String>
): CourseInfo {
    val hasEnded = course.hasEnded(currentHour, currentMinute)
    val isOngoing = course.isOngoing(currentHour, currentMinute, 1) && !hasEnded

    val progress = if (isOngoing) {
        val courseStartMinutes = course.startHour * 60 + course.startMinute
        val courseEndMinutes = course.endHour * 60 + course.endMinute
        val courseDuration = courseEndMinutes - courseStartMinutes
        val elapsed = currentMinutes - courseStartMinutes
        (elapsed.toFloat() / courseDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val periodText = getPeriodText(course, periodTimes)

    return CourseInfo(
        course = course,
        isPersonA = isPersonA,
        hasEnded = hasEnded,
        isOngoing = isOngoing,
        progress = progress,
        periodText = periodText
    )
}

private fun getPeriodText(course: Course, periodTimes: List<String>): String {
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
