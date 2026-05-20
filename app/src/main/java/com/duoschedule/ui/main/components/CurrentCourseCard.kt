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
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    singleModeEnabled: Boolean = false,
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
            state = personAState,
            personColor = personAColor,
            modifier = if (singleModeEnabled) Modifier.fillMaxWidth() else Modifier.weight(1f),
            backdrop = backdrop
        )

        if (!singleModeEnabled) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = with(LocalDensity.current) { 100.dp.toPx() }
                        )
                    )
            )

            PersonCourseColumn(
                state = personBState,
                personColor = personBColor,
                modifier = Modifier.weight(1f),
                backdrop = backdrop
            )
        }
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
                style = MaterialTheme.typography.bodyMedium,
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
                    style = MaterialTheme.typography.titleMedium.copy(
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (state.hasNextCourse) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "↗ 下节: ${state.nextCourseDisplayText} · ${state.nextCourseStartTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelsTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            val darkTheme = LocalDarkTheme.current
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(personColor.copy(alpha = if (darkTheme) 0.12f else 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = "空闲中",
                        tint = labelsSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "空闲中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelsSecondary
                )
                if (state.hasNextCourse) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "↗ 下节: ${state.nextCourseDisplayText} · ${state.nextCourseStartTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelsTertiary
                    )
                }
            }
        }
    }
}
