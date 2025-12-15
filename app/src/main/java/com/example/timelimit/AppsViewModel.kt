package com.example.timelimit

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timelimit.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel : ViewModel() {

    private val _appsList = MutableLiveData<List<AppInfo>>()
    val appsList: LiveData<List<AppInfo>> = _appsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var isTrackerRunning = false

    fun startUsageTracker(context: Context) {
        if (isTrackerRunning) return
        isTrackerRunning = true

        viewModelScope.launch(Dispatchers.IO) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            while (isActive) {
                val foregroundApp = getForegroundApp(usageStatsManager)

                if (foregroundApp != null) {
                    val currentList = _appsList.value ?: emptyList()
                    var listUpdated = false
                    val updatedList = currentList.map {
                        if (it.packageName == foregroundApp && it.limitTime > 0) {
                            // Increment usage time by 1 second (we delay by 1 sec)
                            val newUsageTime = it.usageTime + 1 // This should be seconds, not minutes
                            listUpdated = true
                            it.copy(usageTime = newUsageTime)
                        } else {
                            it
                        }
                    }

                    if (listUpdated) {
                        _appsList.postValue(sortApps(updatedList))
                    }
                }
                delay(1000) // Check every second
            }
        }
    }

    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val time = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        if (usageStats != null && usageStats.isNotEmpty()) {
            return usageStats.sortedBy { it.lastTimeUsed }.lastOrNull()?.packageName
        }
        return null
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                // ... (existing code to load apps)
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                val packages = pm.queryIntentActivities(mainIntent, 0)
                packages.map {
                    AppInfo(
                        packageName = it.activityInfo.packageName,
                        appName = it.loadLabel(pm).toString(),
                        category = "",
                        icon = it.loadIcon(pm),
                        isLimited = false,
                        usageTime = 0 // Initial usage time
                    )
                }
            }
            _appsList.value = sortApps(apps)
            _isLoading.value = false
        }
    }

    fun setAppLimit(packageName: String, limitInMinutes: Int) {
        val currentList = _appsList.value ?: return
        val updatedList = currentList.map {
            if (it.packageName == packageName) {
                // limitTime is in minutes, usageTime is in seconds
                it.copy(limitTime = limitInMinutes, isLimited = limitInMinutes > 0)
            } else {
                it
            }
        }
        _appsList.value = sortApps(updatedList)
    }

    fun removeAppLimit(packageName: String) {
        val currentList = _appsList.value ?: return
        val updatedList = currentList.map {
            if (it.packageName == packageName) {
                it.copy(limitTime = 0, isLimited = false, usageTime = 0) // Reset usage time as well
            } else {
                it
            }
        }
        _appsList.value = sortApps(updatedList)
    }

    private fun sortApps(apps: List<AppInfo>): List<AppInfo> {
        return apps.sortedWith(
            compareByDescending<AppInfo> { it.limitTime > 0 && (it.usageTime / 60) >= it.limitTime } // Blocked apps first
                .thenByDescending { it.isLimited } // Then limited apps
                .thenBy { it.appName } // Then by app name alphabetically
        )
    }
}