package com.duoschedule.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

object ContinuousShapes {
    fun roundedRectangle(radius: Dp) = ContinuousRoundedRectangle(radius)
    
    val Capsule = ContinuousCapsule
    
    object iOS26 {
        val small get() = ContinuousRoundedRectangle(BorderRadius.iOS26.small)
        val medium get() = ContinuousRoundedRectangle(BorderRadius.iOS26.medium)
        val large get() = ContinuousRoundedRectangle(BorderRadius.iOS26.large)
        val xlarge get() = ContinuousRoundedRectangle(BorderRadius.iOS26.xlarge)
        val xxlarge get() = ContinuousRoundedRectangle(BorderRadius.iOS26.xxlarge)
        val container get() = ContinuousRoundedRectangle(BorderRadius.iOS26.container)
        val icon get() = ContinuousRoundedRectangle(BorderRadius.iOS26.icon)
        val navBar get() = ContinuousRoundedRectangle(BorderRadius.iOS26.navBar)
        val tabBar get() = ContinuousRoundedRectangle(BorderRadius.iOS26.tabBar)
        val modal get() = ContinuousRoundedRectangle(BorderRadius.iOS26.modal)
        val widget get() = ContinuousRoundedRectangle(BorderRadius.iOS26.widget)
        val continuous get() = ContinuousRoundedRectangle(BorderRadius.iOS26.continuous)
    }
    
    val xs get() = ContinuousRoundedRectangle(BorderRadius.xs)
    val sm get() = ContinuousRoundedRectangle(BorderRadius.sm)
    val md get() = ContinuousRoundedRectangle(BorderRadius.md)
    val lg get() = ContinuousRoundedRectangle(BorderRadius.lg)
    val xl get() = ContinuousRoundedRectangle(BorderRadius.xl)
    val xxl get() = ContinuousRoundedRectangle(BorderRadius.xxl)
    val xxxl get() = ContinuousRoundedRectangle(BorderRadius.xxxl)
    val liquidGlass get() = ContinuousRoundedRectangle(BorderRadius.liquidGlass)
    val navBar get() = ContinuousRoundedRectangle(BorderRadius.navBar)
    val pill get() = ContinuousCapsule
}
