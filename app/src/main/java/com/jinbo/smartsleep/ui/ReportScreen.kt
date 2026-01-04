package com.jinbo.smartsleep.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.data.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val session = sessionManager.getLastSession()
    
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Last Session Report", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Start: ${dateFormat.format(Date(session.startTime))}")
        Text("End: ${dateFormat.format(Date(session.endTime))}")
        
        val duration = if (session.endTime > session.startTime) (session.endTime - session.startTime) / 1000 else 0
        Text("Duration: ${duration}s")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Snore Events: ${session.eventCount}", style = MaterialTheme.typography.titleLarge)
        Text("Max Intensity: ${String.format("%.2f", session.maxAmplitude)}", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
