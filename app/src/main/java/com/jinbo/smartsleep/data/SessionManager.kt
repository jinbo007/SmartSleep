package com.jinbo.smartsleep.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_sleep_prefs", Context.MODE_PRIVATE)

    fun startSession() {
        prefs.edit()
            .putLong("start_time", System.currentTimeMillis())
            .putInt("event_count", 0)
            .putFloat("max_amplitude", 0f)
            .apply()
    }

    fun stopSession() {
        prefs.edit()
            .putLong("end_time", System.currentTimeMillis())
            .apply()
    }

    fun addEvent(amplitude: Float) {
        val currentCount = prefs.getInt("event_count", 0)
        val currentMax = prefs.getFloat("max_amplitude", 0f)
        
        prefs.edit()
            .putInt("event_count", currentCount + 1)
            .putFloat("max_amplitude", if (amplitude > currentMax) amplitude else currentMax)
            .apply()
    }
    
    fun getEventCount(): Int {
        return prefs.getInt("event_count", 0)
    }

    fun getLastSession(): SessionData {
        return SessionData(
            startTime = prefs.getLong("start_time", 0),
            endTime = prefs.getLong("end_time", 0),
            eventCount = prefs.getInt("event_count", 0),
            maxAmplitude = prefs.getFloat("max_amplitude", 0f)
        )
    }
}

data class SessionData(
    val startTime: Long,
    val endTime: Long,
    val eventCount: Int,
    val maxAmplitude: Float
)
