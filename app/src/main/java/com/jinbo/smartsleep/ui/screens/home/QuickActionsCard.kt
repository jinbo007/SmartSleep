package com.jinbo.smartsleep.ui.screens.home

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jinbo.smartsleep.service.SnoreDetectionService
import com.jinbo.smartsleep.ui.components.buttons.ActionButton
import com.jinbo.smartsleep.ui.theme.AppDimens
import com.jinbo.smartsleep.ui.theme.AppShapes

/**
 * QuickActionsCard - Start/Stop monitoring controls
 *
 * @param isMonitoring Whether monitoring is active
 * @param onStartMonitoring Callback to start monitoring
 * @param onStopMonitoring Callback to stop monitoring
 * @param modifier Modifier
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QuickActionsCard(
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimens.elevation_medium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.card_padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isMonitoring) {
                // Stop Monitoring Button
                ActionButton(
                    text = "Stop Monitoring",
                    onClick = onStopMonitoring,
                    icon = Icons.Default.Stop,
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = true
                )
            } else {
                // Start Monitoring Button (with permissions)
                StartMonitoringButton(
                    onStartMonitoring = onStartMonitoring
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun StartMonitoringButton(
    onStartMonitoring: () -> Unit
) {
    val permissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    ActionButton(
        text = if (permissionState.allPermissionsGranted) {
            "Start Sleep Monitoring"
        } else {
            "Grant Permissions"
        },
        onClick = {
            if (permissionState.allPermissionsGranted) {
                onStartMonitoring()
            } else {
                permissionState.launchMultiplePermissionRequest()
            }
        },
        icon = if (permissionState.allPermissionsGranted) {
            Icons.Default.MonitorHeart
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (!permissionState.allPermissionsGranted) {
        Spacer(modifier = Modifier.height(AppDimens.spacing_2))
        Text(
            text = "Microphone permission required for snore detection",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
