package com.exoduss.cronos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exoduss.cronos.ui.calendar.CalendarScreen
import com.exoduss.cronos.ui.home.HomeScreen
import com.exoduss.cronos.ui.home.HomeViewModel
import com.exoduss.cronos.ui.notifications.NotificationsScreen
import com.exoduss.cronos.ui.onboarding.OnboardingScreen
import com.exoduss.cronos.ui.permissions.PermissionsScreen
import com.exoduss.cronos.ui.settings.SettingsScreen
import com.exoduss.cronos.ui.shared.TaskFormViewModel
import com.exoduss.cronos.ui.tasks.TasksScreen

private data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Calendar, Icons.Default.CalendarMonth, "Calendário"),
    BottomNavItem(Screen.Tasks, Icons.Default.CheckBox, "Tarefas"),
    BottomNavItem(Screen.Notifications, Icons.Default.Notifications, "Avisos"),
    BottomNavItem(Screen.Settings, Icons.Default.Settings, "Config")
)

private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun CronosNavGraph(
    startDestination: String,
    pendingNotifTaskId: String? = null,
    onNotifTaskConsumed: () -> Unit = {},
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    // TaskFormViewModel compartilhado — scoped ao NavGraph (Activity lifecycle)
    val taskFormViewModel: TaskFormViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    // Abre o detail da tarefa quando o app é iniciado via notificação
    LaunchedEffect(pendingNotifTaskId) {
        val taskId = pendingNotifTaskId ?: return@LaunchedEffect
        val task = taskFormViewModel.getTaskById(taskId)
        if (task != null) taskFormViewModel.openDetail(task)
        onNotifTaskConsumed()
    }

    val overdueTasks by homeViewModel.overdueTasks.collectAsStateWithLifecycle()
    val overdueCount = overdueTasks.size

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                if (item.screen == Screen.Notifications && overdueCount > 0) {
                                    BadgedBox(badge = { Badge { Text(overdueCount.toString()) } }) {
                                        Icon(item.icon, item.label)
                                    }
                                } else {
                                    Icon(item.icon, item.label)
                                }
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onDone = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onThemeChange = onThemeChange
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(formViewModel = taskFormViewModel)
            }
            composable(Screen.Tasks.route) {
                TasksScreen(formViewModel = taskFormViewModel)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(formViewModel = taskFormViewModel)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(formViewModel = taskFormViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onThemeChange = onThemeChange)
            }
        }
    }
}
