package com.jinbo.smartsleep.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)

/**
 * StatCard - Display statistics with icon, label, value, and optional trend
 *
 * @param icon Icon to display
 * @param label Label text for the statistic
 * @param value Value text to display
 * @param modifier Modifier for the card
 * @param trend Optional trend percentage (positive = up, negative = down)
 * @param color Color for the icon and value
 * @param onClick Optional click handler
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trend: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = AppShapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.card_padding)
        ) {
            // Icon (top left)
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart),
                tint = color.copy(alpha = 0.8f)
            )

            // Trend indicator (top right)
            if (trend != null) {
                val trendIcon = if (trend >= 0)
                    Icons.Default.TrendingUp
                else
                    Icons.Default.TrendingDown

                // More snoring = bad (red), less snoring = good (green)
                val trendColor = if (trend >= 0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary

                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = "Trend",
                        modifier = Modifier.size(20.dp),
                        tint = trendColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (trend >= 0) "+" else ""}${String.format("%.1f", trend)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor
                    )
                }
            }

            // Label (bottom left)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // Value (bottom right)
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                modifier = Modifier.align(Alignment.BottomEnd),
                textAlign = TextAlign.End
            )
        }
    }
}
