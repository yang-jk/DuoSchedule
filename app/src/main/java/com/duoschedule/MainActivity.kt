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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.duoschedule.data.model.AppThemeMode
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.navigation.BottomNavItem
import com.duoschedule.ui.navigation.DuoScheduleNavGraph
import com.duoschedule.ui.settings.SettingsViewModel
import com.duoschedule.ui.theme.DuoScheduleTheme
import com.duoschedule.ui.theme.LiquidBottomTab
import com.duoschedule.ui.theme.LiquidBottomTabs
import com.duoschedule.ui.theme.LiquidBottomTabsSpec
import com.duoschedule.ui.theme.LocalBackdrop
import com.duoschedule.ui.theme.LocalDarkTheme
import com.duoschedule.ui.theme.LocalAppThemeMode
import com.duoschedule.ui.theme.LocalLiquidBottomTabContentColor
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import top.yukonga.miuix.kmp.blur.layerBackdrop as miuixLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as miuixRememberLayerBackdrop
import com.duoschedule.data.sync.SyncManager
import com.duoschedule.data.sync.SyncPreferences
import com.duoschedule.util.PerformanceMonitor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import java.time.LocalDate
import javax.inject.Inject

object AddButtonReveal {
    var sourceBounds: Rect = Rect.Zero
    var shouldReveal by mutableStateOf(false)
    var shouldConceal by mutableStateOf(false)
    var enteredViaReveal by mutableStateOf(false)
    private var concealCallback: (() -> Unit)? = null

    fun startReveal(bounds: Rect) {
        sourceBounds = bounds
        shouldReveal = true
        enteredViaReveal = true
    }

    fun startConceal(bounds: Rect, onComplete: () -> Unit) {
        sourceBounds = bounds
        shouldConceal = true
        concealCallback = onComplete
    }

    fun onConcealComplete() {
        shouldConceal = false
        enteredViaReveal = false
        concealCallback?.invoke()
        concealCallback = null
    }

