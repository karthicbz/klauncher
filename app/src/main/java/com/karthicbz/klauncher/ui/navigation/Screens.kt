package com.karthicbz.klauncher.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}
