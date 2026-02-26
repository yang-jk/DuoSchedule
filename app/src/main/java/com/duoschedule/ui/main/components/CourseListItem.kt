package com.duoschedule.ui.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoschedule.data.model.Course
import com.duoschedule.ui.theme.CourseEndedColor
import com.duoschedule.ui.theme.FlatCard
import com.duoschedule.ui.theme.getPersonAColor
import com.duoschedule.ui.theme.getPersonBColor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListItem(
    course: Course,
    isPersonA: Boolean,
    hasEnded: Boolean,
    onClick: () -> Unit,
    periodText: String = "",
    modifier: Modifier = Modifier
) {
    val personColor = if (isPersonA) getPersonAColor() else getPersonBColor()
    val effectiveColor = if (hasEnded) CourseEndedColor else personColor
    val itemAlpha = if (hasEnded) 0.5f else 1f

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "scale"
    )

    FlatCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = if (isPressed) 1.dp else 2.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(44.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = course.getStartTimeString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasEnded) CourseEndedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    maxLines = 1
                )
                Text(
                    text = course.getEndTimeString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasEnded) CourseEndedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(effectiveColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (hasEnded) CourseEndedColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (course.location.isNotEmpty()) {
                    Text(
                        text = course.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasEnded) CourseEndedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (periodText.isNotEmpty()) {
                    Text(
                        text = periodText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasEnded) CourseEndedColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
