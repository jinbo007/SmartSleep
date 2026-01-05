package com.jinbo.smartsleep.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for amplitude samples during a session
 * Stores the audio amplitude data points for visualization and playback
 */
@Entity(
    tableName = "amplitude_samples",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AmplitudeSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Foreign key to the session
    val sessionId: Long,

    // Timestamp of this sample (relative to session start)
    val timestamp: Long,

    // Amplitude value
    val amplitude: Float,

    // Whether this sample was detected as a snore event
    val isSnore: Boolean
)
