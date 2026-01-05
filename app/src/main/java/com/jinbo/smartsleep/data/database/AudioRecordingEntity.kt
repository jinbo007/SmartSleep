package com.jinbo.smartsleep.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for audio recording snippets
 * Stores information about recorded audio segments around snore events
 */
@Entity(
    tableName = "audio_recordings",
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
data class AudioRecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Foreign key to the session
    val sessionId: Long,

    // Timestamp of the snore event (center of the recording window)
    val timestamp: Long,

    // File path to the audio recording
    val filePath: String,

    // Duration of this recording in milliseconds
    val durationMs: Long,

    // Amplitude level that triggered this recording
    val triggerAmplitude: Float
)
