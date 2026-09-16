package com.exoduss.cronos.ui.navigation

sealed class Screen(val route: String) {
    object Permissions    : Screen("permissions")
    object Onboarding     : Screen("onboarding")
    object Home           : Screen("home")
    object Tasks          : Screen("tasks")
    object Calendar       : Screen("calendar")
    object Settings       : Screen("settings")
}
