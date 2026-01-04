package com.jinbo.smartsleep.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages sleep monitoring sessions with persistent storage
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_sleep_prefs", Context.MODE_PRIVATE)
    private val repository = SessionRepository(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_EVENT_COUNT = "event_count"
        private const val KEY_MAX_AMPLITUDE = "max_amplitude"
        private const val KEY_SESSION_ACTIVE = "session_active"
    }

    /**
     * Start a new monitoring session
     */
    fun startSession(): Boolean {
        return try {
            prefs.edit()
                .putLong(KEY_START_TIME, System.currentTimeMillis())
                .putInt(KEY_EVENT_COUNT, 0)
                .putFloat(KEY_MAX_AMPLITUDE, 0f)
                .putBoolean(KEY_SESSION_ACTIVE, true)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Stop current session and save to database
     */
    suspend fun stopSession(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = prefs.getLong(KEY_START_TIME, 0)
                val eventCount = prefs.getInt(KEY_EVENT_COUNT, 0)
                val maxAmplitude = prefs.getFloat(KEY_MAX_AMPLITUDE, 0f)

                // Validate session data
                if (startTime <= 0) {
                    return@withContext Result.failure(IllegalStateException("No active session found"))
                }

                if (endTime <= startTime) {
                    return@withContext Result.failure(IllegalStateException("Invalid session time range"))
                }

                // Save to database
                repository.saveSession(
                    startTime = startTime,
                    endTime = endTime,
                    snoreCount = eventCount,
                    maxAmplitude = maxAmplitude
                )

                // Mark session as inactive
                prefs.edit()
                    .putBoolean(KEY_SESSION_ACTIVE, false)
                    .putLong(KEY_END_TIME, endTime)
                    .apply()

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Add a snore event to the current session
     */
    fun addEvent(amplitude: Float): Boolean {
        return try {
            if (!isSessionActive()) {
                return false
            }

            val currentCount = prefs.getInt(KEY_EVENT_COUNT, 0)
            val currentMax = prefs.getFloat(KEY_MAX_AMPLITUDE, 0f)

            prefs.edit()
                .putInt(KEY_EVENT_COUNT, currentCount + 1)
                .putFloat(KEY_MAX_AMPLITUDE, if (amplitude > currentMax) amplitude else currentMax)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get current event count
     */
    fun getEventCount(): Int {
        return try {
            prefs.getInt(KEY_EVENT_COUNT, 0)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get current max amplitude
     */
    fun getMaxAmplitude(): Float {
        return try {
            prefs.getFloat(KEY_MAX_AMPLITUDE, 0f)
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Check if a session is currently active
     */
    fun isSessionActive(): Boolean {
        return try {
            prefs.getBoolean(KEY_SESSION_ACTIVE, false) &&
                    prefs.getLong(KEY_START_TIME, 0) > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the last completed session data
     */
    fun getLastSession(): SessionData {
        return try {
            SessionData(
                startTime = prefs.getLong(KEY_START_TIME, 0),
                endTime = prefs.getLong(KEY_END_TIME, 0),
                eventCount = prefs.getInt(KEY_EVENT_COUNT, 0),
                maxAmplitude = prefs.getFloat(KEY_MAX_AMPLITUDE, 0f)
            )
        } catch (e: Exception) {
            SessionData(0, 0, 0, 0f)
        }
    }

    /**
     * Reset current session (useful for cleanup)
     */
    fun resetSession() {
        try {
            prefs.edit()
                .remove(KEY_START_TIME)
                .remove(KEY_EVENT_COUNT)
                .remove(KEY_MAX_AMPLITUDE)
                .putBoolean(KEY_SESSION_ACTIVE, false)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Data class representing a session
 */
data class SessionData(
    val startTime: Long,
    val endTime: Long,
    val eventCount: Int,
    val maxAmplitude: Float
) {
    /**
     * Check if this session has valid data
     */
    fun isValid(): Boolean {
        return startTime > 0 && endTime > startTime
    }

    /**
     * Get session duration in minutes
     */
    fun getDurationMinutes(): Long {
        return if (endTime > startTime) {
            (endTime - startTime) / 1000 / 60
        } else {
            0
        }
    }
}
