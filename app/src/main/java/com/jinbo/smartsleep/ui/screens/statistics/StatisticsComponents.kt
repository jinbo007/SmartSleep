package com.jinbo.smartsleep.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.data.SessionRepository
import com.jinbo.smartsleep.data.database.AggregateStats
import com.jinbo.smartsleep.data.database.DailyCount
import com.jinbo.smartsleep.data.database.SessionEntity
import com.jinbo.smartsleep.ui.components.cards.StatCard
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes
import java.text.SimpleDateFormat
import java.util.*

// Import TimePeriod enum
import com.jinbo.smartsleep.data.TimePeriod

// Opt-in for experimental Material API
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Overview cards section showing aggregate statistics
 */
@Composable
fun OverviewCardsSection(
    stats: AggregateStats?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
    ) {
        // Section title
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Stats cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.card_spacing)
        ) {
            // Total Snores Card
            StatCard(
                icon = Icons.Default.ShowChart,
                label = "Total Snores",
                value = stats?.totalSnores?.toString() ?: "0",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )

            // Average Intensity Card
            StatCard(
                icon = Icons.Default.Timer,
                label = "Avg Intensity",
                value = if (stats != null && stats.avgAmplitude > 0) {
                    stats.avgAmplitude.toInt().toString()
                } else {
                    "0"
                },
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Longest session card
        if (stats != null && stats.totalMinutes > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = AppDimens.elevation_small
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.card_padding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
                    ) {
                        Text(
                            text = "Longest Session",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDuration(stats.totalMinutes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${stats.totalSessions} sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Format duration in minutes to readable string
 */
private fun formatDuration(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

/**
 * Time period selector chips
 */
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
    ) {
        TimePeriodChip(
            text = "7 Days",
            selected = selectedPeriod == TimePeriod.SEVEN_DAYS,
            onClick = { onPeriodSelected(TimePeriod.SEVEN_DAYS) }
        )

        TimePeriodChip(
            text = "30 Days",
            selected = selectedPeriod == TimePeriod.THIRTY_DAYS,
            onClick = { onPeriodSelected(TimePeriod.THIRTY_DAYS) }
        )

        TimePeriodChip(
            text = "All Time",
            selected = selectedPeriod == TimePeriod.ALL_TIME,
            onClick = { onPeriodSelected(TimePeriod.ALL_TIME) }
        )
    }
}

/**
 * Individual time period chip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = AppShapes.medium
    )
}

/**
 * Daily snore count trend chart (simplified bar chart using Canvas)
 */
@Composable
fun DailyTrendChart(
    dailyCounts: List<DailyCount>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = AppShapes.medium,
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
                text = "Daily Snore Counts",
                style = MaterialTheme.typography.titleMedium
            )

            if (dailyCounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Simple bar visualization
                val maxCount = dailyCounts.maxOfOrNull { it.dailyCount } ?: 1

                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                ) {
                    dailyCounts.forEach { dailyCount ->
                        DailyCountBar(
                            date = dailyCount.dateTimestamp,
                            count = dailyCount.dailyCount,
                            maxCount = maxCount
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single daily count bar
 */
@Composable
private fun DailyCountBar(
    date: Long,
    count: Int,
    maxCount: Int
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
    ) {
        Text(
            text = dateFormat.format(Date(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )

        // Bar background
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // Bar fill
            Box(
                modifier = Modifier
                    .fillMaxWidth((count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * Recent sessions list
 */
@Composable
fun RecentSessionsList(
    sessions: List<SessionEntity>,
    onSessionClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = AppShapes.medium,
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
                text = "Recent Sessions",
                style = MaterialTheme.typography.titleMedium
            )

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                ) {
                    sessions.take(10).forEach { session ->
                        SessionListItem(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single session item in the list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionListItem(
    session: SessionEntity,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = AppShapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.spacing_2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
            ) {
                Text(
                    text = dateFormat.format(Date(session.startTime)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(session.durationMinutes.toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_3)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${session.snoreCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "snores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
                ) {
                    Text(
                        text = session.maxAmplitude.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "max",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
