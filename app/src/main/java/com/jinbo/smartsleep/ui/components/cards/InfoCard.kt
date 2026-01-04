package com.jinbo.smartsleep.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)

/**
 * InfoCard - General information display card
 *
 * @param icon Icon to display (optional)
 * @param title Title text
 * @param subtitle Subtitle or description text
 * @param modifier Modifier for the card
 * @param backgroundColor Custom background color
 * @param onClick Optional click handler
 */
@Composable
fun InfoCard(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = AppShapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_small
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon (optional)
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
