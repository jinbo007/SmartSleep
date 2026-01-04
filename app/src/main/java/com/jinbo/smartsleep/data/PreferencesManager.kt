package com.jinbo.smartsleep.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app settings using SharedPreferences
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "smartsleep_prefs"

        // Setting keys
        private const val KEY_SENSITIVITY_LEVEL = "sensitivity_level"
        private const val KEY_MIN_DURATION_MS = "min_duration_ms"

        // Default values
        private const val DEFAULT_SENSITIVITY_LEVEL = 3 // 1-5, 3 is medium
        private const val DEFAULT_MIN_DURATION_MS = 500L // 500ms

        // Sensitivity threshold values (lower = more sensitive)
        val SENSITIVITY_THRESHOLDS = mapOf(
            1 to 400.0,  // Very sensitive
            2 to 600.0,  // Sensitive
            3 to 800.0,  // Medium (default)
            4 to 1000.0, // Less sensitive
            5 to 1200.0  // Least sensitive
        )
    }

    /**
     * Sensitivity level (1-5, where 1 is most sensitive)
     */
    var sensitivityLevel: Int
        get() = prefs.getInt(KEY_SENSITIVITY_LEVEL, DEFAULT_SENSITIVITY_LEVEL)
        set(value) {
            prefs.edit().putInt(KEY_SENSITIVITY_LEVEL, value.coerceIn(1, 5)).apply()
        }

    /**
     * Get the RMS threshold based on current sensitivity level
     */
    fun getRMSThreshold(): Double {
        return SENSITIVITY_THRESHOLDS[sensitivityLevel] ?: SENSITIVITY_THRESHOLDS[DEFAULT_SENSITIVITY_LEVEL]!!
    }

    /**
     * Minimum duration in ms to consider as snore event
     */
    var minDurationMs: Long
        get() = prefs.getLong(KEY_MIN_DURATION_MS, DEFAULT_MIN_DURATION_MS)
        set(value) {
            prefs.edit().putLong(KEY_MIN_DURATION_MS, value).apply()
        }
}
