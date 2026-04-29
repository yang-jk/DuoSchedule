package com.duoschedule.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

object IOSColors {
    val Blue = Color(0xFF007AFF)
    val Green = Color(0xFF34C759)
    val Indigo = Color(0xFF5856D6)
    val Orange = Color(0xFFFF9500)
    val Pink = Color(0xFFFF2D55)
    val Purple = Color(0xFFAF52DE)
    val Red = Color(0xFFFF3B30)
    val Teal = Color(0xFF5AC8FA)
    val Yellow = Color(0xFFFFCC00)
    val Mint = Color(0xFF00C7BE)
    val Cyan = Color(0xFF32ADE6)
    val Brown = Color(0xFFA2845E)
    val Gray = Color(0xFF8E8E93)
}

object IOS26Colors {
    val TintBlue = Color(0xFF0A84FF)
    val TintGreen = Color(0xFF30D158)
    val TintIndigo = Color(0xFF5E5CE6)
    val TintOrange = Color(0xFFFF9F0A)
    val TintPink = Color(0xFFFF375F)
    val TintPurple = Color(0xFFBF5AF2)
    val TintRed = Color(0xFFFF453A)
    val TintTeal = Color(0xFF64D2FF)
    val TintYellow = Color(0xFFFFD60A)
    val TintMint = Color(0xFF63E6E2)
    val TintCyan = Color(0xFF70D7FF)
    
    object TabBar {
        val SelectedIconLight = Color(0xFF0088FF)
        val SelectedIconDark = Color(0xFF0091FF)
        val UnselectedIconLight = Color(0xFF1A1A1A)
        val UnselectedIconDark = Color(0xFFF5F5F5)
        val SelectionBackgroundLight = Color(0xFFEDEDED)
        val SelectionBackgroundDark = Color(0xFF121212)
    }
}

object GraysLight {
    val Black = Color(0xFF000000)
    val Gray = Color(0xFF8E8E93)
    val Gray2 = Color(0xFFAEAEB2)
    val Gray3 = Color(0xFFC7C7CC)
    val Gray4 = Color(0xFFD1D1D6)
    val Gray5 = Color(0xFFE5E5EA)
    val Gray6 = Color(0xFFF2F2F7)
    val White = Color(0xFFFFFFFF)
}

object GraysDark {
    val Black = Color(0xFF000000)
    val Gray = Color(0xFF8E8E93)
    val Gray2 = Color(0xFF636366)
    val Gray3 = Color(0xFF48484A)
    val Gray4 = Color(0xFF3A3A3C)
    val Gray5 = Color(0xFF2C2C2E)
    val Gray6 = Color(0xFF1C1C1E)
    val White = Color(0xFFFFFFFF)
}

object BackgroundsLight {
    val Primary = Color(0xFFF2F2F7)
    val Secondary = Color(0xFFFFFFFF)
    val Tertiary = Color(0xFFF2F2F7)
    val PrimaryElevated = Color(0xFFFFFFFF)
    val SecondaryElevated = Color(0xFFFFFFFF)
    val TertiaryElevated = Color(0xFFFFFFFF)
}

object BackgroundsDark {
    val Primary = Color(0xFF000000)
    val Secondary = Color(0xFF1C1C1E)
    val Tertiary = Color(0xFF2C2C2E)
    val PrimaryElevated = Color(0xFF1C1C1E)
    val SecondaryElevated = Color(0xFF2C2C2E)
    val TertiaryElevated = Color(0xFF3A3A3C)
}

object LabelsLight {
    val Primary = Color(0xFF000000)
    val Secondary = Color(0x993C3C43)
    val Tertiary = Color(0x4D3C3C43)
    val Quaternary = Color(0x2E3C3C43)
}

object LabelsDark {
    val Primary = Color(0xFFFFFFFF)
    val Secondary = Color(0xB3EBEBF5)
    val Tertiary = Color(0x4DEBEBF5)
    val Quaternary = Color(0x28EBEBF5)
}

object FillsLight {
    val Primary = Color(0x14787880)
    val Secondary = Color(0x28787880)
    val Tertiary = Color(0x14787880)
    val Quaternary = Color(0x0A787880)
}

