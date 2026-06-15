package com.duoschedule.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.zIndex
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import com.duoschedule.data.model.AppThemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null
) {
    val appThemeMode = LocalAppThemeMode.current
    val dividerColor = if (appThemeMode == AppThemeMode.MIUIX) {
        color ?: MiuixTheme.colorScheme.dividerLine
    } else {
        val darkTheme = LocalDarkTheme.current
        color ?: if (darkTheme) {
            SeparatorsDark.NonOpaque
        } else {
            SeparatorsLight.NonOpaque
        }
    }
    
    if (appThemeMode == AppThemeMode.MIUIX) {
        // MIUIX 风格不需要分割线
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(dividerColor)
    )
}

object ColorBlendToken {
    val Info_Extra_Thin_Light = listOf(
        BlendColorEntry(Color(0x3BB0B0B1), BlurBlendMode.PlusDarker),
    )

    val Info_Extra_Thin_Dark = listOf(
        BlendColorEntry(Color(0x3BB0B0B1), BlurBlendMode.PlusLighter),
    )

    val Info_Thin_Light = listOf(
        BlendColorEntry(Color(0x801E1E1E), BlurBlendMode.PlusLighter),
    )

    val Info_Thin_Dark = listOf(
        BlendColorEntry(Color(0x801E1E1E), BlurBlendMode.PlusDarker),
    )

    val Info_Regular_Light = listOf(
        BlendColorEntry(Color(0xB3141414), BlurBlendMode.PlusLighter),
    )

    val Info_Regular_Dark = listOf(
        BlendColorEntry(Color(0xB3141414), BlurBlendMode.PlusDarker),
    )

    val Info_Thick_Light = listOf(
        BlendColorEntry(Color(0xFF9A9A9A), BlurBlendMode.PlusLighter),
    )

    val Info_Thick_Dark = listOf(
        BlendColorEntry(Color(0xFF9A9A9A), BlurBlendMode.PlusDarker),
    )

    val Info_Colored_Regular = listOf(
        BlendColorEntry(Color(0xFF9C27B0), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x0FFFFFFF), BlurBlendMode.PlusLighter),
    )

    val Colored_Extra_Thin_Light = listOf(
        BlendColorEntry(Color(0x7F040404), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x26F1F1F1), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x1AC8C8C8), BlurBlendMode.SrcOver),
    )

    val Colored_Extra_Thin_Dark = listOf(
        BlendColorEntry(Color(0x6A4A4A4A), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x2E525252), BlurBlendMode.SrcOver),
    )

    val Colored_Thin_Light = listOf(
        BlendColorEntry(Color(0x991C1C1C), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x802B2B2B), BlurBlendMode.SoftLight),
    )

    val Colored_Thin_Dark = listOf(
        BlendColorEntry(Color(0x1A9C9C9C), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x337A7A7A), BlurBlendMode.PlusDarker),
    )

    val Colored_Regular_Light = listOf(
        BlendColorEntry(Color(0x803F3F3F), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x1CE6E6E6), BlurBlendMode.PlusLighter),
    )

    val Colored_Regular_Dark = listOf(
        BlendColorEntry(Color(0x70000000), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x14000000), BlurBlendMode.SrcOver),
    )

    val Colored_Thick_Light = listOf(
        BlendColorEntry(Color(0xE6BDBDBD), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x992B2B2B), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x339C9C9C), BlurBlendMode.SrcOver),
    )

    val Colored_Thick_Dark = listOf(
        BlendColorEntry(Color(0x667A7A7A), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x33747474), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x322B2B2B), BlurBlendMode.SrcOver),
    )

    val Colored_Extra_Thick_Light = listOf(
        BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.PlusLighter),
        BlendColorEntry(Color(0x6BC0C0C0), BlurBlendMode.ColorDodge),
    )

    val Colored_Extra_Thick_Dark = listOf(
        BlendColorEntry(Color(0x667A7A7A), BlurBlendMode.PlusDarker),
        BlendColorEntry(Color(0x619C9C9C), BlurBlendMode.ColorBurn),
    )

    val Pured_Extra_Thin_Light = listOf(
        BlendColorEntry(Color(0x7F040404), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x5EFFFFFF), BlurBlendMode.PlusLighter),
        BlendColorEntry(Color(0x24FF2424), BlurBlendMode.SrcOver),
    )

    val Pured_Extra_Thin_Dark = listOf(
        BlendColorEntry(Color(0xE6E6E6E6), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x999C9C9C), BlurBlendMode.SrcOver),
    )

    val Pured_Thin_Light = listOf(
        BlendColorEntry(Color(0x307A7A7A), BlurBlendMode.PlusLighter),
        BlendColorEntry(Color(0x5EFFFFFF), BlurBlendMode.PlusLighter),
        BlendColorEntry(Color(0x66FF6666), BlurBlendMode.SrcOver),
    )

    val Pured_Thin_Dark = listOf(
        BlendColorEntry(Color(0x969C9C9C), BlurBlendMode.PlusDarker),
        BlendColorEntry(Color(0x66000000), BlurBlendMode.SrcOver),
    )

    val Pured_Regular_Light = listOf(
        BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0xB3FFFFFF), BlurBlendMode.HardLight),
    )

    val Pured_Regular_Dark = listOf(
        BlendColorEntry(Color(0x75000000), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x52000000), BlurBlendMode.SrcOver),
    )

    val Pured_Thick_Light = listOf(
        BlendColorEntry(Color(0x4D000000), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x80000000), BlurBlendMode.SrcOver),
    )

    val Pured_Thick_Dark = listOf(
        BlendColorEntry(Color(0x4C000000), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x80030303), BlurBlendMode.SrcOver),
    )

    val Pured_Extra_Thick_Light = listOf(
        BlendColorEntry(Color(0x66FF6666), BlurBlendMode.PlusLighter),
        BlendColorEntry(Color(0x999C9C9C), BlurBlendMode.SrcOver),
    )

    val Pured_Extra_Thick_Dark = listOf(
        BlendColorEntry(Color(0x999C9C9C), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x54525252), BlurBlendMode.PlusLighter),
    )

    val Overlay_Extra_Thin_Light = listOf(
        BlendColorEntry(Color(0x0F7A7A7A), BlurBlendMode.Luminosity),
    )

    val Overlay_Extra_Thin_Dark = listOf(
        BlendColorEntry(Color(0x757A7A7A), BlurBlendMode.Luminosity),
    )

    val Overlay_Thin_Light = listOf(
        BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x1A9C9C9C), BlurBlendMode.PlusDarker),
    )

    val Overlay_Regular_Light = listOf(
        BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x1A2B2B2B), BlurBlendMode.PlusDarker),
    )

    val Overlay_Thick_Light = listOf(
        BlendColorEntry(Color(0xA8A8A8A8), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0xFF9A9A9A), BlurBlendMode.Overlay),
    )

    val Overlay_Thick_Dark = listOf(
        BlendColorEntry(Color(0x66A8A8A8), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x999C9C9C), BlurBlendMode.PlusDarker),
    )

    val Overlay_Extra_Thick_Light = listOf(
        BlendColorEntry(Color(0x99A8A8A8), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x4C000000), BlurBlendMode.ColorBurn),
    )

    val ExtraHeavy_Light = listOf(
        BlendColorEntry(Color(0x8F040404), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0xA3A3A3A3), BlurBlendMode.SrcOver),
    )

    val ExtraHeavy_Dark = listOf(
        BlendColorEntry(Color(0x757A7A7A), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x88888888), BlurBlendMode.SrcOver),
        BlendColorEntry(Color(0x0B000000), BlurBlendMode.SrcOver),
    )

    val Heavy_Light = listOf(
        BlendColorEntry(Color(0x949C9C9C), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x999C9C9C), BlurBlendMode.SrcOver),
    )

    val Heavy_Dark = listOf(
        BlendColorEntry(Color(0x7F040404), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0xB3B3B3B3), BlurBlendMode.SrcOver),
    )
}

