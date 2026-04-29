package com.duoschedule.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import com.kyant.capsule.ContinuousRoundedRectangle
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.theme.BackgroundsDark
import com.duoschedule.ui.theme.BackgroundsLight
import com.duoschedule.ui.theme.BorderRadius
import com.duoschedule.ui.theme.Elevation
import com.duoschedule.ui.theme.Spacing

object GlassmorphismColors {
    val LightCardBackground = Color(0xFFFFFFFF)
    val LightCardAlpha = 0.75f
    val LightBorderColor = Color(0xFFFFFFFF)
    val LightBorderAlpha = 0.5f
    val LightShadowColor = Color(0xFF000000)
    val LightShadowAlpha = 0.08f
    
    val DarkCardBackground = Color(0xFF242424)
    val DarkCardAlpha = 0.65f
    val DarkBorderColor = Color(0xFFFFFFFF)
    val DarkBorderAlpha = 0.1f
    val DarkShadowColor = Color(0xFF000000)
    val DarkShadowAlpha = 0.3f
    
    val LightGradientStart = Color(0xFFFFFFFF)
    val LightGradientEnd = Color(0xFFF0F0F0)
    val DarkGradientStart = Color(0xFF3A3A3A)
    val DarkGradientEnd = Color(0xFF242424)
}

@Composable
fun glassmorphismBackgroundColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) {
        GlassmorphismColors.DarkCardBackground.copy(alpha = GlassmorphismColors.DarkCardAlpha)
    } else {
        GlassmorphismColors.LightCardBackground.copy(alpha = GlassmorphismColors.LightCardAlpha)
    }
}

@Composable
fun glassmorphismBorderColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) {
        GlassmorphismColors.DarkBorderColor.copy(alpha = GlassmorphismColors.DarkBorderAlpha)
    } else {
        GlassmorphismColors.LightBorderColor.copy(alpha = GlassmorphismColors.LightBorderAlpha)
    }
}

@Composable
fun glassmorphismGradientBrush(darkTheme: Boolean = LocalDarkTheme.current): Brush {
    return if (darkTheme) {
        Brush.linearGradient(
            colors = listOf(
                GlassmorphismColors.DarkGradientStart.copy(alpha = 0.3f),
                GlassmorphismColors.DarkGradientEnd.copy(alpha = 0.1f)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                GlassmorphismColors.LightGradientStart.copy(alpha = 0.6f),
                GlassmorphismColors.LightGradientEnd.copy(alpha = 0.3f)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    shape: Shape = ContinuousRoundedRectangle(16.dp),
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val backgroundColor = glassmorphismBackgroundColor()
    val borderColor = glassmorphismBorderColor()
    val gradientBrush = glassmorphismGradientBrush()
    
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (darkTheme) 
                    GlassmorphismColors.DarkShadowColor.copy(alpha = 0.3f)
                else 
                    GlassmorphismColors.LightShadowColor.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 扁平卡片组件 - 用于首页和其他页面的卡片样式
 * 浅色模式：背景 BackgroundsLight.Secondary
 * 深色模式：背景 BackgroundsDark.Secondary
 */
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    shape: Shape = ContinuousRoundedRectangle(BorderRadius.lg),
    elevation: Dp = Elevation.Level1,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val backgroundColor = if (darkTheme) {
        BackgroundsDark.Secondary
    } else {
        BackgroundsLight.Secondary
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        shadowElevation = elevation,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GlassmorphismSurface(
    modifier: Modifier = Modifier,
    shape: Shape = ContinuousRoundedRectangle(8.dp),
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val backgroundColor = if (darkTheme) {
        color.copy(alpha = 0.2f)
    } else {
        color.copy(alpha = 0.15f)
    }
    
    val borderColor = if (darkTheme) {
        color.copy(alpha = 0.3f)
    } else {
        color.copy(alpha = 0.4f)
    }
    
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            content = content
        )
    }
}

@Composable
fun GlassmorphismBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor = glassmorphismBackgroundColor()
    val borderColor = glassmorphismBorderColor()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = ContinuousRoundedRectangle(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = content
        )
    }
}

fun Modifier.glassmorphism(
    shape: Shape = ContinuousRoundedRectangle(16.dp),
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    isDarkTheme: Boolean = false
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = if (isDarkTheme) 
            GlassmorphismColors.DarkShadowColor.copy(alpha = 0.3f)
        else 
            GlassmorphismColors.LightShadowColor.copy(alpha = 0.08f)
    )
    .background(
        if (isDarkTheme) 
            GlassmorphismColors.DarkCardBackground.copy(alpha = GlassmorphismColors.DarkCardAlpha)
        else 
            GlassmorphismColors.LightCardBackground.copy(alpha = GlassmorphismColors.LightCardAlpha), 
        shape
    )
    .border(
        borderWidth, 
        if (isDarkTheme) 
            GlassmorphismColors.DarkBorderColor.copy(alpha = GlassmorphismColors.DarkBorderAlpha)
        else 
            GlassmorphismColors.LightBorderColor.copy(alpha = GlassmorphismColors.LightBorderAlpha), 
        shape
    )

@Composable
fun Modifier.animatedGlassmorphism(
    shape: Shape = ContinuousRoundedRectangle(16.dp),
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    animated: Boolean = true
): Modifier {
    val darkTheme = LocalDarkTheme.current
    val targetBackgroundColor = glassmorphismBackgroundColor()
    val targetBorderColor = glassmorphismBorderColor()
    
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "background_color"
    )
    
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "border_color"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = elevation,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "elevation"
    )
    
    return this
        .shadow(
            elevation = animatedElevation,
            shape = shape,
            spotColor = if (darkTheme) 
                GlassmorphismColors.DarkShadowColor.copy(alpha = 0.3f)
            else 
                GlassmorphismColors.LightShadowColor.copy(alpha = 0.08f)
        )
        .background(animatedBackgroundColor, shape)
        .border(borderWidth, animatedBorderColor, shape)
}

@Composable
fun SmartRecommendationCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val darkTheme = LocalDarkTheme.current
    val baseColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF4CAF50)
    val backgroundColor = if (darkTheme) {
        baseColor.copy(alpha = 0.15f)
    } else {
        baseColor.copy(alpha = 0.12f)
    }
    
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.2f),
            baseColor.copy(alpha = 0.05f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val shape = ContinuousRoundedRectangle(16.dp)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                spotColor = baseColor.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = baseColor.copy(alpha = 0.4f),
                shape = shape
            )
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

fun Modifier.glowEffect(
    color: Color = Color(0xFF4789FE),
    intensity: Float = 0.3f
): Modifier = this.drawBehind {
    val glowRadius = 20.dp.toPx()
    
    drawCircle(
        color = color.copy(alpha = intensity * 0.5f),
        radius = glowRadius,
        center = Offset(size.width / 2, size.height / 2)
    )
}