object FillsDark {
    val Primary = Color(0x28787880)
    val Secondary = Color(0x38787880)
    val Tertiary = Color(0x28787880)
    val Quaternary = Color(0x1A787880)
}

object SeparatorsLight {
    val Opaque = Color(0xFFC6C6C8)
    val NonOpaque = Color(0x14000000)
}

object SeparatorsDark {
    val Opaque = Color(0xFF38383A)
    val NonOpaque = Color(0x28FFFFFF)
}

object Separators {
    val OpaqueLight = SeparatorsLight.Opaque
    val OpaqueDark = SeparatorsDark.Opaque
    val NonOpaqueLight = SeparatorsLight.NonOpaque
    val NonOpaqueDark = SeparatorsDark.NonOpaque
}

object FillsVibrant {
    val PrimaryLight = Color(0x14787880)
    val PrimaryDark = Color(0x28787880)
    val SecondaryLight = Color(0x28787880)
    val SecondaryDark = Color(0x38787880)
    val TertiaryLight = Color(0x14787880)
    val TertiaryDark = Color(0x28787880)
    val QuaternaryLight = Color(0x0A787880)
    val QuaternaryDark = Color(0x1A787880)
}

object SemanticColors {
    val SuccessLight = Color(0xFF34C759)
    val SuccessDark = Color(0xFF30D158)
    val WarningLight = Color(0xFFFF9500)
    val WarningDark = Color(0xFFFF9F0A)
    val ErrorLight = Color(0xFFFF3B30)
    val ErrorDark = Color(0xFFFF453A)
    val InfoLight = Color(0xFF007AFF)
    val InfoDark = Color(0xFF0A84FF)
}

object BrandColors {
    val Primary = Color(0xFF007AFF)
    val Secondary = Color(0xFFFFB74D)
    val PersonA = Color(0xFFFFB74D)
    val PersonB = Color(0xFF4789FE)
    val PersonALight = Color(0xFFFFB74D)
    val PersonADark = Color(0xFFFFCA28)
    val PersonBLight = Color(0xFF4789FE)
    val PersonBDark = Color(0xFF4789FE)
}

object LiquidGlassColors {
    val GlassBackgroundLight = Color(0xE6FFFFFF)
    val GlassBackgroundDark = Color(0xE61C1C1E)
    val GlassBorderLight = Color(0x33FFFFFF)
    val GlassBorderDark = Color(0x1AFFFFFF)
    val GlassShadowLight = Color(0x14000000)
    val GlassShadowDark = Color(0x33000000)
    val GlassOverlayLight = Color(0x08000000)
    val GlassOverlayDark = Color(0x1AFFFFFF)
    val GlassTintLight = Color(0x0D007AFF)
    val GlassTintDark = Color(0x1A0A84FF)
    
    val FillShadowBackgroundLight = Color(0xFFFFFFFF)
    val FillShadowBackgroundDark = Color(0xFF1C1C1E)
    val GradientOverlayLight = Color(0x99FFFFFF)
    val GradientOverlayDark = Color(0x993A3A3A)
    val GlassEffectBackground = Color(0x01000000)
    val ShadowLight = Color(0x14000000)
    val ShadowDark = Color(0x33000000)
    
    object BottomSheet {
        val CornerRadiusTop = 34.dp
        val CornerRadiusBottom = 0.dp
        val BlurRadius = 40.dp
        val ShadowBlurRadius = 40.dp
        val ShadowOffsetY = 8.dp
        
        object Light {
            val Layer1_Tint = Color(0xFFFFFFFF)
            val Layer1_Alpha = 0.95f
            val Layer2_Base = Color(0xFFE5E5EA)
            val GlassEffect = Color(0x08000000)
            val Shadow = Color(0x1E000000)
        }
        
        object Dark {
            val Layer1_Tint = Color(0xFF1C1C1E)
            val Layer1_Alpha = 0.85f
            val Layer2_Base = Color(0xFF3A3A3C)
            val GlassEffect = Color(0x0DFFFFFF)
            val Shadow = Color(0x33000000)
        }
    }
    
