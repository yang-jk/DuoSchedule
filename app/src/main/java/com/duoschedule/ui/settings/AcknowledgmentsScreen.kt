package com.duoschedule.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import com.duoschedule.ui.theme.GlassSymbolIconButton
import com.duoschedule.ui.theme.GlassSymbolButtonStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

import top.yukonga.miuix.kmp.theme.MiuixTheme

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val url: String
)

val libraries = listOf(
    OpenSourceLibrary("Jetpack Compose", "Google", "https://github.com/android/compose-samples"),
    OpenSourceLibrary("Kotlin", "JetBrains", "https://github.com/JetBrains/kotlin"),
    OpenSourceLibrary("Kotlin Coroutines", "JetBrains", "https://github.com/Kotlin/kotlinx.coroutines"),
    OpenSourceLibrary("Room", "Google", "https://developer.android.com/topic/libraries/architecture/room"),
    OpenSourceLibrary("Hilt", "Google", "https://github.com/google/dagger"),
    OpenSourceLibrary("OkHttp", "Square", "https://github.com/square/okhttp"),
    OpenSourceLibrary("jsoup", "Jonathan Hedley", "https://github.com/jhy/jsoup"),
    OpenSourceLibrary("Miuix", "Yukonga", "https://github.com/miuix-kotlin-multiplatform/miuix"),
    OpenSourceLibrary("AndroidLiquidGlass", "kyant0", "https://github.com/kyant0/AndroidLiquidGlass"),
    OpenSourceLibrary("Backdrop", "kyant0", "https://github.com/kyant0/Backdrop"),
    OpenSourceLibrary("Haze", "Chris Banes", "https://github.com/nickkimk/haze"),
    OpenSourceLibrary("Shapes", "kyant0", "https://github.com/kyant0/Shapes"),
    OpenSourceLibrary("Capsule", "kyant0", "https://github.com/kyant0/Capsule"),
    OpenSourceLibrary("DataStore", "Google", "https://developer.android.com/topic/libraries/architecture/datastore"),
    OpenSourceLibrary("Navigation", "Google", "https://developer.android.com/guide/navigation"),
    OpenSourceLibrary("Glance", "Google", "https://developer.android.com/develop/ui/compose/glance"),
    OpenSourceLibrary("WorkManager", "Google", "https://developer.android.com/topic/libraries/architecture/workmanager"),
    OpenSourceLibrary("SplashScreen", "Google", "https://developer.android.com/develop/ui/views/launch/splash-screen"),
)

@Composable
fun AcknowledgmentsScreen(
    onNavigateBack: () -> Unit
) {
    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()

    val contentBackdrop = kyantRememberLayerBackdrop()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val miuixBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    Scaffold(
        topBar = {
            BlurredBar(hazeState, backdrop = miuixBackdrop, contentBackdrop = contentBackdrop) {
                SmallTopAppBar(
                    title = "致谢",
                    scrollBehavior = MiuixScrollBehavior(),
                    color = Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        val appThemeMode = LocalAppThemeMode.current
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                            }
                        } else {
                            GlassSymbolIconButton(onClick = onNavigateBack, style = GlassSymbolButtonStyle.NonTinted) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "返回")
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.hazeSource(hazeState).kyantLayerBackdrop(contentBackdrop).layerBackdrop(miuixBackdrop)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                SettingsSection(title = null) {
                    libraries.forEachIndexed { index, library ->
                        LibraryRow(
                            name = library.name,
                            author = library.author,
                            url = library.url
                        )
                        if (index < libraries.size - 1) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                val miuixTextStyles = MiuixTheme.textStyles

                Text(
                    text = "感谢开源社区的贡献者们 ❤️",
                    style = miuixTextStyles.footnote1,
                    color = getLabelsVibrantTertiary(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
                )
            }
        }
    }
}

@Composable
fun LibraryRow(
    name: String,
    author: String,
    url: String
) {
    val context = LocalContext.current
    val miuixTextStyles = MiuixTheme.textStyles

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = IOSColors.Blue,
            fontWeight = FontWeight.Medium,
            style = miuixTextStyles.body1,
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
        )
        Text(
            text = author,
            color = IOSColors.Blue,
            style = miuixTextStyles.body2,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        )
    }
}
