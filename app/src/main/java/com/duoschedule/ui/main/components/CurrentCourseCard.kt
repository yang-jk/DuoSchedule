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
import androidx.compose.foundation.shape.CircleShape
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.model.CurrentCourseState
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun CurrentCourseCard(
    personAState: CurrentCourseState,
    personBState: CurrentCourseState,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val personAColor = getPersonAColor()
    val personBColor = getPersonBColor()
    val density = LocalDensity.current

    val layer1Tint = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Tint
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Tint
    }

    val layer1Alpha = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Alpha
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Alpha
    }

    val layer2Base = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer2_Base
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer2_Base
    }

    val glassEffect = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.GlassEffect
    } else {
        LiquidGlassColors.BottomSheet.Light.GlassEffect
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.large) },
                effects = {
                    vibrancy()
                    blur(with(density) { GlassBottomSheetDefaults.BlurRadius.toPx() })
                    lens(
                        refractionHeight = with(density) { GlassBottomSheetDefaults.LensRefractionHeight.toPx() },
                        refractionAmount = with(density) { GlassBottomSheetDefaults.LensRefractionAmount.toPx() },
                        chromaticAberration = true
                    )
                },
                onDrawSurface = {
                    drawRect(layer1Tint.copy(alpha = layer1Alpha))
                    drawRect(layer2Base, blendMode = BlendMode.ColorDodge)
                    drawRect(glassEffect)
                }
            )
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        PersonCourseColumn(
            state = personBState,
            personColor = personBColor,
            modifier = Modifier.weight(1f),
            backdrop = backdrop
        )
        
        VerticalDivider(
            color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
            thickness = 1.dp,
            modifier = Modifier.height(100.dp)
        )
        
        PersonCourseColumn(
            state = personAState,
            personColor = personAColor,
            modifier = Modifier.weight(1f),
            backdrop = backdrop
        )
    }
}

@Composable
private fun PersonCourseColumn(
    state: CurrentCourseState,
    personColor: Color,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val labelsPrimary = getLabelsVibrantPrimary()
    val labelsSecondary = getLabelsVibrantSecondary()
    val labelsTertiary = getLabelsVibrantTertiary()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
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
                text = state.personName,
                style = MaterialTheme.typography.labelMedium,
                color = labelsSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.hasCourse) {
            AnimatedContent(
                targetState = state.displayText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(AnimationDuration.Standard, easing = FastOutSlowInEasing)) +
                     slideInVertically(animationSpec = tween(AnimationDuration.Standard, easing = FastOutSlowInEasing)) { it / 4 }) togetherWith
                    (fadeOut(animationSpec = tween(AnimationDuration.Quick, easing = FastOutSlowInEasing)) +
                     slideOutVertically(animationSpec = tween(AnimationDuration.Quick, easing = FastOutSlowInEasing)) { -it / 4 })
                },
                label = "course_name"
            ) { displayText ->
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = labelsPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (state.locationText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsSecondary,
                    maxLines = 1,
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
                        color = labelsTertiary
                    )
                }
                
                if (state.remainingMinutes > 0) {
                    RemainingTimeBadge(
                        remainingMinutes = state.remainingMinutes,
                        personColor = personColor,
                        backdrop = backdrop
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalCafe,
                    contentDescription = "空闲中",
                    tint = labelsSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "空闲中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
                if (state.nextCourseStartTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "下节 ${state.nextCourseStartTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelsTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun RemainingTimeBadge(
    remainingMinutes: Int,
    personColor: Color,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current ?: emptyBackdrop()
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val text = formatRemainingTime(remainingMinutes)

    val layer1Tint = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Tint
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Tint
    }

    val layer1Alpha = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer1_Alpha
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer1_Alpha
    }

    val layer2Base = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.Layer2_Base
    } else {
        LiquidGlassColors.BottomSheet.Light.Layer2_Base
    }

    val glassEffect = if (darkTheme) {
        LiquidGlassColors.BottomSheet.Dark.GlassEffect
    } else {
        LiquidGlassColors.BottomSheet.Light.GlassEffect
    }

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousRoundedRectangle(BorderRadius.iOS26.small) },
                effects = {
                    vibrancy()
                    blur(with(density) { 2.dp.toPx() })
                    lens(
                        refractionHeight = with(density) { 4.dp.toPx() },
                        refractionAmount = with(density) { 8.dp.toPx() },
                        chromaticAberration = false
                    )
                },
                onDrawSurface = {
                    drawRect(personColor, blendMode = BlendMode.Hue)
                    drawRect(personColor.copy(alpha = 0.3f))
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatRemainingTime(remainingMinutes: Int): String {
    return when {
        remainingMinutes >= 60 -> {
            val hours = remainingMinutes / 60
            val minutes = remainingMinutes % 60
            if (minutes > 0) "${hours}小时${minutes}分" else "${hours}小时"
        }
        remainingMinutes >= 5 -> "${remainingMinutes}分"
        remainingMinutes > 0 -> "即将结束"
        else -> ""
    }
}