    object iOS26 {
        val MaterialLight = Color(0xE6FFFFFF)
        val MaterialDark = Color(0x123A3A3A)
        val MaterialLightThin = Color(0xCCFFFFFF)
        val MaterialDarkThin = Color(0x0A3A3A3A)
        val MaterialLightThick = Color(0xE6FFFFFF)
        val MaterialDarkThick = Color(0x1A3A3A3A)
        val BorderLight = Color(0x1A000000)
        val BorderDark = Color(0x1AFFFFFF)
        val SeparatorLight = Color(0x14000000)
        val SeparatorDark = Color(0x28FFFFFF)
        val HighlightLight = Color(0x1AFFFFFF)
        val HighlightDark = Color(0x1AFFFFFF)
    }
    
    object Button {
        val TintColorLight = Color(0xFF007AFF)
        val TintColorDark = Color(0xFF0A84FF)
        val BackgroundLight = Color(0xE6FFFFFF)
        val BackgroundDark = Color(0xBF1C1C1E)
        val GlassEffectOverlay = Color(0x01000000)
        val ShadowColor = Color(0x1E000000)
        val TextColorLight = Color(0xFF007AFF)
        val TextColorDark = Color.White
        
        object Tinted {
            val TintColor = Color(0xFF0091FF)
            val BackgroundBase = Color(0xBFFFFFFF)
            val TintLayer1 = Color(0xFF0091FF)
            val TintLayer2 = Color(0xFF999999)
            val TintLayer3 = Color(0xFFFFFFFF)
            val TintLayer4 = Color(0xBFFFFFFF)
            val TintGradientTop = Color(0x800091FF)
            val TintGradientBottom = Color(0x400091FF)
            val ShadowColor = Color(0x1E000000)
            val GlassEffect = Color(0x01000000)
            val TextColor = Color(0xFFFFFFFF)
            val BorderRadius = 1000.dp
            val InnerBorderRadius = 296.dp
        }
        
        object NonTinted {
            val FillLayer1Light = Color(0xFFF7F7F7)
            val FillLayer2Light = Color(0xFFDDDDDD)
            val FillLayer3Light = Color(0xA6FFFFFF)
            val HighlightGradientTopLight = Color(0x66FFFFFF)
            val HighlightGradientBottomLight = Color(0x19FFFFFF)
            val GrayTintLight = Color(0xFF8E8E93)
            
            val FillLayer1Dark = Color(0x0FFFFFFF)
            val FillLayer2Dark = Color(0x99000000)
            val FillLayer3Dark = Color(0x80CCCCCC)
            val HighlightGradientTopDark = Color(0x4DFFFFFF)
            val HighlightGradientBottomDark = Color(0x1AFFFFFF)
            val GrayTintDark = Color(0xFF636366)
            
            val BackgroundBaseLight = Color(0xE6FFFFFF)
            val BackgroundBaseDark = Color(0xE62C2C2E)
            val ShadowColor = Color(0x1E000000)
            val GlassEffectLight = Color(0x01000000)
            val GlassEffectDark = Color(0x33000000)
            val TextColorLight = Color(0xFF1C1C1E)
            val TextColorDark = Color(0xFFEBEBF5)
            val BorderRadius = 1000.dp
            val InnerBorderRadius = 296.dp
        }
    }
}

object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
    
    object iOS26 {
        val compactXS = 2.dp
        val compactSM = 4.dp
        val compactMD = 8.dp
        val compactLG = 12.dp
        val regularSM = 8.dp
        val regularMD = 12.dp
        val regularLG = 16.dp
        val regularXL = 20.dp
        val regularXXL = 24.dp
        val sectionHeader = 20.dp
        val groupSpacing = 35.dp
    }
}

object BorderRadius {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val liquidGlass = 32.dp
    val navBar = 34.dp
    val full = 999.dp
    val pill = 1000.dp
    
