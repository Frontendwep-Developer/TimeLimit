package com.example.timelimit.data

import androidx.room.*
import com.example.timelimit.model.UsageHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageHistoryDao {
    @Query("SELECT * FROM usage_history WHERE packageName = :packageName ORDER BY date DESC")
    fun getUsageHistory(packageName: String): Flow<List<UsageHistory>>

    @Query("SELECT * FROM usage_history WHERE packageName = :packageName AND date = :date")
    suspend fun getUsageForDate(packageName: String, date: Long): UsageHistory?

    @Query("SELECT * FROM usage_history WHERE date >= :startDate AND date <= :endDate")
    fun getUsageHistoryBetweenDates(startDate: Long, endDate: Long): Flow<List<UsageHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistory(history: UsageHistory)

    @Update
    suspend fun updateUsageHistory(history: UsageHistory)

    @Query("DELETE FROM usage_history WHERE date < :date")
    suspend fun deleteOldHistory(date: Long)
}

