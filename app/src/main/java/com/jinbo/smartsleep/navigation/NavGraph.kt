package com.jinbo.smartsleep.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        enterTransition = { slideIntoContainer() },
        exitTransition = { slideOutOfContainer() }
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

/**
 * Enhanced enter transition with slide and fade
 */
private fun slideIntoContainer(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it / 4 }, // Slide in from the right by 25% of screen width
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        )
    ) + fadeIn(
        animationSpec = tween(300)
    )
}

/**
 * Enhanced exit transition with slide and fade
 */
private fun slideOutOfContainer(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 4 }, // Slide out to the left by 25% of screen width
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        )
    ) + fadeOut(
        animationSpec = tween(300)
    )
}
