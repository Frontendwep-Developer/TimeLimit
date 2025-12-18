package com.example.timelimit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UsageTrackingService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var appLimitDao: AppLimitDao

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "UsageTrackingChannel"
    }

    override fun onCreate() {
        super.onCreate()
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("UsageTrackingService", "Service has started.")
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            while (isActive) {
                resetLimitsIfNeeded()
                checkUsageAndBlock(usageStatsManager)
                delay(5000) // Check every 5 seconds
            }
        }
        return START_STICKY
    }

    private suspend fun resetLimitsIfNeeded() {
        // ... (This logic remains the same)
    }

    private suspend fun checkUsageAndBlock(usageStatsManager: UsageStatsManager) {
        val limitedApps = appLimitDao.getAllLimitsAsList()
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
                Log.d("UsageTrackingService", "Updating ${appLimit.packageName} usage to $totalUsageToday seconds")
                appLimitDao.updateUsageTime(appLimit.packageName, totalUsageToday)
            }

            val usageSinceLimit = totalUsageToday - appLimit.usageOffset
            val limitInSeconds = appLimit.limitTime * 60

            if (limitInSeconds > 0 && usageSinceLimit >= limitInSeconds && !appLimit.isBlocked) {
                Log.d("UsageTrackingService", "Blocking ${appLimit.packageName}")
                appLimit.isBlocked = true
                appLimitDao.insertOrUpdateLimit(appLimit)
            }
        }

        // The UI will be updated by the Handler in MainActivity, so no broadcast is needed here.

        // Block foreground app if needed
        val foregroundApp = getForegroundApp(usageStatsManager)
        foregroundApp?.let {
            val blockedApp = appLimitDao.getLimit(it)
            if (blockedApp != null && blockedApp.isBlocked) {
                showBlockingScreen(it)
            }
        }
    }
    
    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        // ... (This function remains the same)
        return null
    }

    private fun showBlockingScreen(packageName: String) {
        // ... (This function remains the same)
    }

    private fun createNotification(): Notification {
        // ... (This function remains the same)
        return NotificationCompat.Builder(this, CHANNEL_ID).build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}