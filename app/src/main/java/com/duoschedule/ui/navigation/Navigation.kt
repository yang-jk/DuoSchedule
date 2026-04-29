package com.duoschedule.ui.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.duoschedule.data.importexport.CourseImportData
import com.duoschedule.data.importexport.ImportResult
import com.duoschedule.data.importexport.ImportPreviewData
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.edit.CourseEditScreen
import com.duoschedule.ui.main.MainScreen
import com.duoschedule.ui.schedule.ScheduleScreen
import com.duoschedule.ui.settings.*
import com.duoschedule.ui.theme.BackgroundsLight
import com.duoschedule.ui.theme.BackgroundsDark
import com.duoschedule.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch

private val iosTransitionSpec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)
private val iosFadeSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
private val bottomNavTransitionSpec = tween<IntOffset>(durationMillis = 250, easing = FastOutSlowInEasing)

private val bottomNavRoutes = listOf(
    BottomNavItem.Home.route,
    BottomNavItem.ScheduleB.route,
    BottomNavItem.ScheduleA.route,
    BottomNavItem.Settings.route
)

private fun isBottomNavRoute(route: String?): Boolean {
    return route in bottomNavRoutes
}

private fun getSlideDirection(fromRoute: String?, toRoute: String?): Int {
    if (!isBottomNavRoute(fromRoute) || !isBottomNavRoute(toRoute)) {
        return 1
    }
    val fromIndex = bottomNavRoutes.indexOf(fromRoute)
    val toIndex = bottomNavRoutes.indexOf(toRoute)
    return if (toIndex > fromIndex) 1 else -1
}



