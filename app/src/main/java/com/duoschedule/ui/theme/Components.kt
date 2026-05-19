package com.duoschedule.ui.theme

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.effects.vibrancy

private const val PROGRESSIVE_BLUR_SHADER = """
uniform shader content;
uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;

half4 main(float2 coord) {
    float blurAlpha = smoothstep(size.y, size.y * 0.3, coord.y);
    float tintAlpha = smoothstep(size.y, size.y * 0.3, coord.y);
    return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
}"""

@Composable
fun Separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null
) {
    val darkTheme = LocalDarkTheme.current
    val dividerColor = color ?: if (darkTheme) {
        SeparatorsDark.NonOpaque
    } else {
        SeparatorsLight.NonOpaque
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(dividerColor)
    )
}

@Composable
fun ScrollTopGradientOverlay(
    modifier: Modifier = Modifier,
    gradientHeight: Dp = 48.dp
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(gradientHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.85f),
                        backgroundColor.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
            .pointerInput(Unit) {}
    )
}

@Composable
fun ScrollTopBlurOverlay(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    scrollOffset: Int = 0,
    blurHeight: Dp = 150.dp,
    blurRadius: Dp = 25.dp
) {
    val density = LocalDensity.current
    val darkTheme = LocalDarkTheme.current
    val containerColor = if (darkTheme) Color(0xFF121212).copy(0.3f) else Color(0xFFFAFAFA).copy(0.3f)
    val tintColor = if (darkTheme) Color(0xFF121212) else Color(0xFFFAFAFA)

    val context = LocalContext.current
    val isLowRamDevice = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.isLowRamDevice == true
    }

    val blurModifier = if (!isLowRamDevice) {
        modifier
            .fillMaxWidth()
            .height(blurHeight)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = {
                    vibrancy()
                    blur(with(density) { blurRadius.toPx() })
                    runtimeShaderEffect(
                        "ProgressiveBlurAlphaMask",
                        PROGRESSIVE_BLUR_SHADER,
                        "content"
                    ) {
                        setFloatUniform("size", size.width, size.height)
                        setColorUniform("tint", tintColor)
                        setFloatUniform("tintIntensity", 0.3f)
                    }
                },
                highlight = { null },
                shadow = { null },
                onDrawSurface = {
                    drawRect(containerColor)
                }
            )
    } else {
        Modifier
    }

    AnimatedVisibility(
        visible = scrollOffset > 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = when {
                isLowRamDevice -> {
                    modifier
                        .fillMaxWidth()
                        .height(blurHeight)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    containerColor,
                                    Color.Transparent
                                )
                            )
                        )
                        .pointerInput(Unit) {}
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    blurModifier.pointerInput(Unit) {}
                }
                else -> {
                    blurModifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    containerColor,
                                    Color.Transparent
                                )
                            )
                        )
                        .pointerInput(Unit) {}
                }
            }
        )
    }
}
