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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.duoschedule.data.model.ThemeMode

private val PrimaryLight = Color(0xFF4789FE)
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFE8F1FF)
private val OnPrimaryContainerLight = Color(0xFF1A5CB0)

private val SecondaryLight = Color(0xFF757575)
private val OnSecondaryLight = Color(0xFFFFFFFF)
private val SecondaryContainerLight = Color(0xFFF5F5F5)
private val OnSecondaryContainerLight = Color(0xFF424242)

private val BackgroundLight = Color(0xFFF7F7F7)
private val OnBackgroundLight = Color(0xFF000000)
private val SurfaceLight = Color(0xFFFFFFFF)
private val OnSurfaceLight = Color(0xFF000000)
private val SurfaceVariantLight = Color(0xFFF0F0F0)
private val OnSurfaceVariantLight = Color(0xFF666666)

private val OutlineLight = Color(0xFFE0E0E0)
private val OutlineVariantLight = Color(0xFFEEEEEE)

private val ErrorLight = Color(0xFFE53935)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFEBEE)
private val OnErrorContainerLight = Color(0xFFB71C1C)

private val PrimaryDark = Color(0xFF4789FE)
private val OnPrimaryDark = Color(0xFFFFFFFF)
private val PrimaryContainerDark = Color(0xFF1A5CB0)
private val OnPrimaryContainerDark = Color(0xFFE8F1FF)

private val SecondaryDark = Color(0xFFB0B0B0)
private val OnSecondaryDark = Color(0xFF000000)
private val SecondaryContainerDark = Color(0xFF3A3A3A)
private val OnSecondaryContainerDark = Color(0xFFE0E0E0)

private val BackgroundDark = Color(0xFF000000)
private val OnBackgroundDark = Color(0xFFFFFFFF)
private val SurfaceDark = Color(0xFF242424)
private val OnSurfaceDark = Color(0xFFFFFFFF)
private val SurfaceVariantDark = Color(0xFF1A1A1A)
private val OnSurfaceVariantDark = Color(0xFFB0B0B0)

private val OutlineDark = Color(0xFF3A3A3A)
private val OutlineVariantDark = Color(0xFF2A2A2A)

private val ErrorDark = Color(0xFFFF6B6B)
private val OnErrorDark = Color(0xFF000000)
private val ErrorContainerDark = Color(0xFFB71C1C)
private val OnErrorContainerDark = Color(0xFFFFCDD2)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

val PersonAColorLight = Color(0xFFFFB74D)
val PersonBColorLight = Color(0xFF4789FE)
val FreeTimeColorLight = Color(0xFF81C784)

val PersonAColorDark = Color(0xFFFFCA28)
val PersonBColorDark = Color(0xFF4789FE)
val FreeTimeColorDark = Color(0xFFA5D6A7)

val CourseEndedColor = Color(0xFF9E9E9E)

val DialogBackgroundLight = Color(0xFFF7F7F7)
val DialogBackgroundDark = Color(0xFF000000)

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun getDialogBackgroundColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) DialogBackgroundDark else DialogBackgroundLight
}

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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
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

// 获取当前主题下的人员区分色
@Composable
fun getPersonAColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) PersonAColorDark else PersonAColorLight
}

@Composable
fun getPersonBColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) PersonBColorDark else PersonBColorLight
}

@Composable
fun getFreeTimeColor(darkTheme: Boolean = LocalDarkTheme.current): Color {
    return if (darkTheme) FreeTimeColorDark else FreeTimeColorLight
}

private val CourseColorPalette = listOf(
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
    Color(0xFFFFD4A3),
    Color(0xFFC4B5E7),
    Color(0xFFA8C4E0)
)

@Composable
fun getCourseColorByName(courseName: String, darkTheme: Boolean = LocalDarkTheme.current): Color {
    val hash = courseName.hashCode()
    val index = Math.abs(hash) % CourseColorPalette.size
    val baseColor = CourseColorPalette[index]
    return if (darkTheme) {
        Color(
            red = (baseColor.red * 1.2f).coerceIn(0f, 1f),
            green = (baseColor.green * 1.2f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 1.2f).coerceIn(0f, 1f)
        )
    } else {
        baseColor
    }
}
