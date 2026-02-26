package com.duoschedule.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "主页",
        icon = Icons.Default.Home
    )

    object ScheduleB : BottomNavItem(
        route = "schedule_b",
        title = "我的课表",
        icon = Icons.Default.Person
    )

    object ScheduleA : BottomNavItem(
        route = "schedule_a",
        title = "Ta的课表",
        icon = Icons.Default.Person
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = Icons.Default.Settings
    )
}
