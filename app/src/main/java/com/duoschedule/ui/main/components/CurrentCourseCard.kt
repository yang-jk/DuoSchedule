package com.duoschedule.ui.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.model.CurrentCourseState
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import com.duoschedule.ui.theme.LocalDarkTheme

@Composable
fun MergedCurrentCourseCard(
    personAState: CurrentCourseState,
    personBState: CurrentCourseState,
    modifier: Modifier = Modifier
) {
    val personAColor = getPersonAColor()
    val personBColor = getPersonBColor()
    val darkTheme = LocalDarkTheme.current
    
    val personABgColor = personAColor.copy(alpha = if (darkTheme) 0.2f else 0.15f)
    val personBBgColor = personBColor.copy(alpha = if (darkTheme) 0.2f else 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(personBBgColor)
            ) {
                PersonCourseSection(
                    state = personBState,
                    personColor = personBColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(personABgColor)
            ) {
                PersonCourseSection(
                    state = personAState,
                    personColor = personAColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PersonCourseSection(
    state: CurrentCourseState,
    personColor: Color,
    modifier: Modifier = Modifier
) {
    if (state.hasCourse) {
        Column(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = state.personName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedContent(
                targetState = state.displayText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                     slideInVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { it / 4 }) togetherWith
                    (fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                     slideOutVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)) { -it / 4 })
                },
                label = "course_name"
            ) { displayText ->
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (state.locationText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.periodText.isNotEmpty()) {
                    Text(
                        text = state.periodText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                if (state.remainingMinutes > 0) {
                    Text(
                        text = formatRemainingTime(state.remainingMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = personColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.personName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalCafe,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "空闲中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (state.nextCourseStartTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "下节 ${state.nextCourseStartTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatRemainingTime(remainingMinutes: Int): String {
    return when {
        remainingMinutes >= 60 -> {
            val hours = remainingMinutes / 60
            val minutes = remainingMinutes % 60
            if (minutes > 0) "还剩 $hours 小时 $minutes 分钟" else "还剩 $hours 小时"
        }
        remainingMinutes >= 5 -> "还剩 $remainingMinutes 分钟"
        remainingMinutes > 0 -> "即将结束"
        else -> ""
    }
}

@Composable
fun DualCurrentCourseSection(
    personAState: CurrentCourseState,
    personBState: CurrentCourseState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "当前课程",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MergedCurrentCourseCard(
            personAState = personAState,
            personBState = personBState
        )
    }
}

@Composable
fun CurrentCourseCard(
    state: CurrentCourseState,
    personColor: Color,
    modifier: Modifier = Modifier,
    showNextCoursePreview: Boolean = false
) {
    val animatedProgress = state.progress

    val darkTheme = LocalDarkTheme.current
    val cardBackgroundColor = if (darkTheme) {
        Color(0xFF242424)
    } else {
        Color(0xFFFFFFFF)
    }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cardBackgroundColor,
            shadowElevation = 2.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                        .background(personColor)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp),
                            spotColor = personColor.copy(alpha = 0.5f)
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = state.personName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (state.hasCourse) {
                        AnimatedContent(
                            targetState = state.displayText,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                 slideInVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { it / 4 }) togetherWith
                                (fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                 slideOutVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)) { -it / 4 })
                            },
                            label = "course_name"
                        ) { displayText ->
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (state.locationText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.locationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.periodText.isNotEmpty()) {
                                Text(
                                    text = state.periodText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            
                            if (state.remainingMinutes > 0) {
                                Text(
                                    text = formatRemainingTime(state.remainingMinutes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = personColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalCafe,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = state.displayText,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (showNextCoursePreview && state.hasNextCourse) {
            Spacer(modifier = Modifier.height(6.dp))
            NextCoursePreview(
                nextCourseName = state.nextCourseDisplayText,
                nextCourseTime = state.nextCourseStartTime,
                nextCourseLocation = state.nextCourseLocationText,
                nextCoursePeriod = state.nextCoursePeriodText,
                personColor = personColor
            )
        }
    }
}

@Composable
private fun NextCoursePreview(
    nextCourseName: String,
    nextCourseTime: String,
    nextCourseLocation: String,
    nextCoursePeriod: String,
    personColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "下节",
                style = MaterialTheme.typography.labelSmall,
                color = personColor,
                fontWeight = FontWeight.Medium
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nextCourseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                if (nextCourseLocation.isNotEmpty()) {
                    Text(
                        text = nextCourseLocation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = nextCourseTime,
                style = MaterialTheme.typography.labelMedium,
                color = personColor,
                fontWeight = FontWeight.Medium
            )
            if (nextCoursePeriod.isNotEmpty()) {
                Text(
                    text = nextCoursePeriod,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SingleCurrentCourseSection(
    state: CurrentCourseState,
    personColor: Color,
    title: String = "当前课程",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CurrentCourseCard(
            state = state,
            personColor = personColor,
            modifier = Modifier.fillMaxWidth(),
            showNextCoursePreview = true
        )
    }
}
