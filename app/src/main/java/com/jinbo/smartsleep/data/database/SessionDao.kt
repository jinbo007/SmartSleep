package com.jinbo.smartsleep.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for sessions
 */
@Dao
interface SessionDao {

    /**
     * Insert a new session
     */
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    /**
     * Update an existing session
     */
    @Update
    suspend fun updateSession(session: SessionEntity)

    /**
     * Get all sessions, ordered by start time (newest first)
     */
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    /**
     * Get sessions within a date range
     */
    @Query("SELECT * FROM sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SessionEntity>>

    /**
     * Get sessions from last N days
     */
    @Query("SELECT * FROM sessions WHERE dateTimestamp >= :startOfDay ORDER BY startTime DESC")
    fun getSessionsFromLastNDays(startOfDay: Long): Flow<List<SessionEntity>>

    /**
     * Get aggregate statistics for all time
     */
    @Query("""
        SELECT
            COUNT(*) as totalSessions,
            SUM(snoreCount) as totalSnores,
            AVG(maxAmplitude) as avgAmplitude,
            MAX(maxAmplitude) as maxAmplitude,
            SUM(durationMinutes) as totalMinutes
        FROM sessions
    """)
    suspend fun getAggregateStats(): AggregateStats?

    /**
     * Get aggregate statistics for date range
     */
    @Query("""
        SELECT
            COUNT(*) as totalSessions,
            SUM(snoreCount) as totalSnores,
            AVG(maxAmplitude) as avgAmplitude,
            MAX(maxAmplitude) as maxAmplitude,
            SUM(durationMinutes) as totalMinutes
        FROM sessions
        WHERE startTime >= :startTime AND startTime <= :endTime
    """)
    suspend fun getAggregateStatsInRange(startTime: Long, endTime: Long): AggregateStats?

    /**
     * Get daily snore counts for the last N days
     */
    @Query("""
        SELECT dateTimestamp, SUM(snoreCount) as dailyCount
        FROM sessions
        WHERE dateTimestamp >= :startOfDay
        GROUP BY dateTimestamp
        ORDER BY dateTimestamp ASC
    """)
    suspend fun getDailySnoreCounts(startOfDay: Long): List<DailyCount>

    /**
     * Get the most recent session
     */
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getMostRecentSession(): SessionEntity?

    /**
     * Get a specific session by ID
     */
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?

    /**
     * Delete old sessions (older than specified timestamp)
     */
    @Query("DELETE FROM sessions WHERE startTime < :timestamp")
    suspend fun deleteOldSessions(timestamp: Long): Int
}

/**
 * Aggregate statistics data class
 */
data class AggregateStats(
    val totalSessions: Int,
    val totalSnores: Int,
    val avgAmplitude: Float,
    val maxAmplitude: Float,
    val totalMinutes: Long
)

/**
 * Daily count data class
 */
data class DailyCount(
    val dateTimestamp: Long,
    val dailyCount: Int
)
