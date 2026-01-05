package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.data.database.SessionEntity
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.viewmodel.HomeViewModel
import com.jinbo.smartsleep.viewmodel.HistoryViewModel
import com.jinbo.smartsleep.viewmodel.HistoryUiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * History Screen - Displays historical sleep sessions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    homeViewModel: HomeViewModel? = null,
    onSessionClick: (Long) -> Unit = {},
    historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.provideFactory(
            SessionRepository(LocalContext.current.applicationContext)
        )
    )
) {
    val historyUiState by historyViewModel.uiState.collectAsState()
    val liveUiState by homeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(com.jinbo.smartsleep.viewmodel.MonitoringUiState())
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
                    text = "View your past sleep monitoring sessions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Monitoring Status
        if (liveUiState.isMonitoring) {
            item {
                MonitoringStatusCard(liveUiState = liveUiState)
            }
        }

        // History Content
        when (val state = historyUiState) {
            is HistoryUiState.Loading -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDimens.card_padding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
                        ) {
                            Text(
                                text = "⏳",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                text = "Loading history...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is HistoryUiState.Success -> {
                if (state.sessions.isEmpty()) {
                    item {
                        EmptyHistoryCard()
                    }
                } else {
                    items(state.sessions) { session ->
                        HistorySessionCard(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                }
            }
            is HistoryUiState.Error -> {
                item {
                    ErrorCard(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun MonitoringStatusCard(
    liveUiState: com.jinbo.smartsleep.viewmodel.MonitoringUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
        ) {
            Text(
                text = "🔴",
                style = MaterialTheme.typography.titleLarge
            )
            Column {
                Text(
                    text = "Monitoring Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Snore count: ${liveUiState.snoreCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Live",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = AppDimens.spacing_2)
                .then(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySessionCard(
    session: SessionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_small
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
        ) {
            // Date header
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
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formatDate(session.startTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = formatTime(session.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // Session details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SessionStat(
                    label = "Duration",
                    value = "${session.durationMinutes}m"
                )
                SessionStat(
                    label = "Snores",
                    value = session.snoreCount.toString(),
                    highlight = session.snoreCount > 0
                )
                SessionStat(
                    label = "Max Amp",
                    value = String.format("%.0f", session.maxAmplitude)
                )
            }

            // Quality indicator
            val snoreRate = if (session.durationMinutes > 0) {
                session.snoreCount.toFloat() / session.durationMinutes
            } else {
                0f
            }
            val quality = when {
                snoreRate < 1 -> "Excellent"
                snoreRate < 3 -> "Good"
                snoreRate < 5 -> "Fair"
                else -> "Poor"
            }
            val qualityColor = when {
                snoreRate < 1 -> MaterialTheme.colorScheme.primary
                snoreRate < 3 -> MaterialTheme.colorScheme.secondary
                snoreRate < 5 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sleep Quality",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = quality,
                    style = MaterialTheme.typography.labelLarge,
                    color = qualityColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SessionStat(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.card_padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No Sleep History Yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start monitoring from the Home screen to track your sleep",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppDimens.spacing_2))
            Text(
                text = "💡 Tip: Keep your phone near your pillow while sleeping",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Unable to load history",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
