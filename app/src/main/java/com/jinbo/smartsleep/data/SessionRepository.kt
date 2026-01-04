package com.jinbo.smartsleep.data

import android.content.Context
import com.jinbo.smartsleep.data.database.AggregateStats
import com.jinbo.smartsleep.data.database.DailyCount
import com.jinbo.smartsleep.data.database.SessionDao
import com.jinbo.smartsleep.data.database.SessionEntity
import com.jinbo.smartsleep.data.database.SleepDatabase
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.math.abs

/**
 * Repository for managing sleep session data
 */
class SessionRepository(context: Context) {

    private val sessionDao: SessionDao = SleepDatabase.getInstance(context).sessionDao()

    companion object {
        // Constants for time period calculations
        private const val DAYS_IN_WEEK = 7
        private const val DAYS_IN_MONTH = 30
        private const val ALL_TIME_YEARS = 100
        private const val MILLIS_IN_MINUTE = 1000 * 60
    }

    /**
     * Save a completed session to database
     */
    suspend fun saveSession(
        startTime: Long,
        endTime: Long,
        snoreCount: Int,
        maxAmplitude: Float
    ): Long {
        val durationMinutes = ((endTime - startTime) / MILLIS_IN_MINUTE).toInt()

        // Calculate start of day for easier date-based queries
        val dateTimestamp = getStartOfDay(startTime)

        val session = SessionEntity(
            startTime = startTime,
            endTime = endTime,
            snoreCount = snoreCount,
            maxAmplitude = maxAmplitude,
            dateTimestamp = dateTimestamp,
            durationMinutes = durationMinutes
        )

        return sessionDao.insertSession(session)
    }

    /**
     * Get all sessions
     */
    fun getAllSessions(): Flow<List<SessionEntity>> {
        return sessionDao.getAllSessions()
    }

    /**
     * Get sessions for a specific time period
     */
    fun getSessionsForPeriod(period: TimePeriod): Flow<List<SessionEntity>> {
        val (startTime, endTime) = getTimeRangeForPeriod(period)
        return sessionDao.getSessionsInRange(startTime, endTime)
    }

    /**
     * Get aggregate statistics for a time period
     */
    suspend fun getStatsForPeriod(period: TimePeriod): AggregateStats? {
        val (startTime, endTime) = getTimeRangeForPeriod(period)
        return sessionDao.getAggregateStatsInRange(startTime, endTime)
    }

    /**
     * Get daily snore counts for the last N days
     */
    suspend fun getDailySnoreCounts(period: TimePeriod): List<DailyCount> {
        val startTime = getStartTimeForPeriod(period)
        val startOfDay = getStartOfDay(startTime)
        return sessionDao.getDailySnoreCounts(startOfDay)
    }

    /**
     * Get the most recent session
     */
    suspend fun getMostRecentSession(): SessionEntity? {
        return sessionDao.getMostRecentSession()
    }

    /**
     * Calculate time range for a given period
     * @return Pair of (startTime, endTime) in milliseconds
     */
    private fun getTimeRangeForPeriod(period: TimePeriod): Pair<Long, Long> {
        val endTime = System.currentTimeMillis()
        val startTime = getStartTimeForPeriod(period)
        return Pair(startTime, endTime)
    }

    /**
     * Calculate start time for a given period
     * @return Start time in milliseconds
     */
    private fun getStartTimeForPeriod(period: TimePeriod): Long {
        val calendar = Calendar.getInstance()

        when (period) {
            TimePeriod.SEVEN_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -DAYS_IN_WEEK)
            TimePeriod.THIRTY_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -DAYS_IN_MONTH)
            TimePeriod.ALL_TIME -> calendar.add(Calendar.YEAR, -ALL_TIME_YEARS)
        }

        return calendar.timeInMillis
    }

    /**
     * Get start of day timestamp for a given time
     * @param timestamp Time in milliseconds
     * @return Start of day timestamp in milliseconds
     */
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

/**
 * Time period enumeration
 */
enum class TimePeriod {
    SEVEN_DAYS,
    THIRTY_DAYS,
    ALL_TIME
}
