package com.duoschedule.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset

enum class AppleSpring(val dampingRatio: Float, val stiffness: Float) {
    Default(dampingRatio = 0.85f, stiffness = 800f),
    Bouncy(dampingRatio = 0.65f, stiffness = 600f),
    Snappy(dampingRatio = 1.0f, stiffness = 900f),
    Gentle(dampingRatio = 0.75f, stiffness = 400f),
    Decelerate(dampingRatio = 1.0f, stiffness = 500f),
    ContainerTransform(dampingRatio = 0.9f, stiffness = 600f)
}

enum class TabDirection { LEFT, RIGHT }

val tabRouteOrder = listOf("home", "schedule_a", "schedule_b", "settings")

fun determineTabDirection(fromRoute: String?, toRoute: String?): TabDirection {
    val fromIndex = tabRouteOrder.indexOf(fromRoute)
    val toIndex = tabRouteOrder.indexOf(toRoute)
    return if (toIndex >= fromIndex) TabDirection.RIGHT else TabDirection.LEFT
}

fun appleSpring(preset: AppleSpring = AppleSpring.Default): SpringSpec<Float> =
    spring(dampingRatio = preset.dampingRatio, stiffness = preset.stiffness)

fun appleSpringInt(preset: AppleSpring = AppleSpring.Default): SpringSpec<Int> =
    spring(dampingRatio = preset.dampingRatio, stiffness = preset.stiffness)

fun <T> appleSpringFor(preset: AppleSpring = AppleSpring.Default, visibilityThreshold: T): SpringSpec<T> =
    spring(dampingRatio = preset.dampingRatio, stiffness = preset.stiffness, visibilityThreshold = visibilityThreshold)

private fun gentleIntOffsetSpring(): SpringSpec<IntOffset> =
    spring(dampingRatio = AppleSpring.Gentle.dampingRatio, stiffness = AppleSpring.Gentle.stiffness)

private const val IOS_ENTER_DURATION = 350
private const val IOS_POP_DURATION = 300

fun iosSlideEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(IOS_ENTER_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn(animationSpec = tween(IOS_ENTER_DURATION, easing = FastOutSlowInEasing))

fun iosSlideExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(IOS_ENTER_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> -(fullWidth * 30 / 100) }
    ) + fadeOut(animationSpec = tween(IOS_ENTER_DURATION, easing = FastOutSlowInEasing), targetAlpha = 0.7f)

fun iosSlidePopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(IOS_POP_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> -(fullWidth * 30 / 100) }
    ) + fadeIn(animationSpec = tween(IOS_POP_DURATION, easing = FastOutSlowInEasing), initialAlpha = 0.7f)

fun iosSlidePopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(IOS_POP_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut(animationSpec = tween(IOS_POP_DURATION, easing = FastOutSlowInEasing))

fun tabSwitchEnter(direction: TabDirection = TabDirection.RIGHT): EnterTransition =
    when (direction) {
        TabDirection.RIGHT -> slideInHorizontally(
            animationSpec = gentleIntOffsetSpring(),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(animationSpec = appleSpring(AppleSpring.Gentle))
        TabDirection.LEFT -> slideInHorizontally(
            animationSpec = gentleIntOffsetSpring(),
            initialOffsetX = { fullWidth -> -(fullWidth * 30 / 100) }
        ) + fadeIn(animationSpec = appleSpring(AppleSpring.Gentle))
    }

fun tabSwitchExit(direction: TabDirection = TabDirection.RIGHT): ExitTransition =
    when (direction) {
        TabDirection.RIGHT -> slideOutHorizontally(
            animationSpec = gentleIntOffsetSpring(),
            targetOffsetX = { fullWidth -> -(fullWidth * 30 / 100) }
        ) + fadeOut(animationSpec = appleSpring(AppleSpring.Gentle))
        TabDirection.LEFT -> slideOutHorizontally(
            animationSpec = gentleIntOffsetSpring(),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(animationSpec = appleSpring(AppleSpring.Gentle))
    }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
