package com.duoschedule.ui.theme

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlur

private val ColoredRegularLight = listOf(
    BlendColorEntry(Color(0x803F3F3F), BlurBlendMode.Overlay),
    BlendColorEntry(Color(0x1CE6E6E6), BlurBlendMode.PlusLighter),
)

private val ColoredRegularDark = listOf(
    BlendColorEntry(Color(0x70000000), BlurBlendMode.Overlay),
    BlendColorEntry(Color(0x14000000), BlurBlendMode.SrcOver),
)

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
    backdrop: LayerBackdrop,
    scrollOffset: Int = 0,
    blurHeight: Dp = 150.dp,
    blurRadius: Dp = 80.dp
) {
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current

    val context = LocalContext.current
    val isLowRamDevice = remember {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.isLowRamDevice == true
    }

    val canBlur = isRuntimeShaderSupported() && !isLowRamDevice
    val blurRadiusPx = with(density) { blurRadius.toPx() }

    val blendColors = if (darkTheme) ColoredRegularDark else ColoredRegularLight
    val fallbackColor = if (darkTheme) Color(0xFF121212).copy(alpha = 0.6f) else Color(0xFFFAFAFA).copy(alpha = 0.6f)

    AnimatedVisibility(
        visible = scrollOffset > 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (canBlur) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(blurHeight)
                    .textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = blurRadiusPx,
                        colors = BlurColors(
                            blendColors = blendColors,
                            saturation = 1.2f
                        ),
                        contentBlendMode = BlendMode.DstIn,
                    )
                    .pointerInput(Unit) {}
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(blurHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                fallbackColor,
                                Color.Transparent
                            )
                        )
                    )
                    .pointerInput(Unit) {}
            )
        }
    }
}
