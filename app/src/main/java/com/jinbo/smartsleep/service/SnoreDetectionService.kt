package com.jinbo.smartsleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.jinbo.smartsleep.MainActivity
import com.jinbo.smartsleep.R
import com.jinbo.smartsleep.audio.AudioRecorder
import com.jinbo.smartsleep.audio.AudioUtils
import com.jinbo.smartsleep.data.PreferencesManager
import com.jinbo.smartsleep.data.SessionManager
import kotlin.math.sqrt

class SnoreDetectionService : Service() {

    companion object {
        const val CHANNEL_ID = "SnoreDetectionChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_SNORE_DETECTED = "com.jinbo.smartsleep.ACTION_SNORE_DETECTED"
        const val ACTION_AMPLITUDE_UPDATE = "com.jinbo.smartsleep.ACTION_AMPLITUDE_UPDATE"
        const val EXTRA_SNORE_COUNT = "extra_snore_count"
        const val EXTRA_AMPLITUDE = "extra_amplitude"

        // Detection parameters
        private const val SAMPLE_RATE = 16000
        const val RMS_THRESHOLD = 800.0 // Default threshold (can be overridden by settings)
        private const val SNORE_FREQ_START = 50.0
        private const val SNORE_FREQ_END = 800.0
        private const val MIN_DURATION_MS = 500L // Default duration (can be overridden by settings)
        private const val ZCR_THRESHOLD = 0.15 // Tightened ZCR to ensure low-freq characteristic

        // Vibration pattern: 0 delay, 1s vib, 0.5s pause, 1s vib, 0.5s pause, 1s vib
        // Total duration = 1000 + 500 + 1000 + 500 + 1000 = 4000ms
        // Add 1s buffer = 5000ms cooldown
        private const val VIBRATION_COOLDOWN_MS = 5000L
    }

    private var audioRecorder: AudioRecorder? = null
    private var isServiceRunning = false
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var preferencesManager: PreferencesManager
    private var vibrator: Vibrator? = null

    // Continuous detection state
    private var consecutiveSnoreStartTime: Long = 0
    private var isTrackingSnore = false
    private var detectionPausedUntil: Long = 0