@Composable
fun BlurredBar(
    hazeState: HazeState,
    backdrop: Backdrop? = null,
    contentBackdrop: com.kyant.backdrop.backdrops.LayerBackdrop? = null,
    content: @Composable () -> Unit,
) {
    val appThemeMode = LocalAppThemeMode.current
    val darkTheme = LocalDarkTheme.current
    val topColor = if (darkTheme) {
        Color(0x80121212)
    } else {
        Color(0x80FAFAFA)
    }
    val gradientBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to topColor,
            0.7f to topColor,
            1.0f to Color.Transparent
        )
    )

    if (appThemeMode == AppThemeMode.MIUIX) {
        val blurColors = BlurDefaults.blurColors(saturation = 1.3f)
        Box(
            modifier = Modifier
                .zIndex(1f)
                .then(
                    if (backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(0.dp),
                            blurRadiusX = 80f,
                            blurRadiusY = 25f,
                            noiseCoefficient = 0f,
                            colors = blurColors,
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(gradientBrush)
            )
            content()
        }
        return
    }

    // iOS 模式：使用渐变背景色，通过 contentBackdrop 提供折射源
    CompositionLocalProvider(LocalBackdrop provides contentBackdrop) {
        Box(
            modifier = Modifier
                .zIndex(1f)
                .background(gradientBrush)
        ) {
            content()
        }
    }
}

class AppSnackbarHostState(
    val snackbarHostState: SnackbarHostState
) {
    suspend fun showAppSnackbar(message: String) {
        snackbarHostState.showSnackbar(message)
    }
}

val LocalAppSnackbarHostState = compositionLocalOf<AppSnackbarHostState?> { null }

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState
) {
    val appThemeMode = LocalAppThemeMode.current

    if (appThemeMode == AppThemeMode.MIUIX) {
        SnackbarHost(
            hostState = snackbarHostState
        ) { data ->
            val darkTheme = LocalDarkTheme.current
            val containerColor = MiuixTheme.colorScheme.surfaceContainer
            val contentColor = MiuixTheme.colorScheme.onSurface

            AnimatedVisibility(
                visible = data.visuals.message.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = containerColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.visuals.message,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else {
        SnackbarHost(
            hostState = snackbarHostState
        ) { data ->
            AnimatedVisibility(
                visible = data.visuals.message.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Snackbar(
                    snackbarData = data
                )
            }
        }
    }
}


