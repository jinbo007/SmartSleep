package com.jinbo.smartsleep.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for amplitude samples
 */
@Dao
interface AmplitudeSampleDao {

    /**
     * Insert a single amplitude sample
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: AmplitudeSampleEntity): Long

    /**
     * Insert multiple amplitude samples in batch
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<AmplitudeSampleEntity>)

    /**
     * Get all samples for a session, ordered by timestamp
     */
    @Query("SELECT * FROM amplitude_samples WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSamplesForSession(sessionId: Long): Flow<List<AmplitudeSampleEntity>>

    /**
     * Get samples for a session within a time range
     */
    @Query("SELECT * FROM amplitude_samples WHERE sessionId = :sessionId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getSamplesInRange(sessionId: Long, startTime: Long, endTime: Long): List<AmplitudeSampleEntity>

    /**
     * Get all snore events for a session
     */
    @Query("SELECT * FROM amplitude_samples WHERE sessionId = :sessionId AND isSnore = 1 ORDER BY timestamp ASC")
    suspend fun getSnoreEventsForSession(sessionId: Long): List<AmplitudeSampleEntity>

    /**
     * Delete all samples for a session
     */
    @Query("DELETE FROM amplitude_samples WHERE sessionId = :sessionId")
    suspend fun deleteSamplesForSession(sessionId: Long): Int

    /**
     * Delete old samples (cascade delete will handle this when session is deleted)
     */
    @Query("DELETE FROM amplitude_samples WHERE timestamp < :oldTimestamp")
    suspend fun deleteOldSamples(oldTimestamp: Long): Int
}
