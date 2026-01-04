package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.service.SnoreDetectionService
import com.jinbo.smartsleep.ui.AmplitudeGraph
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes
import com.jinbo.smartsleep.viewmodel.HomeViewModel
import com.jinbo.smartsleep.viewmodel.MonitoringUiState

/**
 * History Screen - Displays historical data and live monitoring curve
 */
@Composable
fun HistoryScreen(
    homeViewModel: HomeViewModel? = null,
    onSessionClick: (Long) -> Unit = {}
) {
    val uiState by homeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(MonitoringUiState())
    }

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
                    text = "Sleep History",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View your sleep patterns and monitoring data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Monitoring Curve
        if (uiState.amplitudeHistory.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.large,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = AppDimens.elevation_medium
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.card_padding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Live Monitoring Curve",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (uiState.isMonitoring) "Recording..." else "Last Session",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimens.spacing_3))

                        // Amplitude Graph
                        AmplitudeGraph(
                            amplitudes = uiState.amplitudeHistory,
                            threshold = SnoreDetectionService.RMS_THRESHOLD.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Session Statistics
        if (uiState.snoreCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.large,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = AppDimens.elevation_small
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.card_padding),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
                    ) {
                        Text(
                            text = "Session Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Snore Events",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = uiState.snoreCount.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Max Intensity",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = uiState.maxAmplitude.toInt().toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Data Points",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = uiState.amplitudeHistory.size.toString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }

        // Empty State
        if (uiState.amplitudeHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = AppShapes.large
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No monitoring data yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Start monitoring from the Home tab to see data here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Future: Historical sessions list (Phase 3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.card_padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Historical sessions coming soon...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
