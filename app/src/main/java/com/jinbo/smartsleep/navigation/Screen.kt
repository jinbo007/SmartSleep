package com.jinbo.smartsleep.navigation

/**
 * Sealed class defining all screens in the app
 * Each screen has a route string for navigation
 */
sealed class Screen(val route: String) {
    // Home screen - Main monitoring interface
    data object Home : Screen("home")

    // History screen - List of past sessions
    data object History : Screen("history")

    // Statistics screen - Analytics and trends
    data object Statistics : Screen("statistics")

    // Settings screen - App configuration
    data object Settings : Screen("settings")

    // Session Detail screen - Individual session analysis
    data object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }
}
