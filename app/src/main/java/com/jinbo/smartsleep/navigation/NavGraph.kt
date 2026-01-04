package com.jinbo.smartsleep.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jinbo.smartsleep.ui.screens.HomeScreen
import com.jinbo.smartsleep.ui.screens.HistoryScreen
import com.jinbo.smartsleep.ui.screens.SessionDetailScreen
import com.jinbo.smartsleep.ui.screens.SettingsScreen
import com.jinbo.smartsleep.ui.screens.StatisticsScreen
import com.jinbo.smartsleep.viewmodel.HomeViewModel

/**
 * Main navigation graph for the app
 * Defines all navigation routes and transitions
 */
@Composable
fun SmartSleepNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
    homeViewModel: HomeViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        // Home Screen - Main monitoring interface
        composable(route = Screen.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel,
                onViewReport = {
                    navController.navigate(Screen.SessionDetail.createRoute(0))
                }
            )
        }

        // History Screen - List of past sessions
        composable(route = Screen.History.route) {
            HistoryScreen(
                homeViewModel = homeViewModel,
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                }
            )
        }

        // Statistics Screen - Analytics and trends
        composable(route = Screen.Statistics.route) {
            StatisticsScreen(
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                }
            )
        }

        // Settings Screen - App configuration
        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        // Session Detail Screen - Individual session analysis
        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            SessionDetailScreen(
                sessionId = sessionId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Transition animations
private fun fadeIn(): EnterTransition {
    return androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
}

private fun fadeOut(): ExitTransition {
    return androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
}
