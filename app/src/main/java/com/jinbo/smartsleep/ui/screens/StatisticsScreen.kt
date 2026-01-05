package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.ui.screens.statistics.*
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.viewmodel.StatisticsViewModel
import com.jinbo.smartsleep.viewmodel.StatisticsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Statistics Screen - Analytics and trends
 */
@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.provideFactory(
            SessionRepository(LocalContext.current.applicationContext)
        )
    ),
    onSessionClick: (Long) -> Unit = {}
) {
    val uiState by statisticsViewModel.uiState.collectAsState()
    val selectedPeriod by statisticsViewModel.selectedPeriod.collectAsState()

    // Collect sessions flow
    var sessionsList by remember { mutableStateOf(emptyList<com.jinbo.smartsleep.data.database.SessionEntity>()) }

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPeriod) {
        statisticsViewModel.selectPeriod(selectedPeriod)
    }

    // Collect sessions directly from the Success state
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is StatisticsUiState.Success -> {
                state.sessions.collect { sessions ->
                    sessionsList = sessions
                }
            }
            else -> {}
        }
    }

    // Handle refresh
    val scope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            statisticsViewModel.refresh()
            delay(500)
            isRefreshing = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.screen_padding),
        contentPadding = PaddingValues(bottom = AppDimens.bottom_nav_height),
        verticalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
    ) {
        // Header with refresh button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track your sleep patterns and trends",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        // Time period selector
        item {
            TimePeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = statisticsViewModel::selectPeriod
            )
        }

        // Loading state
        if (uiState is StatisticsUiState.Loading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "⏳ Loading statistics...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.card_padding),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error state
        if (uiState is StatisticsUiState.Error) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: ${(uiState as StatisticsUiState.Error).message}",
                        modifier = Modifier.padding(AppDimens.card_padding),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Success state
        if (uiState is StatisticsUiState.Success) {
            val successState = uiState as StatisticsUiState.Success

            // Overview cards
            item {
                OverviewCardsSection(
                    stats = successState.aggregateStats
                )
            }

            // Daily trend chart
            item {
                DailyTrendChart(
                    dailyCounts = successState.dailyCounts
                )
            }

            // Recent sessions list
            item {
                RecentSessionsList(
                    sessions = sessionsList,
                    onSessionClick = onSessionClick
                )
            }
        }

        // Empty state hint
        if (uiState is StatisticsUiState.Success &&
            (uiState as StatisticsUiState.Success).aggregateStats == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.card_padding),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                    ) {
                        Text(
                            text = "No Data Yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Start monitoring from the Home screen to see your sleep statistics here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
