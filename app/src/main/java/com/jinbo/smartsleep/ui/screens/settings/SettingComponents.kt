package com.jinbo.smartsleep.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes

/**
 * Setting item with icon, title, description, and action
 */
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (description != null) 80.dp else 64.dp),
        shape = AppShapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_small
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.card_padding),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            // Title and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action
            action()
        }
    }
}

/**
 * Sensitivity slider with discrete steps
 */
@Composable
fun SensitivitySlider(
    level: Int,
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = getSensitivityLabel(level),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = level.toFloat(),
            onValueChange = { newValue ->
                onLevelChange(newValue.toInt().coerceIn(1, 5))
            },
            valueRange = 1f..5f,
            steps = 3, // 5 levels: 1, 2, 3, 4, 5 (3 steps between)
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Get sensitivity label for display
 */
fun getSensitivityLabel(level: Int): String {
    return when (level) {
        1 -> "Very Sensitive"
        2 -> "Sensitive"
        3 -> "Medium"
        4 -> "Less Sensitive"
        5 -> "Least Sensitive"
        else -> "Medium"
    }
}

/**
 * Duration slider with discrete steps
 */
@Composable
fun DurationSlider(
    durationMs: Long,
    onDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${durationMs}ms",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Min. snore duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = durationMs.toFloat(),
            onValueChange = { newValue ->
                // Snap to 100ms increments
                val snapped = (newValue / 100f).toInt() * 100L
                onDurationChange(snapped.coerceIn(100, 2000))
            },
            valueRange = 100f..2000f,
            steps = 19, // 20 steps: 100, 200, 300... 2000
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
