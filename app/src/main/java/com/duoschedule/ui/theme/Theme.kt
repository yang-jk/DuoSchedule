package com.duoschedule.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.duoschedule.data.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = IOSColors.Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F1FF),
    onPrimaryContainer = Color(0xFF1A5CB0),
    
    secondary = GraysLight.Gray,
    onSecondary = Color.White,
    secondaryContainer = GraysLight.Gray6,
    onSecondaryContainer = GraysLight.Gray2,
    
    tertiary = IOSColors.Orange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),
    
    background = BackgroundsLight.Primary,
    onBackground = LabelsLight.Primary,
    
    surface = BackgroundsLight.Secondary,
    onSurface = LabelsLight.Primary,
    surfaceVariant = BackgroundsLight.Tertiary,
    onSurfaceVariant = LabelsLight.Secondary,
    
    outline = SeparatorsLight.Opaque,
    outlineVariant = SeparatorsLight.NonOpaque,
    
    error = IOSColors.Red,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    
    inverseSurface = GraysDark.Gray6,
    inverseOnSurface = GraysLight.Gray6,
    inversePrimary = IOSColors.Blue.copy(alpha = 0.8f)
)

private val DarkColorScheme = darkColorScheme(
    primary = IOSColors.Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A5CB0),
    onPrimaryContainer = Color(0xFFE8F1FF),
    
    secondary = GraysDark.Gray,
    onSecondary = Color.Black,
    secondaryContainer = GraysDark.Gray5,
    onSecondaryContainer = GraysDark.Gray2,
    
    tertiary = IOSColors.Orange,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A3700),
    onTertiaryContainer = Color(0xFFFFE0B2),
    
    background = BackgroundsDark.Primary,
    onBackground = LabelsDark.Primary,
    
    surface = BackgroundsDark.Secondary,
    onSurface = LabelsDark.Primary,
    surfaceVariant = BackgroundsDark.Tertiary,
    onSurfaceVariant = LabelsDark.Secondary,
    
    outline = SeparatorsDark.Opaque,
    outlineVariant = SeparatorsDark.NonOpaque,
    
    error = Color(0xFFFF453A),
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    inverseSurface = GraysLight.Gray6,
    inverseOnSurface = GraysDark.Gray6,
    inversePrimary = IOSColors.Blue.copy(alpha = 0.8f)
)

@Composable
fun DuoScheduleTheme(
    themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.FOLLOW_SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
