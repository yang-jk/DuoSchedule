package com.duoschedule.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duoschedule.ui.settings.components.*
import com.duoschedule.ui.theme.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import com.kyant.capsule.ContinuousRoundedRectangle

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val description: String,
    val url: String
)

val libraries = listOf(
    OpenSourceLibrary("Kotlin", "JetBrains", "Kotlin 编程语言", "https://kotlinlang.org"),
    OpenSourceLibrary("Hilt", "Google", "依赖注入框架", "https://dagger.dev/hilt/"),
    OpenSourceLibrary("OkHttp", "Square", "HTTP 客户端", "https://square.github.io/okhttp/"),
    OpenSourceLibrary("jsoup", "Jonathan Hedley", "Java HTML 解析器", "https://jsoup.org/"),
    OpenSourceLibrary("AndroidLiquidGlass", "kyant0", "Compose 液态玻璃效果", "https://github.com/kyant0/AndroidLiquidGlass"),
    OpenSourceLibrary("Shapes", "kyant0", "iOS 风格平滑圆角形状", "https://github.com/kyant0/Shapes"),
    OpenSourceLibrary("Capsule", "kyant0", "Compose 连续圆角矩形", "https://github.com/kyant0/Capsule")
)

@Composable
fun AcknowledgmentsScreen(
    onNavigateBack: () -> Unit
) {
    val cornerRadius = LocalDeviceCornerRadius.current

    Scaffold(
        modifier = Modifier.clip(RoundedCornerShape(cornerRadius)),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("开源致谢") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        val backgroundColor = MaterialTheme.colorScheme.background
        val scrollBackdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .layerBackdrop(scrollBackdrop)
            ) {
                SettingsSection(title = null) {
                    libraries.forEachIndexed { index, library ->
                        LibraryRow(
                            name = library.name,
                            author = library.author,
                            description = library.description,
                            url = library.url
                        )
                        if (index < libraries.size - 1) {
                            Separator(modifier = Modifier.padding(horizontal = Spacing.lg))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "感谢开源社区的贡献者们 ❤️",
                    style = MaterialTheme.typography.bodySmall,
                    color = getLabelsVibrantTertiary(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LiquidBottomTabsSpec.Height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
                )
            }

            ScrollTopBlurOverlay(backdrop = scrollBackdrop, scrollOffset = scrollState.value)
        }
    }
}

@Composable
fun LibraryRow(
    name: String,
    author: String,
    description: String,
    url: String
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(ContinuousRoundedRectangle(BorderRadius.iOS26.icon)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IOSColors.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = getLabelsVibrantPrimary()
            )
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = getLabelsVibrantSecondary()
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = getLabelsVibrantSecondary()
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = getLabelsVibrantSecondary()
        )
    }
}
