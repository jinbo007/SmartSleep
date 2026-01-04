package com.jinbo.smartsleep.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.service.SnoreDetectionService
import com.jinbo.smartsleep.ui.AmplitudeGraph
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes

/**
 * MonitoringCard - Displays real-time monitoring status and amplitude graph
 *
 * @param isMonitoring Whether monitoring is active
 * @param amplitudeHistory List of amplitude readings
 * @param snoreCount Current snore count
 * @param modifier Modifier
 */
@Composable
fun MonitoringCard(
    isMonitoring: Boolean,
    amplitudeHistory: List<Pair<Long, Float>>,
    snoreCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isMonitoring) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding)
        ) {
            // Header with icon and status
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
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = "Monitoring Status",
                        tint = if (isMonitoring) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    )

                    Column {
                        Text(
                            text = "Sleep Monitor",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = if (isMonitoring) "Active" else "Inactive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMonitoring) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Status indicator with pulse animation
                if (isMonitoring) {
                    MonitoringStatusIndicator()
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.spacing_4))

            // Amplitude graph
            if (isMonitoring && amplitudeHistory.isNotEmpty()) {
                AmplitudeGraph(
                    amplitudes = amplitudeHistory,
                    threshold = SnoreDetectionService.RMS_THRESHOLD.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                // Placeholder when not monitoring
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(AppDimens.corner_radius_medium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start monitoring to see live data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Animated pulse indicator for active monitoring
 */
@Composable
private fun MonitoringStatusIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.5f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(8.dp * scale)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}
