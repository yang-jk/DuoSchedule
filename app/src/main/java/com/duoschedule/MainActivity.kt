package com.duoschedule

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.duoschedule.data.model.ThemeMode
import com.duoschedule.ui.navigation.BottomNavItem
import com.duoschedule.ui.navigation.DuoScheduleNavGraph
import com.duoschedule.ui.settings.SettingsViewModel
import com.duoschedule.ui.theme.DuoScheduleTheme
import com.duoschedule.ui.theme.LiquidBottomTab
import com.duoschedule.ui.theme.LiquidBottomTabs
import com.duoschedule.ui.theme.LiquidBottomTabsSpec
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.LocalSharedTransitionScope
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.LocalLiquidBottomTabContentColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.duoschedule.util.PerformanceMonitor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingImportUri: Uri? = null
    private var backInvokedCallback: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        PerformanceMonitor.recordStartupComplete()

        updatePredictiveBack(false)

        pendingImportUri = extractFileUri(intent)

        val settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.predictiveBackEnabled.collect { enabled ->
                    updatePredictiveBack(enabled)
                }
            }
        }

        @OptIn(ExperimentalSharedTransitionApi::class)
        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val predictiveBackEnabled by viewModel.predictiveBackEnabled.collectAsStateWithLifecycle()
            val singleModeEnabled by viewModel.singleModeEnabled.collectAsStateWithLifecycle()

            DuoScheduleTheme(themeMode = themeMode) {
                val darkTheme = LocalDarkTheme.current
                MiuixTheme(colors = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var selectedTabIndex by remember { mutableIntStateOf(0) }

                val bottomNavItems = if (singleModeEnabled) {
                    listOf(
                        BottomNavItem.Home,
                        BottomNavItem.ScheduleA,
                        BottomNavItem.Settings
                    )
                } else {
                    listOf(
                        BottomNavItem.Home,
                        BottomNavItem.ScheduleA,
                        BottomNavItem.ScheduleB,
                        BottomNavItem.Settings
                    )
                }

                val showBottomBar = currentRoute in bottomNavItems.map { it.route }

                val personAName by viewModel.personAName.collectAsStateWithLifecycle()
                val personBName by viewModel.personBName.collectAsStateWithLifecycle()

                LaunchedEffect(singleModeEnabled) {
                    if (singleModeEnabled && currentRoute == BottomNavItem.ScheduleB.route) {
                        navController.navigate(BottomNavItem.ScheduleA.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    val routeIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                    selectedTabIndex = routeIndex
                }

                val backgroundColor = MaterialTheme.colorScheme.background
                val backdrop = rememberLayerBackdrop()
                val contentBackdrop = rememberLayerBackdrop()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                            .background(backgroundColor)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(contentBackdrop)
                    ) {
                        SharedTransitionLayout {
                            CompositionLocalProvider(
                                LocalSharedTransitionScope provides this,
                                LocalBackdrop provides backdrop
                            ) {
                                DuoScheduleNavGraph(
                                    navController = navController,
                                    startDestination = BottomNavItem.Home.route,
                                    pendingImportUri = pendingImportUri,
                                    onImportHandled = {
                                        pendingImportUri = null
                                    }
                                )
                            }
                        }
                    }

                    if (showBottomBar) {
                        val currentSelectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                        LaunchedEffect(currentRoute, bottomNavItems.size) {
                            selectedTabIndex = currentSelectedIndex
                        }

                        LiquidBottomTabs(
                            selectedTabIndex = { selectedTabIndex },
                            onTabSelected = { index ->
                                selectedTabIndex = index
                                val item = bottomNavItems.getOrNull(index) ?: return@LiquidBottomTabs
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            backdrop = contentBackdrop,
                            tabsCount = bottomNavItems.size,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 36.dp)
                        ) {
                            val tabContentColor = LocalLiquidBottomTabContentColor.current
                            bottomNavItems.forEachIndexed { index, item ->
                                val displayTitle = when (item) {
                                    is BottomNavItem.ScheduleA -> "${personAName}的课表"
                                    is BottomNavItem.ScheduleB -> if (singleModeEnabled) "课表" else "${personBName}的课表"
                                    else -> item.title
                                }
                                LiquidBottomTab(
                                    onClick = { selectedTabIndex = index }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = displayTitle,
                                        tint = tabContentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = displayTitle,
                                        color = tabContentColor,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingImportUri = extractFileUri(intent)
    }

    @Suppress("NewApi")
    private fun updatePredictiveBack(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backInvokedCallback?.let {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it as android.window.OnBackInvokedCallback)
            }
            backInvokedCallback = null

            if (!enabled) {
                val callback = android.window.OnBackInvokedCallback {
                    onBackPressedDispatcher.onBackPressed()
                }
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT + 1,
                    callback
                )
                backInvokedCallback = callback
            }
        }
    }
    
    private fun extractFileUri(intent: Intent?): Uri? {
        return intent?.let {
            when (it.action) {
                Intent.ACTION_VIEW -> it.data
                Intent.ACTION_SEND -> {
                    if (it.hasExtra(Intent.EXTRA_STREAM)) {
                        it.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
    }
}
