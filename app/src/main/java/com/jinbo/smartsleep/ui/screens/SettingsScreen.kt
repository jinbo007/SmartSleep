package com.jinbo.smartsleep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.data.PreferencesManager
import com.jinbo.smartsleep.ui.screens.settings.DurationSlider
import com.jinbo.smartsleep.ui.screens.settings.SensitivitySlider
import com.jinbo.smartsleep.ui.screens.settings.SettingItem
import com.jinbo.smartsleep.ui.theme.AppDimens
import java.text.SimpleDateFormat
import java.util.*

/**
 * Settings Screen - App configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
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

        // Data Management Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
            ) {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppDimens.spacing_1)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.card_padding),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
                    ) {
                        Text(
                            text = "Your sleep data is stored locally on this device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.padding(vertical = AppDimens.spacing_2))

                        SettingItem(
                            icon = Icons.Default.CloudUpload,
                            title = "Export Data",
                            description = "Export your sleep data (coming soon)",
                            action = {
                                Text(
                                    text = "Soon",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }

        // App Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.card_padding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                ) {
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "About SmartSleep",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
                    ) {
                        InfoRow(label = "Version", value = "1.0.0")
                        InfoRow(label = "Build", value = "Debug")
                        InfoRow(label = "Platform", value = "Android")
                    }

                    Spacer(modifier = Modifier.height(AppDimens.spacing_2))

                    Text(
                        text = "SmartSleep helps you track and improve your sleep quality by monitoring snoring patterns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tips Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.card_padding),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2)
                    ) {
                        Text(
                            text = "💡",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Tips for Better Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing_1)
                    ) {
                        TipItem(text = "Place your phone near your pillow with microphone facing you")
                        TipItem(text = "Keep the phone charging overnight to avoid battery drain")
                        TipItem(text = "Adjust sensitivity based on your environment and snoring level")
                        TipItem(text = "Check statistics regularly to track your progress")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing_2),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
