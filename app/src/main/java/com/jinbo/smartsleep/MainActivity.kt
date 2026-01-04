package com.jinbo.smartsleep

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.jinbo.smartsleep.navigation.SmartSleepBottomNav
import com.jinbo.smartsleep.navigation.SmartSleepNavHost
import com.jinbo.smartsleep.ui.theme.SmartSleepTheme
import com.jinbo.smartsleep.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel
    var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        homeViewModel = HomeViewModel()

        setContent {
            SmartSleepTheme {
                SmartSleepApp(homeViewModel = homeViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }
}

@Composable
fun SmartSleepApp(homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Register broadcast receiver for real-time updates
    DisposableEffect(context) {
        val receiver = homeViewModel.registerReceiver(
            context = context,
            onSnoreDetected = { count, amplitude ->
                // Handled in ViewModel
            },
            onAmplitudeUpdate = { amplitude ->
                // Handled in ViewModel
            }
        )

        // Store receiver reference in activity
        val activity = context as? MainActivity
        activity?.broadcastReceiver = receiver

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    Scaffold(
        bottomBar = {
            SmartSleepBottomNav(navController = navController)
        }
    ) { paddingValues ->
        SmartSleepNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            homeViewModel = homeViewModel
        )
    }
}