    object iOS26 {
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val xlarge = 20.dp
        val xxlarge = 24.dp
        val continuous = 22.dp
        val container = 16.dp
        val navBar = 34.dp
        val tabBar = 34.dp
        val modal = 20.dp
        val widget = 22.dp
        val icon = 10.dp
    }
}

object Elevation {
    val Level0 = 0.dp
    val Level1 = 2.dp
    val Level2 = 4.dp
    val Level3 = 8.dp
    val Level4 = 16.dp
    val Level5 = 24.dp
    val LiquidGlass = 40.dp
}

object AnimationDuration {
    const val Instant = 0
    const val Micro = 100
    const val Quick = 150
    const val Standard = 200
    const val Medium = 300
    const val Slow = 400
    const val Emphasis = 500
    const val Breathing = 2000
}

object ComponentSize {
    val ButtonHeight = 50.dp
    val CompactButtonHeight = 40.dp
    val InputFieldHeight = 56.dp
    val ListItemHeight = 56.dp
    val NavItemHeight = 56.dp
    val NavBarItemHeight = 48.dp
    val DragHandleWidth = 36.dp
    val DragHandleHeight = 5.dp
    val TouchTarget = 44.dp
    val QuickActionButtonHeight = 56.dp
    val PillChipHeight = 36.dp
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp
    
    object LiquidGlassButton {
        val TextButtonWidth = 85.dp
        val TextButtonHeight = 48.dp
        val IconButtonSize = 34.dp
        val TopAppBarIconButtonSize = 32.dp
        val HorizontalPadding = 20.dp
        val VerticalPadding = 6.dp
        val ContentGap = 4.dp
    }
}

val CourseColorPalette = listOf(
    Color(0xFF7EC8E3),
    Color(0xFF8ED4A8),
    Color(0xFFFFB5A7),
    Color(0xFFB5A8D4),
    Color(0xFFFFCF9F),
    Color(0xFFA8D8D8),
    Color(0xFFF8B4D9),
    Color(0xFFC4E0B4),
    Color(0xFFFFB7B2),
    Color(0xFFB4C7E7),
    Color(0xFFFFD9A0),
    Color(0xFFD4A5A5),
    Color(0xFFA5D6D6),
    Color(0xFFE7B5C4),
    Color(0xFFB5D8C4),
    Color(0xFFFFD4A3)
)

fun getCourseColor(courseName: String, darkTheme: Boolean): Color {
    val hash = courseName.hashCode()
    val index = Math.abs(hash) % CourseColorPalette.size
    val baseColor = CourseColorPalette[index]
    return if (darkTheme) {
        Color(
            red = (baseColor.red * 1.15f).coerceIn(0f, 1f),
            green = (baseColor.green * 1.15f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 1.15f).coerceIn(0f, 1f)
        )
    } else {
        baseColor
    }
}

fun getCourseColorByName(courseName: String): Color {
    val hash = courseName.hashCode()
    val index = Math.abs(hash) % CourseColorPalette.size
    return CourseColorPalette[index]
}

val LocalDarkTheme = compositionLocalOf { false }

val LocalBackdrop = compositionLocalOf<LayerBackdrop?> { null }

@Composable
fun rememberLayerBackdropWithBackground(backgroundColor: Color): LayerBackdrop {
    return rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }
}

@Composable
fun getPersonAColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) BrandColors.PersonADark else BrandColors.PersonA
}

@Composable
fun getPersonBColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) BrandColors.PersonBDark else BrandColors.PersonB
}

@Composable
fun getBackgroundColor(elevated: Boolean = false): Color {
    val darkTheme = LocalDarkTheme.current
    return when {
        !darkTheme && !elevated -> BackgroundsLight.Primary
        !darkTheme && elevated -> BackgroundsLight.Secondary
        darkTheme && !elevated -> BackgroundsDark.Primary
        else -> BackgroundsDark.PrimaryElevated
    }
}

@Composable
fun getSurfaceColor(elevated: Boolean = false): Color {
    val darkTheme = LocalDarkTheme.current
    return when {
        !darkTheme && !elevated -> BackgroundsLight.Secondary
        !darkTheme && elevated -> BackgroundsLight.Secondary
        darkTheme && !elevated -> BackgroundsDark.Secondary
        else -> BackgroundsDark.SecondaryElevated
    }
}