    // Add startup cooldown to prevent false positives from initialization noise
    private var serviceStartTime: Long = 0
    private val STARTUP_COOLDOWN_MS = 3000L // Ignore detections for first 3 seconds

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sessionManager = SessionManager(this)
        preferencesManager = PreferencesManager(this)
        vibrator = getSystemService(Vibrator::class.java)

        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartSleep::AudioRecordingWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes timeout, will re-acquire if needed or rely on service*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!isServiceRunning) {
            startForegroundService()
            startAudioAnalysis()
            sessionManager.startSession()
            isServiceRunning = true
        }
        
        return START_STICKY
    }

    private fun startForegroundService() {
        val stopIntent = Intent(this, SnoreDetectionService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Snore Detection Active")
            .setContentText("Monitoring your sleep...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startAudioAnalysis() {
        serviceStartTime = System.currentTimeMillis()
        // Initialize detection pause to cover startup cooldown period
        // This prevents any vibration during the startup cooldown
        detectionPausedUntil = serviceStartTime + STARTUP_COOLDOWN_MS

        audioRecorder = AudioRecorder(sampleRate = SAMPLE_RATE)
        audioRecorder?.setCallback(object : AudioRecorder.AudioDataCallback {
            override fun onAudioData(data: ShortArray) {
                // Check for cooldown (vibration in progress) BEFORE processing
                if (System.currentTimeMillis() < detectionPausedUntil) {
                    // During vibration, broadcast 0 amplitude to keep graph clean and prevent self-triggering
                    val ampIntent = Intent(ACTION_AMPLITUDE_UPDATE).apply {
                        putExtra(EXTRA_AMPLITUDE, 0f)
                    }
                    LocalBroadcastManager.getInstance(this@SnoreDetectionService).sendBroadcast(ampIntent)
                    resetSnoreTracking()
                    return
                }
                processAudioData(data)
            }
        })
        audioRecorder?.startRecording()
    }

    private fun processAudioData(data: ShortArray) {
        // Check for startup cooldown FIRST - ignore ALL processing during first 5 seconds
        if (System.currentTimeMillis() - serviceStartTime < STARTUP_COOLDOWN_MS) {
            // Broadcast 0 amplitude during startup to keep graph clean
            val ampIntent = Intent(ACTION_AMPLITUDE_UPDATE).apply {
                putExtra(EXTRA_AMPLITUDE, 0f)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(ampIntent)
            // Also reset snore tracking to prevent immediate trigger after cooldown
            resetSnoreTracking()
            return
        }

        val rms = AudioUtils.calculateRMS(data)

        // Get current settings from preferences
        val rmsThreshold = preferencesManager.getRMSThreshold()
        val minDurationMs = preferencesManager.minDurationMs

        // Broadcast amplitude for graph
        val ampIntent = Intent(ACTION_AMPLITUDE_UPDATE).apply {
            putExtra(EXTRA_AMPLITUDE, rms.toFloat())
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(ampIntent)

        // 1. Basic Silence Threshold
        if (rms < rmsThreshold) {
            resetSnoreTracking()
            return
        }

        // 2. Zero Crossing Rate (ZCR) Check
        // Snoring is low frequency, so ZCR should be relatively low.
        val zcr = AudioUtils.calculateZCR(data)

        // 3. Frequency Analysis (Optional Refinement)
        // Instead of complex ratio, we use ZCR + RMS + Duration as primary heuristics for MVP v2.
        // We still check if there is *some* low frequency energy if needed, but ZCR is a good proxy.

        Log.v("SnoreService", "RMS: $rms, Threshold: $rmsThreshold, ZCR: $zcr")

        // ZCR < 0.15 means < 15% of samples cross zero. For 16kHz, max freq is 8kHz.
        // 0.15 * 8000 = 1200Hz effective "dominant" frequency ceiling roughly.
        if (zcr < ZCR_THRESHOLD) {
            // Matches low frequency characteristic
            if (!isTrackingSnore) {
                isTrackingSnore = true
                consecutiveSnoreStartTime = System.currentTimeMillis()
            } else {
                val duration = System.currentTimeMillis() - consecutiveSnoreStartTime
                if (duration > minDurationMs) {
                    triggerVibration()
                    recordSnoreEvent(rms.toFloat())
                    // Don't reset immediately, allow continuous vibration for long snores
                    // But to avoid spamming, we can reset or debounce.
                    // For now, reset to pulse vibration pattern.
                    resetSnoreTracking()
                }
            }
        } else {
            // High frequency noise (talking, hissing, typing)
            resetSnoreTracking()
        }
    }

    private fun resetSnoreTracking() {
        isTrackingSnore = false
        consecutiveSnoreStartTime = 0
    }

    private fun recordSnoreEvent(intensity: Float) {
        sessionManager.addEvent(intensity)
        val count = sessionManager.getEventCount()
        
        // Notify UI
        val intent = Intent(ACTION_SNORE_DETECTED).apply {
            putExtra(EXTRA_SNORE_COUNT, count)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun triggerVibration() {
        Log.d("SnoreService", "Snore detected! Vibrating...")
        
        // Pause detection for the duration of vibration + buffer
        detectionPausedUntil = System.currentTimeMillis() + VIBRATION_COOLDOWN_MS
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            val effect = VibrationEffect.createWaveform(timings, -1) 
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), -1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Snore Detection Service",
                NotificationManager.IMPORTANCE_LOW // No sound, no vibration
            ).apply {
                enableVibration(false) // Explicitly disable vibration
                setSound(null, null) // No sound
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder?.stopRecording()
        isServiceRunning = false
        // Reset detection state
        resetSnoreTracking()
        detectionPausedUntil = 0
        serviceStartTime = 0
        if (::sessionManager.isInitialized) {
            sessionManager.stopSession()
        }
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
