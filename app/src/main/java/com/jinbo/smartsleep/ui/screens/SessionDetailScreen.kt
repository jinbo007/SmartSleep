package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.data.database.SessionEntity
import com.jinbo.smartsleep.ui.components.cards.StatCard
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.viewmodel.SessionDetailViewModel
import com.jinbo.smartsleep.viewmodel.SessionDetailUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session Detail Screen - Individual session analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onBack: () -> Unit = {},
    viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModel.provideFactory(
            SessionRepository(LocalContext.current.applicationContext)
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SessionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
                    ) {
                        Text(
                            text = "⏳",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Loading session details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is SessionDetailUiState.Success -> {
                val session = state.session
                SessionDetailContent(
                    session = session,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is SessionDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionDetailContent(
    session: SessionEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.screen_padding),
        verticalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
    ) {
        // Session Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.card_padding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
            ) {
                Text(
                    text = "Session Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                SessionInfoRow(
                    label = "Date",
                    value = formatDate(session.startTime)
                )
                SessionInfoRow(
                    label = "Start Time",
                    value = formatTime(session.startTime)
                )
                SessionInfoRow(
                    label = "End Time",
                    value = formatTime(session.endTime)
                )
                SessionInfoRow(
                    label = "Duration",
                    value = "${session.durationMinutes} minutes"
                )
            }
        }

        // Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
        ) {
            StatCard(
                icon = Icons.Default.Mic,
                label = "Snore Count",
                value = session.snoreCount.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            StatCard(
                icon = Icons.Default.ShowChart,
                label = "Max Amplitude",
                value = String.format("%.1f", session.maxAmplitude),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Detailed Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.card_padding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
            ) {
                Text(
                    text = "Detailed Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Snore Rate
                val snoreRate = if (session.durationMinutes > 0) {
                    session.snoreCount.toFloat() / session.durationMinutes
                } else {
                    0f
                }
                SessionInfoRow(
                    label = "Snore Rate",
                    value = String.format("%.1f snores/minute", snoreRate)
                )

                // Average Amplitude (estimated)
                val avgAmplitude = session.maxAmplitude / 2
                SessionInfoRow(
                    label = "Avg Amplitude",
                    value = String.format("%.1f", avgAmplitude)
                )

                // Session Quality
                val quality = when {
                    snoreRate < 1 -> "Excellent"
                    snoreRate < 3 -> "Good"
                    snoreRate < 5 -> "Fair"
                    else -> "Poor"
                }
                val qualityColor = when {
                    snoreRate < 1 -> MaterialTheme.colorScheme.primary
                    snoreRate < 3 -> MaterialTheme.colorScheme.primaryContainer
                    snoreRate < 5 -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sleep Quality",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = quality,
                        style = MaterialTheme.typography.bodyLarge,
                        color = qualityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Session ID Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.card_padding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
            ) {
                Text(
                    text = "Session ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "#${session.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(AppDimens.bottom_nav_height))
    }
}

@Composable
private fun SessionInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
