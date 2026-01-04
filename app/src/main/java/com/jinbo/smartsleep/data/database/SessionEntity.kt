package com.jinbo.smartsleep.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for sleep monitoring sessions
 */
@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["dateTimestamp"]),
        Index(value = ["startTime", "dateTimestamp"])
    ]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Session timing
    val startTime: Long,
    val endTime: Long,

    // Snore statistics
    val snoreCount: Int,
    val maxAmplitude: Float,

    // Session metadata
    val dateTimestamp: Long, // For easier date-based queries (start of day)
    val durationMinutes: Int // Pre-calculated duration in minutes
)
