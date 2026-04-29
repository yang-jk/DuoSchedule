package com.duoschedule.ui.theme

import androidx.compose.animation.core.*

object AnimationSpecs {
    
    val defaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val defaultTween = tween<Float>(
        durationMillis = AnimationDuration.Standard,
        easing = FastOutSlowInEasing
    )
    
    val slowTween = tween<Float>(
        durationMillis = AnimationDuration.Medium,
        easing = FastOutSlowInEasing
    )
    
    val cardEnterTransition = tween<Float>(
        durationMillis = 250,
        easing = EaseOutCubic
    )
    
    val listItemEnterTransition = tween<Float>(
        durationMillis = AnimationDuration.Standard,
        easing = EaseOutQuad
    )
    
    val liquidGlassEnter = tween<Float>(
        durationMillis = AnimationDuration.Standard,
        easing = FastOutSlowInEasing
    )
    
    val liquidGlassExit = tween<Float>(
        durationMillis = AnimationDuration.Quick,
        easing = FastOutLinearInEasing
    )
    
    val stateChange = tween<Float>(
        durationMillis = AnimationDuration.Standard,
        easing = FastOutSlowInEasing
    )
    
    val microInteraction = tween<Float>(
        durationMillis = AnimationDuration.Micro,
        easing = LinearEasing
    )
    
    val breathingAnimation = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = AnimationDuration.Breathing,
            easing = EaseInOutSine
        ),
        repeatMode = RepeatMode.Reverse
    )
    
    val pageTransition = tween<Float>(
        durationMillis = AnimationDuration.Standard,
        easing = FastOutSlowInEasing
    )
    
    val bottomSheetEnter = tween<Float>(
        durationMillis = AnimationDuration.Medium,
        easing = LinearOutSlowInEasing
    )
    
    val bottomSheetExit = tween<Float>(
        durationMillis = AnimationDuration.Quick,
        easing = FastOutLinearInEasing
    )
    
    val iosPageSlide = tween<Int>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    val iosPageFade = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    val iosBottomNavSlide = tween<Int>(
        durationMillis = 250,
        easing = FastOutSlowInEasing
    )
}
