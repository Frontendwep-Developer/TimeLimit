package com.example.timelimit.data

import androidx.room.*
import com.example.timelimit.model.LimitedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM limited_apps")
    fun getAllLimitedApps(): Flow<List<LimitedApp>>

    @Query("SELECT * FROM limited_apps WHERE packageName = :packageName")
    suspend fun getLimitedApp(packageName: String): LimitedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitedApp(app: LimitedApp)

    @Update
    suspend fun updateLimitedApp(app: LimitedApp)

    @Delete
    suspend fun deleteLimitedApp(app: LimitedApp)

    @Query("DELETE FROM limited_apps WHERE packageName = :packageName")
    suspend fun deleteLimitedAppByPackageName(packageName: String)

    @Query("SELECT * FROM limited_apps WHERE isBlocked = 1")
    fun getBlockedApps(): Flow<List<LimitedApp>>
}

