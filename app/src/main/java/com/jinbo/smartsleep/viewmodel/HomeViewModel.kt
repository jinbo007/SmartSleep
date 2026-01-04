package com.jinbo.smartsleep.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinbo.smartsleep.data.SessionManager
import com.jinbo.smartsleep.service.SnoreDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * ViewModel managing global monitoring state
 * Persists across tab switches and screen rotations
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()

    fun updateMonitoringState(isMonitoring: Boolean) {
        _uiState.value = _uiState.value.copy(isMonitoring = isMonitoring)
    }

    fun updateSnoreCount(count: Int) {
        _uiState.value = _uiState.value.copy(snoreCount = count)
    }

    fun updateAmplitudeData(amplitudes: List<Pair<Long, Float>>) {
        _uiState.value = _uiState.value.copy(amplitudeHistory = amplitudes)
    }

    fun updateMaxAmplitude(amplitude: Float) {
        _uiState.value = _uiState.value.copy(
            maxAmplitude = maxOf(_uiState.value.maxAmplitude, amplitude)
        )
    }

    fun addAmplitudePoint(point: Pair<Long, Float>) {
        val currentList = _uiState.value.amplitudeHistory.toMutableList()
        currentList.add(point)
        // Keep last 100 points
        if (currentList.size > 100) {
            currentList.removeAt(0)
        }
        _uiState.value = _uiState.value.copy(amplitudeHistory = currentList)

        // Update max amplitude
        if (point.second > _uiState.value.maxAmplitude) {
            _uiState.value = _uiState.value.copy(maxAmplitude = point.second)
        }
    }

    fun resetData() {
        _uiState.value = MonitoringUiState()
    }

    /**
     * Start monitoring service
     */
    fun startMonitoring(context: Context) {
        val intent = Intent(context, SnoreDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _uiState.value = _uiState.value.copy(isMonitoring = true)
    }

    /**
     * Stop monitoring service
     */
    fun stopMonitoring(context: Context) {
        val intent = Intent(context, SnoreDetectionService::class.java)
        intent.action = SnoreDetectionService.ACTION_STOP_SERVICE
        context.startService(intent)
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }

    /**
     * Initialize session data
     */
    fun initializeSession(context: Context) {
        viewModelScope.launch {
            val sessionManager = SessionManager(context)
            val count = sessionManager.getEventCount()
            _uiState.value = _uiState.value.copy(
                snoreCount = count,
                maxAmplitude = 0f // Will be updated during monitoring
            )
        }
    }

    /**
     * Register broadcast receiver for real-time updates
     */
    fun registerReceiver(
        context: Context,
        onSnoreDetected: (Int, Float) -> Unit,
        onAmplitudeUpdate: (Float) -> Unit
    ): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    SnoreDetectionService.ACTION_SNORE_DETECTED -> {
                        val count = intent.getIntExtra(SnoreDetectionService.EXTRA_SNORE_COUNT, 0)
                        val amplitude = intent.getFloatExtra(SnoreDetectionService.EXTRA_AMPLITUDE, 0f)
                        updateSnoreCount(count)
                        updateMaxAmplitude(amplitude)
                        onSnoreDetected(count, amplitude)
                    }
                    SnoreDetectionService.ACTION_AMPLITUDE_UPDATE -> {
                        val amp = intent.getFloatExtra(SnoreDetectionService.EXTRA_AMPLITUDE, 0f)
                        addAmplitudePoint(System.currentTimeMillis() to amp)
                        onAmplitudeUpdate(amp)
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(SnoreDetectionService.ACTION_SNORE_DETECTED)
            addAction(SnoreDetectionService.ACTION_AMPLITUDE_UPDATE)
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)

        return receiver
    }
}

/**
 * UI state for monitoring
 */
data class MonitoringUiState(
    val isMonitoring: Boolean = false,
    val snoreCount: Int = 0,
    val maxAmplitude: Float = 0f,
    val amplitudeHistory: List<Pair<Long, Float>> = emptyList()
)
