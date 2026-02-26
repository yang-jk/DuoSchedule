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
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )
    
    val slowTween = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    val cardEnterTransition = tween<Float>(
        durationMillis = 250,
        easing = EaseOutCubic
    )
    
    val listItemEnterTransition = tween<Float>(
        durationMillis = 200,
        easing = EaseOutQuad
    )
}
