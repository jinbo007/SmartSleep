package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.ui.theme.AppDimens

/**
 * Statistics Screen - Analytics and trends
 * TODO: Implement in Phase 4 with charts and analytics
 */
@Composable
fun StatisticsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.screen_padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Statistics Screen - Coming Soon!",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
