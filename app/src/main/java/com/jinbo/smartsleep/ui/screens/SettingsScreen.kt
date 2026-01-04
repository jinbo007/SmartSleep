package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.data.PreferencesManager
import com.jinbo.smartsleep.ui.screens.settings.DurationSlider
import com.jinbo.smartsleep.ui.screens.settings.SensitivitySlider
import com.jinbo.smartsleep.ui.screens.settings.SettingItem
import com.jinbo.smartsleep.ui.theme.AppDimens

/**
 * Settings Screen - App configuration
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }

    // State for settings
    var sensitivityLevel by remember {
        mutableStateOf(prefsManager.sensitivityLevel)
    }
    var minDurationMs by remember {
        mutableStateOf(prefsManager.minDurationMs)
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
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize your monitoring experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sensitivity Setting
        item {
            SettingItem(
                icon = Icons.Default.Equalizer,
                title = "Sensitivity",
                description = "Adjust detection sensitivity (affects threshold)",
                action = {
                    // Don't show anything here, show slider below
                }
            )
        }

        // Sensitivity Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                SensitivitySlider(
                    level = sensitivityLevel,
                    onLevelChange = { newLevel ->
                        sensitivityLevel = newLevel
                        prefsManager.sensitivityLevel = newLevel
                    },
                    modifier = Modifier.padding(AppDimens.card_padding)
                )
            }
        }

        // Duration Setting
        item {
            SettingItem(
                icon = Icons.Default.Timer,
                title = "Min Duration",
                description = "Minimum duration to trigger detection",
                action = {
                    // Don't show anything here, show slider below
                }
            )
        }

        // Duration Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                DurationSlider(
                    durationMs = minDurationMs,
                    onDurationChange = { newDuration ->
                        minDurationMs = newDuration
                        prefsManager.minDurationMs = newDuration
                    },
                    modifier = Modifier.padding(AppDimens.card_padding)
                )
            }
        }

        // Info card
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
                        text = "About Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Sensitivity: Lower level = more sensitive (triggers on quieter sounds)\n" +
                              "• Min Duration: How long sound must last to be considered snoring\n" +
                              "• Changes take effect on next monitoring session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
