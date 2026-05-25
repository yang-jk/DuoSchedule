package com.duoschedule.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

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
fun rememberBlurBackdrop(): LayerBackdrop? {
    if (!isRenderEffectSupported()) return null
    val surfaceColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

val LocalHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun rememberHazeState(): HazeState {
    return remember { HazeState() }
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val hazeState = LocalHazeState.current
    val surfaceColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface

    if (blurEnabled && hazeState != null) {
        Box(
            modifier = Modifier
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = surfaceColor.copy(alpha = 0.35f),
                        tint = HazeTint.Unspecified,
                        blurRadius = 20.dp,
                    ),
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f,
                    )
                }
        ) {
            content()
        }
    } else if (blurEnabled && backdrop != null) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 80f,
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surfaceColor.copy(0.35f))
                )
            }
            content()
        }
    } else {
        content()
    }
}
