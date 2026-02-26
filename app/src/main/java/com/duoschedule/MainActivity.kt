package com.duoschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.duoschedule.ui.theme.glassmorphismBackgroundColor
import com.duoschedule.ui.theme.glassmorphismBorderColor
import com.duoschedule.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PerformanceMonitor.recordStartupComplete()
        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            
            DuoScheduleTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.ScheduleB,
                    BottomNavItem.ScheduleA,
                    BottomNavItem.Settings
                )

                val showBottomBar = currentRoute in bottomNavItems.map { it.route }
                
                val personAName by viewModel.personAName.collectAsStateWithLifecycle()
                val personBName by viewModel.personBName.collectAsStateWithLifecycle()

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            CapsuleBottomNavigationBar(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onItemSelected = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                personAName = personAName,
                                personBName = personBName
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { _paddingValues ->
                    DuoScheduleNavGraph(
                        navController = navController,
                        startDestination = BottomNavItem.Home.route
                    )
                }
            }
        }
    }
}

@Composable
fun CapsuleBottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    personAName: String,
    personBName: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = glassmorphismBackgroundColor()
    val borderColor = glassmorphismBorderColor()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(36.dp),
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val displayTitle = when (item) {
                        is BottomNavItem.ScheduleA -> "${personAName}的课表"
                        is BottomNavItem.ScheduleB -> "${personBName}的课表"
                        else -> item.title
                    }
                    CapsuleNavItem(
                        item = item,
                        displayTitle = displayTitle,
                        isSelected = isSelected,
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun CapsuleNavItem(
    item: BottomNavItem,
    displayTitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isSelected -> 1.05f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_item_scale"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "nav_item_alpha"
    )

    val glowIntensity by animateFloatAsState(
        targetValue = if (isSelected) 0.3f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "nav_item_glow"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .alpha(iconAlpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = glowIntensity)
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = displayTitle,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = displayTitle,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
