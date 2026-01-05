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
import com.jinbo.smartsleep.audio.AudioManager
import com.jinbo.smartsleep.audio.AudioRecorder
import com.jinbo.smartsleep.audio.AudioUtils
import com.jinbo.smartsleep.data.PreferencesManager
import com.jinbo.smartsleep.data.SessionManager
import com.jinbo.smartsleep.data.database.AmplitudeSampleEntity
import com.jinbo.smartsleep.data.database.AudioRecordingEntity
import com.jinbo.smartsleep.data.database.SleepDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private var audioManager: AudioManager? = null
    private var isServiceRunning = false
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: SleepDatabase
    private var vibrator: Vibrator? = null

    // Continuous detection state
    private var consecutiveSnoreStartTime: Long = 0
    private var isTrackingSnore = false
    private var detectionPausedUntil: Long = 0

    // Data recording state
    private var currentSessionId: Long = 0
    private var sessionStartTime: Long = 0
    private val amplitudeSamples = mutableListOf<AmplitudeSampleEntity>()
    private var lastSampleTime: Long = 0
    private val SAMPLE_INTERVAL_MS = 100L // Save sample every 100ms

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sessionManager = SessionManager(this)
        preferencesManager = PreferencesManager(this)
        database = SleepDatabase.getInstance(this)
        audioManager = AudioManager(this)
        vibrator = getSystemService(Vibrator::class.java)

        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartSleep::AudioRecordingWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes timeout*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!isServiceRunning) {
            startForegroundService()
            sessionStartTime = System.currentTimeMillis()

            // Start continuous audio recording for buffer
            audioManager?.startRecording()

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
        val rms = AudioUtils.calculateRMS(data)

        // Get current settings from preferences
        val rmsThreshold = preferencesManager.getRMSThreshold()
        val minDurationMs = preferencesManager.minDurationMs

        // Broadcast amplitude for graph
        val ampIntent = Intent(ACTION_AMPLITUDE_UPDATE).apply {
            putExtra(EXTRA_AMPLITUDE, rms.toFloat())
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(ampIntent)

        // Save amplitude sample to database (throttled to every SAMPLE_INTERVAL_MS)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSampleTime >= SAMPLE_INTERVAL_MS) {
            lastSampleTime = currentTime
            val relativeTime = currentTime - sessionStartTime

            // Check if this is a snore event (above threshold)
            val isSnore = rms >= rmsThreshold

            amplitudeSamples.add(
                AmplitudeSampleEntity(
                    sessionId = currentSessionId,
                    timestamp = relativeTime,
                    amplitude = rms.toFloat(),
                    isSnore = isSnore
                )
            )

            // Periodically flush samples to database (every 50 samples = 5 seconds)
            if (amplitudeSamples.size >= 50) {
                flushAmplitudeSamples()
            }
        }

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

        val currentTime = System.currentTimeMillis()
        val relativeTime = currentTime - sessionStartTime

        // Save audio recording for this snore event
        saveAudioRecording(relativeTime)

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

    /**
     * Save audio recording snippet around snore event
     */
    private fun saveAudioRecording(snoreTimestamp: Long) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // Stop current recording and save the snippet
                val result = audioManager?.stopRecording(snoreTimestamp)

                if (result != null && currentSessionId > 0) {
                    val (filePath, durationMs) = result

                    // Get the amplitude that triggered this recording
                    val triggerAmplitude = amplitudeSamples.lastOrNull { it.timestamp == snoreTimestamp }?.amplitude ?: 0f

                    val recording = AudioRecordingEntity(
                        sessionId = currentSessionId,
                        timestamp = snoreTimestamp,
                        filePath = filePath,
                        durationMs = durationMs,
                        triggerAmplitude = triggerAmplitude
                    )

                    database.audioRecordingDao().insertRecording(recording)
                    Log.d("SnoreService", "Saved audio recording: $filePath")
                }

                // Start a new recording for the next event
                audioManager?.startRecording()
            } catch (e: Exception) {
                Log.e("SnoreService", "Failed to save audio recording", e)
                // Restart recording anyway
                audioManager?.startRecording()
            }
        }
    }

    /**
     * Flush accumulated amplitude samples to database
     */
    private fun flushAmplitudeSamples() {
        if (amplitudeSamples.isEmpty() || currentSessionId <= 0) return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                database.amplitudeSampleDao().insertSamples(amplitudeSamples.toList())
                Log.d("SnoreService", "Flushed ${amplitudeSamples.size} amplitude samples to database")
                amplitudeSamples.clear()
            } catch (e: Exception) {
                Log.e("SnoreService", "Failed to flush amplitude samples", e)
            }
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

        // Stop audio recording
        audioManager?.cancelRecording()
        audioManager?.release()

        // Flush any remaining amplitude samples
        flushAmplitudeSamples()

        audioRecorder?.stopRecording()
        isServiceRunning = false

        // Reset detection state
        resetSnoreTracking()
        detectionPausedUntil = 0

        if (::sessionManager.isInitialized) {
            // Stop session in coroutine to handle suspend function
            CoroutineScope(Dispatchers.IO).launch {
                sessionManager.stopSession()
            }
        }

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