    fun reset() {
        shouldReveal = false
        shouldConceal = false
        enteredViaReveal = false
        concealCallback = null
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var syncPreferences: SyncPreferences

    private var pendingImportUri: Uri? = null
    private var backInvokedCallback: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            @Suppress("DEPRECATION")
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

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
            val predictiveBackEnabled by viewModel.predictiveBackEnabled.collectAsStateWithLifecycle()
            val singleModeEnabled by viewModel.singleModeEnabled.collectAsStateWithLifecycle()

            DuoScheduleTheme(themeMode = themeMode) {
                val darkTheme = LocalDarkTheme.current
                MiuixTheme(colors = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                CompositionLocalProvider(LocalAppThemeMode provides appThemeMode) {
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

                // 处理待办提醒通知点击
                LaunchedEffect(intent) {
                    if (intent?.getBooleanExtra("navigate_to_todo_edit", false) == true) {
                        val todoId = intent.getLongExtra("todo_id", 0L)
                        if (todoId > 0) {
                            navController.navigate("todo_edit?todoId=$todoId")
                        }
                        intent.removeExtra("navigate_to_todo_edit")
                        intent.removeExtra("todo_id")
                    }
                }

                val backgroundColor = MaterialTheme.colorScheme.background
                val backdrop = rememberLayerBackdrop()
                val contentBackdrop = rememberLayerBackdrop()
                val miuixContentBackdrop = miuixRememberLayerBackdrop()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                            .background(backgroundColor)
                    )

                    CompositionLocalProvider(LocalBackdrop provides backdrop) {
                        if (appThemeMode == AppThemeMode.MIUIX) {
                            // MIUIX 模式：内容区域注册到 miuixLayerBackdrop（用于 BlurredBar 等组件的模糊效果）
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .layerBackdrop(contentBackdrop)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .miuixLayerBackdrop(miuixContentBackdrop)
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
                        } else {
                            // iOS 模式：原有布局
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .layerBackdrop(contentBackdrop)
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

                    // 统一底栏：两种模式都使用 iOS 风格的 LiquidBottomTabs
                    if (showBottomBar) {
                        val currentSelectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                        LaunchedEffect(currentRoute, bottomNavItems.size) {
                            selectedTabIndex = currentSelectedIndex
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
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
                                modifier = Modifier.weight(1f)
                            ) {
                                val tabContentColor = LocalLiquidBottomTabContentColor.current
                                bottomNavItems.forEachIndexed { index, item ->
                                    val displayTitle = when (item) {
                                        is BottomNavItem.ScheduleA -> "${personAName}的课表"
                                        is BottomNavItem.ScheduleB -> if (singleModeEnabled) "课表" else "${personBName}的课表"
                                        else -> item.title
                                    }
                                    LiquidBottomTab(
                                        onClick = {
                                            selectedTabIndex = index
                                        }
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

                            // 占位保持底栏右侧宽度（真正的添加按钮由 AddButtonRevealOverlay 渲染）
                            Spacer(
                                modifier = Modifier
                                    .width(LiquidBottomTabsSpec.Height)
                                    .height(LiquidBottomTabsSpec.Height)
                            )
                        }
                    }

                    // 添加按钮及其展开菜单/揭示动画覆盖层（与底栏一致显示）
                    AddButtonRevealOverlay(
                        navController = navController,
                        contentBackdrop = contentBackdrop,
                        isVisible = showBottomBar,
                        personType = when (currentRoute) {
                            BottomNavItem.ScheduleA.route -> PersonType.PERSON_A
                            BottomNavItem.ScheduleB.route -> PersonType.PERSON_B
                            else -> PersonType.PERSON_A
                        }
                    )
                }
                }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val enabled = syncPreferences.syncEnabled.first()
            if (!enabled) return@launch
            val lastSyncTime = syncPreferences.lastSyncTime.first()
            val now = System.currentTimeMillis()
            if (now - lastSyncTime > 30_000) {
                try {
                    syncManager.sync()
                } catch (_: Exception) {
                    // 静默失败
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingImportUri = extractFileUri(intent)
        setIntent(intent)
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
                        @Suppress("DEPRECATION")
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

@Composable
private fun AddButtonWithMenu(
    personType: PersonType,
    contentColor: ComposeColor,
    containerColor: ComposeColor,
    contentBackdrop: LayerBackdrop,
    navController: NavController,
    revealInProgress: Boolean,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        showAddMenu = false
    }

    val addMenuProgress by animateFloatAsState(
        targetValue = if (showAddMenu) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "addMenuProgress"
    )

    val collapsedSize = LiquidBottomTabsSpec.Height
    val expandedMenuHeight = 128.dp
    val addButtonHeight by animateDpAsState(
        targetValue = if (showAddMenu) expandedMenuHeight else collapsedSize,
        animationSpec = tween(durationMillis = 300),
        label = "addButtonHeight"
    )

    Box(
        modifier = modifier
            .width(LiquidBottomTabsSpec.Height)
            .height(addButtonHeight)
            .clip(Capsule())
            .onGloballyPositioned { coords ->
                AddButtonReveal.sourceBounds = coords.boundsInWindow()
            }
            .drawBackdrop(
                backdrop = contentBackdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(LiquidBottomTabsSpec.BlurRadius.toPx())
                    lens(
                        LiquidBottomTabsSpec.LensRefractionHeight.toPx() * addMenuProgress,
                        LiquidBottomTabsSpec.LensRefractionAmount.toPx() * addMenuProgress
                    )
                },
                highlight = {
                    Highlight.Default.copy(
                        width = 1.dp,
                        alpha = 0.75f
                    )
                },
                onDrawSurface = { drawRect(containerColor) }
            )
    ) {
        // 竖向菜单内容（渲染在底层，收起时不参与命中测试）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = addMenuProgress }
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 添加课程
            Column(
                modifier = Modifier
                    .clip(Capsule())
                    .then(
                        if (showAddMenu && !revealInProgress) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showAddMenu = false
                            AddButtonReveal.startReveal(AddButtonReveal.sourceBounds)
                            navController.navigate("edit?courseId=&dayOfWeek=-1&period=-1&personType=${personType.name}")
                        } else Modifier
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "课程",
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }

            // 添加待办
            Column(
                modifier = Modifier
                    .clip(Capsule())
                    .then(
                        if (showAddMenu && !revealInProgress) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showAddMenu = false
                            AddButtonReveal.startReveal(AddButtonReveal.sourceBounds)
                            navController.navigate("todo_edit?todoId=&date=${LocalDate.now().toEpochDay()}&startHour=-1&startMinute=-1&endHour=-1&endMinute=-1&personType=${personType.name}")
                        } else Modifier
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "待办",
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }

        // "+" 图标（渲染在顶层，收起时可点击展开）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 1f - addMenuProgress }
                .then(
                    if (!showAddMenu && !revealInProgress) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showAddMenu = true
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加",
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = addMenuProgress * 45f }
            )
        }
    }
}

@Composable
private fun AddButtonRevealOverlay(
    navController: NavController,
    contentBackdrop: LayerBackdrop,
    isVisible: Boolean,
    personType: PersonType
) {
    val isLightTheme = !LocalDarkTheme.current
    val containerColor =
        if (isLightTheme) LiquidBottomTabsSpec.ContainerColorLight
        else LiquidBottomTabsSpec.ContainerColorDark
    val contentColor =
        if (isLightTheme) LiquidBottomTabsSpec.ContentColorLight
        else LiquidBottomTabsSpec.ContentColorDark

    var revealTarget by remember { mutableStateOf(0f) }

    LaunchedEffect(AddButtonReveal.shouldReveal, AddButtonReveal.shouldConceal) {
        when {
            AddButtonReveal.shouldReveal -> revealTarget = 1f
            AddButtonReveal.shouldConceal -> revealTarget = 0f
        }
    }

    val revealProgress by animateFloatAsState(
        targetValue = revealTarget,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "revealProgress",
        finishedListener = {
            if (AddButtonReveal.shouldReveal) {
                AddButtonReveal.shouldReveal = false
            }
            if (AddButtonReveal.shouldConceal) {
                AddButtonReveal.onConcealComplete()
            }
        }
    )

    val revealInProgress = AddButtonReveal.shouldReveal || AddButtonReveal.shouldConceal

    var overlayBounds by remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayBounds = it.boundsInWindow() }
    ) {
        // 正常状态下的添加按钮
        if (isVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.weight(1f))

                AddButtonWithMenu(
                    personType = personType,
                    contentColor = contentColor,
                    containerColor = containerColor,
                    contentBackdrop = contentBackdrop,
                    navController = navController,
                    revealInProgress = revealInProgress,
                    modifier = Modifier
                )
            }
        }

        // 按钮放大/收缩揭示覆盖层（正向放大成页面，反向收缩回按钮）
        if ((revealInProgress || revealProgress > 0f) && overlayBounds != Rect.Zero) {
            val src = AddButtonReveal.sourceBounds
            if (src != Rect.Zero) {
                val targetBounds = overlayBounds
                val currentLeft = src.left + (targetBounds.left - src.left) * revealProgress
                val currentTop = src.top + (targetBounds.top - src.top) * revealProgress
                val currentRight = src.right + (targetBounds.right - src.right) * revealProgress
                val currentBottom = src.bottom + (targetBounds.bottom - src.bottom) * revealProgress
                val currentWidth = currentRight - currentLeft
                val currentHeight = currentBottom - currentTop
                val cornerRadius = src.height / 2 * (1f - revealProgress)
                val alpha = if (revealProgress < 0.85f) 1f else 1f - (revealProgress - 0.85f) / 0.15f

                Box(
                    modifier = Modifier
                        .offset { IntOffset(currentLeft.toInt(), currentTop.toInt()) }
                        .width(with(density) { currentWidth.toDp() })
                        .height(with(density) { currentHeight.toDp() })
                        .graphicsLayer { this.alpha = alpha }
                        .clip(RoundedCornerShape(with(density) { cornerRadius.toDp() }))
                        .background(containerColor)
                )
            }
        }
    }
}
