package com.example.timelimit

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.timelimit.data.AppLimit
import com.example.timelimit.data.AppLimitDao
import com.example.timelimit.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val appLimitDao: AppLimitDao

    private val _appsList = MutableLiveData<List<AppInfo>>()
    val appsList: LiveData<List<AppInfo>> = _appsList

    private val _totalUsageTimeFormatted = MutableLiveData<String>()
    val totalUsageTimeFormatted: LiveData<String> = _totalUsageTimeFormatted

    private val _mostUsedApp = MutableLiveData<AppInfo?>()
    val mostUsedApp: LiveData<AppInfo?> = _mostUsedApp

    private val _blockedApps = MutableLiveData<List<AppInfo>>()
    val blockedApps: LiveData<List<AppInfo>> = _blockedApps

    init {
        val database = AppDatabase.getDatabase(application)
        appLimitDao = database.appLimitDao()

        viewModelScope.launch {
            appLimitDao.getAllLimits().distinctUntilChanged().collect { dbLimits ->
                updateFullAppList(dbLimits)
            }
        }
    }

    fun forceUpdate() {
        viewModelScope.launch {
            val currentLimits = appLimitDao.getAllLimitsAsList()
            updateFullAppList(currentLimits)
        }
    }

    private suspend fun updateFullAppList(dbLimits: List<AppLimit>) {
        withContext(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val packages = pm.queryIntentActivities(mainIntent, 0)

            val allApps = packages.map { resolveInfo ->
                val dbLimit = dbLimits.find { it.packageName == resolveInfo.activityInfo.packageName }
                AppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm),
                    category = "",
                    isLimited = dbLimit != null,
                    usageTime = dbLimit?.usageTime ?: 0,
                    limitTime = dbLimit?.limitTime ?: 0,
                    isBlocked = dbLimit?.isBlocked ?: false
                )
            }

            _appsList.postValue(sortApps(allApps))

            val limitedApps = allApps.filter { it.isLimited }
            val totalUsageSeconds = limitedApps.sumOf { it.usageTime }
            val hours = totalUsageSeconds / 3600
            val minutes = (totalUsageSeconds % 3600) / 60
            _totalUsageTimeFormatted.postValue("${hours} soat ${minutes} daq")

            _mostUsedApp.postValue(limitedApps.maxByOrNull { it.usageTime })

            _blockedApps.postValue(limitedApps.filter { it.isBlocked })
        }
    }

    fun setAppLimit(packageName: String, limitInMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newLimit = AppLimit(
                packageName = packageName,
                limitTime = limitInMinutes,
                usageTime = 0, // **THE FIX IS HERE: Reset usage time to 0**
                isBlocked = false,
                lastReset = System.currentTimeMillis(),
                usageOffset = 0 // No longer needed, but kept for DB schema compatibility
            )
            appLimitDao.insertOrUpdateLimit(newLimit)
            forceUpdate()
        }
    }

    fun removeAppLimit(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appLimitDao.deleteLimit(packageName)
            forceUpdate()
        }
    }

    private fun sortApps(apps: List<AppInfo>): List<AppInfo> {
        return apps.sortedWith(
            compareByDescending<AppInfo> { it.isBlocked }
                .thenByDescending { it.isLimited }
                .thenBy { it.appName }
        )
    }
}