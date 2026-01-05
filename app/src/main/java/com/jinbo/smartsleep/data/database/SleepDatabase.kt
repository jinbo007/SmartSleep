package com.jinbo.smartsleep.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for SmartSleep app
 */
@Database(
    entities = [SessionEntity::class, AmplitudeSampleEntity::class, AudioRecordingEntity::class],
    version = 4,
    exportSchema = false
)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun amplitudeSampleDao(): AmplitudeSampleDao
    abstract fun audioRecordingDao(): AudioRecordingDao

    companion object {
        private const val DATABASE_NAME = "smartsleep_database"

        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For MVP, simple migration strategy
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
