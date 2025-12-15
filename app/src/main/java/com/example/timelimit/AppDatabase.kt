package com.example.timelimit

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.timelimit.data.AppDao
import com.example.timelimit.data.UsageHistoryDao
import com.example.timelimit.data.Converters
import com.example.timelimit.model.LimitedApp
import com.example.timelimit.model.UsageHistory

@Database(
    entities = [LimitedApp::class, UsageHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun historyDao(): UsageHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_limit_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}