package com.example.timelimit

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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

    private val appLimitDao: AppLimitDao = AppDatabase.getDatabase(application).appLimitDao()

    private val _appsList = MutableLiveData<List<AppInfo>>()
    val appsList: LiveData<List<AppInfo>> = _appsList

    private val _totalUsageTimeFormatted = MutableLiveData<String>()
    val totalUsageTimeFormatted: LiveData<String> = _totalUsageTimeFormatted

    private val _totalLimitTimeFormatted = MutableLiveData<String>()
    val totalLimitTimeFormatted: LiveData<String> = _totalLimitTimeFormatted

    private val _remainingTimeFormatted = MutableLiveData<String>()
    val remainingTimeFormatted: LiveData<String> = _remainingTimeFormatted

    private val _usageProgress = MutableLiveData<Int>()
    val usageProgress: LiveData<Int> = _usageProgress

    private val _blockedApps = MutableLiveData<List<AppInfo>>()
    val blockedApps: LiveData<List<AppInfo>> = _blockedApps

    private val _mostUsedApp = MutableLiveData<AppInfo?>()
    val mostUsedApp: LiveData<AppInfo?> = _mostUsedApp

    init {
        viewModelScope.launch {
            appLimitDao.getAllLimits().distinctUntilChanged().collect { dbLimits ->
                updateFullAppList(dbLimits)
            }
        }
    }

    fun forceUpdate() {
        viewModelScope.launch {
            updateFullAppList(appLimitDao.getAllLimitsAsList())
        }
    }

    private suspend fun updateFullAppList(dbLimits: List<AppLimit>) {
        withContext(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
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

            _appsList.postValue(allApps.sortedByDescending { it.isLimited })
            
            val limitedApps = allApps.filter { it.isLimited }
            _blockedApps.postValue(limitedApps.filter { it.isBlocked })
            _mostUsedApp.postValue(limitedApps.maxByOrNull { it.usageTime })
            
            val totalUsageSec = limitedApps.sumOf { it.usageTime }
            val totalLimitSec = limitedApps.sumOf { it.limitTime } * 60
            
            _totalUsageTimeFormatted.postValue(formatTime(totalUsageSec))
            _totalLimitTimeFormatted.postValue(formatTime(totalLimitSec))
            
            val remainingSec = (totalLimitSec - totalUsageSec).coerceAtLeast(0)
            _remainingTimeFormatted.postValue(formatTime(remainingSec))
            
            if (totalLimitSec > 0) {
                _usageProgress.postValue((totalUsageSec * 100 / totalLimitSec).coerceAtMost(100))
            } else {
                _usageProgress.postValue(0)
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "${h}s ${m}m" else "${m}m"
    }

    fun setAppLimit(packageName: String, limitInMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingLimit = appLimitDao.getLimit(packageName)
            val newLimit = if (existingLimit != null) {
                existingLimit.copy(
                    limitTime = limitInMinutes,
                    usageTime = 0,
                    isBlocked = false,
                    lastReset = System.currentTimeMillis()
                )
            } else {
                AppLimit(
                    packageName = packageName,
                    limitTime = limitInMinutes,
                    usageTime = 0,
                    isBlocked = false,
                    lastReset = System.currentTimeMillis(),
                    usageOffset = 0
                )
            }
            appLimitDao.insertOrUpdateLimit(newLimit)
        }
    }

    fun removeAppLimit(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) { appLimitDao.deleteLimit(packageName) }
    }
}
