package com.jinbo.smartsleep.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.service.SnoreDetectionService
import com.jinbo.smartsleep.ui.components.cards.StatCard
import com.jinbo.smartsleep.ui.screens.home.MonitoringCard
import com.jinbo.smartsleep.ui.screens.home.QuickActionsCard
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.viewmodel.HomeViewModel
import com.jinbo.smartsleep.viewmodel.MonitoringUiState

/**
 * Home Screen - Main monitoring interface with real-time updates
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel? = null,
    onViewReport: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by homeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(MonitoringUiState())
    }

    // Initialize session data
    LaunchedEffect(Unit) {
        homeViewModel?.initializeSession(context)
    }

    // Main screen layout
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.screen_padding),
        contentPadding = PaddingValues(bottom = AppDimens.bottom_nav_height),
        verticalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
            ) {
                Text(
                    text = "Good Night",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track your sleep quality",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Monitoring Card
        item {
            MonitoringCard(
                isMonitoring = uiState.isMonitoring,
                amplitudeHistory = uiState.amplitudeHistory,
                snoreCount = uiState.snoreCount
            )
        }

        // Today's Summary Cards
        if (uiState.isMonitoring || uiState.snoreCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
                ) {
                    // Snore Count Card
                    StatCard(
                        icon = Icons.Default.ShowChart,
                        label = "Snore Events",
                        value = uiState.snoreCount.toString(),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Max Amplitude Card
                    StatCard(
                        icon = Icons.Default.Bedtime,
                        label = "Max Intensity",
                        value = uiState.maxAmplitude.toInt().toString(),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Quick Actions Card
        item {
            QuickActionsCard(
                isMonitoring = uiState.isMonitoring,
                onStartMonitoring = {
                    homeViewModel?.startMonitoring(context)
                },
                onStopMonitoring = {
                    homeViewModel?.stopMonitoring(context)
                    // Don't navigate away - stay on Home screen
                }
            )
        }
    }
}
