"package com.example.timelimit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {

    @Query(\"SELECT * FROM app_limits\")
    fun getAllLimits(): Flow<List<AppLimit>>

    @Query(\"SELECT * FROM app_limits\")
    suspend fun getAllLimitsAsList(): List<AppLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLimit(appLimit: AppLimit)

    @Query(\"DELETE FROM app_limits WHERE packageName = :packageName\")
    suspend fun deleteLimit(packageName: String)

    @Query(\"SELECT * FROM app_limits WHERE packageName = :packageName\")
    suspend fun getLimit(packageName: String): AppLimit?

    @Query(\"UPDATE app_limits SET usageTime = :usageTime WHERE packageName = :packageName\")
    suspend fun updateUsageTime(packageName: String, usageTime: Int)
    
    // Yangi optimallashtirilgan funksiyalar
    @Query(\"SELECT packageName FROM app_limits WHERE limitTime > 0\")
    suspend fun getLimitedPackageNames(): List<String>
    
    @Query(\"SELECT * FROM app_limits WHERE limitTime > 0\")
    suspend fun getActiveLimits(): List<AppLimit>
    
    @Query(\"SELECT * FROM app_limits WHERE isBlocked = 1\")
    suspend fun getBlockedApps(): List<AppLimit>

}"