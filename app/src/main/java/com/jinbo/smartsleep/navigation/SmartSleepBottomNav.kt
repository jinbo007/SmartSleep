package com.jinbo.smartsleep.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jinbo.smartsleep.ui.theme.AppDimens

/**
 * Bottom navigation data class
 */
data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation bar with 4 items
 */
@Composable
fun SmartSleepBottomNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            icon = Icons.Filled.Home,
            label = "Home"
        ),
        BottomNavItem(
            screen = Screen.History,
            icon = Icons.Filled.History,
            label = "History"
        ),
        BottomNavItem(
            screen = Screen.Statistics,
            icon = Icons.Filled.Analytics,
            label = "Statistics"
        ),
        BottomNavItem(
            screen = Screen.Settings,
            icon = Icons.Filled.Settings,
            label = "Settings"
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = AppDimens.elevation_medium
    ) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route ||
                            (item.screen == Screen.Home && currentRoute?.startsWith("session_detail") == true)

            var buttonScale by remember { mutableStateOf(1f) }

            // Animated scale for selected state
            val selectedScale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "selected_scale"
            )

            // Animated scale for click feedback
            val clickScale by animateFloatAsState(
                targetValue = buttonScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "click_scale"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(selectedScale * clickScale)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = if (isSelected) {
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        } else {
                            MaterialTheme.typography.labelSmall
                        }
                    )
                },
                selected = isSelected,
                onClick = {
                    // Navigate to the selected screen
                    if (currentRoute != item.screen.route) {
                        // Scale down animation for click feedback
                        buttonScale = 0.85f
                        // Reset scale after animation
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            buttonScale = 1f
                        }, 150)

                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            )
        }
    }
}
