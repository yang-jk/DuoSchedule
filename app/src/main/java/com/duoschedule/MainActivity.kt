package com.duoschedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.LocalLiquidBottomTabContentColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.duoschedule.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingImportUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PerformanceMonitor.recordStartupComplete()
        
        pendingImportUri = extractFileUri(intent)
        
        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            
            DuoScheduleTheme(themeMode = themeMode) {
                val darkTheme = LocalDarkTheme.current
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var selectedTabIndex by remember { mutableIntStateOf(0) }

                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.ScheduleB,
                    BottomNavItem.ScheduleA,
                    BottomNavItem.Settings
                )

                val showBottomBar = currentRoute in bottomNavItems.map { it.route }
                
                val personAName by viewModel.personAName.collectAsStateWithLifecycle()
                val personBName by viewModel.personBName.collectAsStateWithLifecycle()

                val backgroundColor = MaterialTheme.colorScheme.background
                val backdrop = rememberLayerBackdrop()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                            .background(backgroundColor)
                    )

                    CompositionLocalProvider(LocalBackdrop provides backdrop) {
                        DuoScheduleNavGraph(
                            navController = navController,
                            startDestination = BottomNavItem.Home.route,
                            pendingImportUri = pendingImportUri,
                            onImportHandled = {
                                pendingImportUri = null
                            }
                        )
                    }

                    if (showBottomBar) {
                        val currentSelectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                        LaunchedEffect(currentRoute) {
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
                            backdrop = backdrop,
                            tabsCount = bottomNavItems.size,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 36.dp)
                        ) {
                            val tabContentColor = LocalLiquidBottomTabContentColor.current
                            bottomNavItems.forEachIndexed { index, item ->
                                val displayTitle = when (item) {
                                    is BottomNavItem.ScheduleA -> "${personAName}的课表"
                                    is BottomNavItem.ScheduleB -> "${personBName}的课表"
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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingImportUri = extractFileUri(intent)
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