@Composable
fun DuoScheduleNavGraph(
    navController: NavHostController,
    startDestination: String = BottomNavItem.Home.route,
    pendingImportUri: Uri? = null,
    onImportHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkTheme.current
    val backgroundColor = if (darkTheme) BackgroundsDark.Primary else BackgroundsLight.Primary
    
    var pendingImportData by remember { mutableStateOf<ImportPreviewData?>(null) }
    var externalImportUri by remember { mutableStateOf<Uri?>(null) }
    var showExternalImportDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(pendingImportUri) {
        pendingImportUri?.let { uri ->
            externalImportUri = uri
            showExternalImportDialog = true
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(backgroundColor),
        enterTransition = {
            val fromRoute = this.initialState?.destination?.route
            val toRoute = this.targetState.destination.route
            val isBottomNavTransition = isBottomNavRoute(fromRoute) && isBottomNavRoute(toRoute)
            
            if (isBottomNavTransition) {
                val direction = getSlideDirection(fromRoute, toRoute)
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    ),
                    initialOffsetX = { fullWidth -> 
                        if (direction > 0) (fullWidth * 0.08f).toInt() else -(fullWidth * 0.08f).toInt()
                    }
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                )
            } else {
                slideInHorizontally(
                    animationSpec = iosTransitionSpec,
                    initialOffsetX = { fullWidth -> fullWidth }
                ) + fadeIn(animationSpec = iosFadeSpec)
            }
        },
        exitTransition = {
            val fromRoute = this.initialState?.destination?.route
            val toRoute = this.targetState.destination.route
            val isBottomNavTransition = isBottomNavRoute(fromRoute) && isBottomNavRoute(toRoute)
            
            if (isBottomNavTransition) {
                val direction = getSlideDirection(fromRoute, toRoute)
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    ),
                    targetOffsetX = { fullWidth -> 
                        if (direction > 0) -(fullWidth * 0.08f).toInt() else (fullWidth * 0.08f).toInt()
                    }
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                )
            } else {
                slideOutHorizontally(
                    animationSpec = iosTransitionSpec,
                    targetOffsetX = { fullWidth -> -fullWidth / 3 }
                ) + fadeOut(animationSpec = iosFadeSpec)
            }
        },
        popEnterTransition = {
            val fromRoute = this.initialState?.destination?.route
            val toRoute = this.targetState.destination.route
            val isBottomNavTransition = isBottomNavRoute(fromRoute) && isBottomNavRoute(toRoute)
            
            if (isBottomNavTransition) {
                val direction = getSlideDirection(fromRoute, toRoute)
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    ),
                    initialOffsetX = { fullWidth -> 
                        if (direction > 0) (fullWidth * 0.08f).toInt() else -(fullWidth * 0.08f).toInt()
                    }
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                )
            } else {
                slideInHorizontally(
                    animationSpec = iosTransitionSpec,
                    initialOffsetX = { fullWidth -> -fullWidth / 3 }
                ) + fadeIn(animationSpec = iosFadeSpec)
            }
        },
        popExitTransition = {
            val fromRoute = this.initialState?.destination?.route
            val toRoute = this.targetState.destination.route
            val isBottomNavTransition = isBottomNavRoute(fromRoute) && isBottomNavRoute(toRoute)
            
            if (isBottomNavTransition) {
                val direction = getSlideDirection(fromRoute, toRoute)
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    ),
                    targetOffsetX = { fullWidth -> 
                        if (direction > 0) -(fullWidth * 0.08f).toInt() else (fullWidth * 0.08f).toInt()
                    }
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                )
            } else {
                slideOutHorizontally(
                    animationSpec = iosTransitionSpec,
                    targetOffsetX = { fullWidth -> fullWidth }
                ) + fadeOut(animationSpec = iosFadeSpec)
            }
        }
    ) {
        composable(BottomNavItem.Home.route) {
            MainScreen(
                onNavigateToEdit = { courseId, dayOfWeek, period, personType ->
                    val route = buildEditRoute(courseId, dayOfWeek, period, personType)
                    navController.navigate(route)
                }
            )
        }

        composable(BottomNavItem.ScheduleA.route) {
            ScheduleScreen(
                personType = PersonType.PERSON_A,
                onNavigateToEdit = { courseId, dayOfWeek, period ->
                    val route = buildEditRoute(courseId, dayOfWeek, period, PersonType.PERSON_A)
                    navController.navigate(route)
                }
            )
        }

        composable(BottomNavItem.ScheduleB.route) {
            ScheduleScreen(
                personType = PersonType.PERSON_B,
                onNavigateToEdit = { courseId, dayOfWeek, period ->
                    val route = buildEditRoute(courseId, dayOfWeek, period, PersonType.PERSON_B)
                    navController.navigate(route)
                }
            )
        }

        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserSettings = { navController.navigate("settings/user") },
                onNavigateToScheduleSettings = { navController.navigate("settings/schedule") },
                onNavigateToDisplaySettings = { navController.navigate("settings/display") },
                onNavigateToDataManagement = { navController.navigate("settings/data") },
                onNavigateToNotificationSettings = { navController.navigate("settings/notification") }
            )
        }

        composable("settings/user") {
            UserSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings/schedule") {
            ScheduleSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPeriodTimes = { personType ->
                    navController.navigate("settings/schedule/period-times/${personType.name}")
                }
            )
        }

        composable(
            route = "settings/schedule/period-times/{personType}",
            arguments = listOf(
                navArgument("personType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val personTypeStr = backStackEntry.arguments?.getString("personType") ?: "PERSON_A"
            val personType = try { 
                PersonType.valueOf(personTypeStr) 
            } catch (e: Exception) { 
                PersonType.PERSON_A 
            }
            
            PeriodTimesSettingsScreen(
                personType = personType,
                personName = if (personType == PersonType.PERSON_A) "Ta" else "我",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings/display") {
            DisplaySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings/notification") {
            NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings/data") {
            DataManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImportPreview = { importData ->
                    pendingImportData = importData
                    navController.navigate("settings/import-preview")
                }
            )
        }
        
        composable("settings/import-preview") {
            pendingImportData?.let { data ->
                ImportPreviewScreen(
                    courses = data.courses,
                    onConfirm = { selectedCourses, targetPerson, mergeMode ->
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                        pendingImportData = null
                    },
                    onDismiss = {
                        navController.popBackStack(BottomNavItem.Home.route, inclusive = false)
                        pendingImportData = null
                    },
                    onImportSuccess = { targetPerson ->
                        val route = if (targetPerson == PersonType.PERSON_A) {
                            BottomNavItem.ScheduleA.route
                        } else {
                            BottomNavItem.ScheduleB.route
                        }
                        navController.navigate(route) {
                            popUpTo(BottomNavItem.Home.route) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        pendingImportData = null
                    },
                    onNavigateToHome = {
                        navController.popBackStack(BottomNavItem.Home.route, inclusive = false)
                        pendingImportData = null
                    },
                    importData = data
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(
            route = "edit?courseId={courseId}&dayOfWeek={dayOfWeek}&period={period}&personType={personType}",
            arguments = listOf(
                navArgument("courseId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("dayOfWeek") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("period") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("personType") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val courseIdStr = backStackEntry.arguments?.getString("courseId")
            val courseId = courseIdStr?.toLongOrNull()
            val dayOfWeek = backStackEntry.arguments?.getInt("dayOfWeek") ?: -1
            val period = backStackEntry.arguments?.getInt("period") ?: -1
            val personTypeStr = backStackEntry.arguments?.getString("personType")
            val initialPersonType = personTypeStr?.let { 
                try { PersonType.valueOf(it) } catch (e: Exception) { null }
            }
            CourseEditScreen(
                courseId = courseId,
                initialDayOfWeek = if (dayOfWeek > 0) dayOfWeek else null,
                initialPeriod = if (period > 0) period else null,
                initialPersonType = initialPersonType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
    
    if (showExternalImportDialog && externalImportUri != null) {
        ExternalImportDialog(
            uri = externalImportUri!!,
            onDismiss = {
                showExternalImportDialog = false
                externalImportUri = null
                onImportHandled()
            },
            onImportSuccess = { importData ->
                pendingImportData = importData
                showExternalImportDialog = false
                externalImportUri = null
                onImportHandled()
                navController.navigate("settings/import-preview")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExternalImportDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onImportSuccess: (ImportPreviewData) -> Unit,
    viewModel: com.duoschedule.ui.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var internalResult by remember { mutableStateOf<ImportResult?>(null) }
    var internalLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uri) {
        if (internalResult == null && !internalLoading) {
            scope.launch {
                internalLoading = true
                val importResult = viewModel.importFromCsv(context, uri)
                internalResult = importResult
                internalLoading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        title = { Text("导入课程") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (internalLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("正在解析文件...")
                    }
                } else if (internalResult != null) {
                    if (internalResult!!.success && internalResult!!.courses.isNotEmpty()) {
                        val fileTypeText = if (internalResult!!.fileType == com.duoschedule.data.importexport.CsvFileType.APP_EXPORT) {
                            "应用导出文件"
                        } else {
                            "模板文件"
                        }
                        Text("检测到 $fileTypeText，共 ${internalResult!!.courses.size} 门课程")
                        if (internalResult!!.errors.isNotEmpty()) {
                            Text(
                                text = "部分课程解析失败：",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            internalResult!!.errors.forEach { error ->
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "导入失败",
                            color = MaterialTheme.colorScheme.error
                        )
                        internalResult!!.errors.forEach { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Text("正在准备导入...")
                }
            }
        },
        confirmButton = {
            if (internalResult != null && internalResult!!.success && internalResult!!.courses.isNotEmpty()) {
                TextButton(onClick = {
                    onImportSuccess(ImportPreviewData(
                        courses = internalResult!!.courses,
                        fileType = internalResult!!.fileType,
                        settingsA = internalResult!!.settingsA,
                        settingsB = internalResult!!.settingsB,
                        personAName = internalResult!!.personAName,
                        personBName = internalResult!!.personBName
                    ))
                }) {
                    Text("继续")
                }
            } else if (internalResult != null) {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        },
        dismissButton = {
            if (internalLoading) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

private fun buildEditRoute(courseId: Long?, dayOfWeek: Int?, period: Int?, personType: PersonType?): String {
    val courseIdStr = courseId?.toString() ?: ""
    val dayOfWeekStr = dayOfWeek?.toString() ?: "-1"
    val periodStr = period?.toString() ?: "-1"
    val personTypeStr = personType?.name ?: ""
    return "edit?courseId=$courseIdStr&dayOfWeek=$dayOfWeekStr&period=$periodStr&personType=$personTypeStr"
}
