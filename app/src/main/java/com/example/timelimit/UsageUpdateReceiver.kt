package com.example.timelimit

import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UsageUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UsageUpdateReceiver", "Alarm received. Checking usage...")
        val appLimitDao = AppDatabase.getDatabase(context).appLimitDao()
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        CoroutineScope(Dispatchers.IO).launch {
            val limitedApps = appLimitDao.getAllLimitsAsList()
            if (limitedApps.isEmpty()) return@launch

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }
            val startTime = cal.timeInMillis
            val endTime = System.currentTimeMillis()
            val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

            for (appLimit in limitedApps) {
                val stats = usageStatsList.find { it.packageName == appLimit.packageName }
                val totalUsageToday = TimeUnit.MILLISECONDS.toSeconds(stats?.totalTimeInForeground ?: 0L).toInt()
                
                if (appLimit.usageTime != totalUsageToday) {
                    appLimitDao.updateUsageTime(appLimit.packageName, totalUsageToday)
                }

                val usageSinceLimit = totalUsageToday - appLimit.usageOffset
                val limitInSeconds = appLimit.limitTime * 60
                if (limitInSeconds > 0 && usageSinceLimit >= limitInSeconds && !appLimit.isBlocked) {
                    appLimit.isBlocked = true
                    appLimitDao.insertOrUpdateLimit(appLimit)
                }
            }
        }
    }
}