@Composable
fun getTextColor(primary: Boolean = true): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) {
        if (primary) LabelsDark.Primary else LabelsDark.Secondary
    } else {
        if (primary) LabelsLight.Primary else LabelsLight.Secondary
    }
}

@Composable
fun getSeparatorColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) SeparatorsDark.NonOpaque else SeparatorsLight.NonOpaque
}

@Composable
fun getDialogBackgroundColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) BackgroundsDark.Secondary else BackgroundsLight.Secondary
}

@Composable
fun getLiquidGlassFillShadow(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LiquidGlassColors.FillShadowBackgroundDark else LiquidGlassColors.FillShadowBackgroundLight
}

@Composable
fun getLiquidGlassGradient(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LiquidGlassColors.GradientOverlayDark else LiquidGlassColors.GradientOverlayLight
}

@Composable
fun getLiquidGlassShadowColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LiquidGlassColors.ShadowDark else LiquidGlassColors.ShadowLight
}

@Composable
fun getLabelsVibrantPrimary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LabelsDark.Primary else LabelsLight.Primary
}

@Composable
fun getLabelsVibrantSecondary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LabelsDark.Secondary else LabelsLight.Secondary
}

@Composable
fun getLabelsVibrantTertiary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LabelsDark.Tertiary else LabelsLight.Tertiary
}

@Composable
fun getFillsVibrantTertiary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) FillsDark.Tertiary else FillsLight.Tertiary
}

@Composable
fun getFillsVibrantQuaternary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) FillsDark.Quaternary else FillsLight.Quaternary
}

@Composable
fun getFillsVibrantPrimary(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) FillsDark.Primary else FillsLight.Primary
}

@Composable
fun getGlassBackgroundColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LiquidGlassColors.GlassBackgroundDark else LiquidGlassColors.GlassBackgroundLight
}

@Composable
fun getGlassBorderColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) LiquidGlassColors.GlassBorderDark else LiquidGlassColors.GlassBorderLight
}

val CourseEndedColor = Color(0xFF9E9E9E)

object ScheduleDimensions {
    val CellMinHeight = 100.dp
    val CellPadding = 2.dp
    val TimeColumnWidth = 44.dp
    val HeaderHeight = 48.dp
    val CourseNameMaxLines = 3
    val LocationMaxLines = 2
    val WeekChipHeight = 32.dp
    val WeekSelectorWidth = 280.dp
}

object ScheduleColors {
    val TimeIndicatorLight = Color(0xFFFF3B30)
    val TimeIndicatorDark = Color(0xFFFF453A)
    val GridSeparatorLight = Color(0x20000000)
    val GridSeparatorDark = Color(0x40FFFFFF)
    val EmptySlotHintLight = Color(0x0A000000)
    val EmptySlotHintDark = Color(0x0DFFFFFF)
    val WeekChipSelectedLight = Color(0xFF007AFF)
    val WeekChipSelectedDark = Color(0xFF0A84FF)
    val WeekChipUnselectedLight = Color(0x14787880)
    val WeekChipUnselectedDark = Color(0x28787880)
}

@Composable
fun getScheduleTimeIndicatorColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) ScheduleColors.TimeIndicatorDark else ScheduleColors.TimeIndicatorLight
}

@Composable
fun getScheduleGridSeparatorColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) ScheduleColors.GridSeparatorDark else ScheduleColors.GridSeparatorLight
}

@Composable
fun getScheduleEmptySlotHintColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) ScheduleColors.EmptySlotHintDark else ScheduleColors.EmptySlotHintLight
}

@Composable
fun getWeekChipSelectedColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) ScheduleColors.WeekChipSelectedDark else ScheduleColors.WeekChipSelectedLight
}

@Composable
fun getWeekChipUnselectedColor(): Color {
    val darkTheme = LocalDarkTheme.current
    return if (darkTheme) ScheduleColors.WeekChipUnselectedDark else ScheduleColors.WeekChipUnselectedLight
}
