package com.jinbo.smartsleep.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for audio recordings
 */
@Dao
interface AudioRecordingDao {

    /**
     * Insert a single audio recording
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: AudioRecordingEntity): Long

    /**
     * Get all recordings for a session
     */
    @Query("SELECT * FROM audio_recordings WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRecordingsForSession(sessionId: Long): Flow<List<AudioRecordingEntity>>

    /**
     * Get recording by ID
     */
    @Query("SELECT * FROM audio_recordings WHERE id = :recordingId")
    suspend fun getRecordingById(recordingId: Long): AudioRecordingEntity?

    /**
     * Delete a recording
     */
    @Query("DELETE FROM audio_recordings WHERE id = :recordingId")
    suspend fun deleteRecording(recordingId: Long): Int

    /**
     * Delete all recordings for a session
     */
    @Query("DELETE FROM audio_recordings WHERE sessionId = :sessionId")
    suspend fun deleteRecordingsForSession(sessionId: Long): Int

    /**
     * Delete recording file and database entry
     */
    @Query("SELECT filePath FROM audio_recordings WHERE id = :recordingId")
    suspend fun getFilePathById(recordingId: Long): String?
}
