package com.example.timelimit

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.timelimit.data.AppLimit
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.*
import java.util.Calendar

class AppBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var appLimitDao: AppLimitDao

    private var currentPackage: String? = null
    private var trackingJob: Job? = null

    companion object {
        private const val TAG = "AppBlockerService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service Connected")
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        
        startForegroundService()
        
        // Servis ulanganda darhol tekshirish
        serviceScope.launch {
            delay(1000)
            checkCurrentWindow()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        startForegroundService()
        // Ekran holatini tekshirish yoki boshqa triggerlar
        checkCurrentWindow()
        return START_STICKY
    }
    
    private fun checkCurrentWindow() {
        var foundPkg: String? = null

        // 1. Accessibility orqali urinib ko'ramiz
        try {
            val rootNode = rootInActiveWindow
            if (rootNode?.packageName != null) {
                foundPkg = rootNode.packageName.toString()
                Log.d(TAG, "Current window (Accessibility): $foundPkg")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking window via Accessibility", e)
        }

        // 2. Agar Accessibility topolmasa, UsageStatsManager ishlatamiz (Zaxira)
        if (foundPkg == null) {
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                // Oxirgi 10 soniyadagi statistika
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time)
                if (stats != null) {
                    val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
                    if (sortedStats.isNotEmpty()) {
                        foundPkg = sortedStats[0].packageName
                        Log.d(TAG, "Current window (UsageStats): $foundPkg")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking window via UsageStats", e)
            }
        }

        // Agar ilova topilgan bo'lsa, monitoringni boshlaymiz
        if (foundPkg != null && foundPkg != packageName && !isLauncher(foundPkg)) {
             startTracking(foundPkg)
        }
    }
    
    private fun startForegroundService() {
        val channelId = "TimeLimitServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TimeLimit Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("TimeLimit")
            .setContentText("Monitoring faol")
            .setSmallIcon(R.drawable.ic_dashboard)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val newPackage = event.packageName?.toString() ?: return
            
            if (currentPackage == newPackage) return

            if (newPackage != packageName && !isLauncher(newPackage)) {
                startTracking(newPackage)
            } else {
                stopTracking()
            }
        }
    }

    private fun startTracking(packageName: String) {
        stopTracking()
        
        currentPackage = packageName
        Log.d(TAG, "START tracking: $packageName")

        trackingJob = serviceScope.launch {
            val limit = appLimitDao.getLimit(packageName)
            
            if (limit == null) return@launch

            checkAndResetDailyLimit(limit)

            if (limit.limitTime <= 0) return@launch

            Log.d(TAG, "Limit: ${limit.limitTime}m. Used: ${limit.usageTime}s")

            if (limit.isBlocked) {
                blockApp(packageName)
                return@launch
            }

            while (isActive) {
                val limitInSeconds = limit.limitTime * 60
                
                if (limit.usageTime >= limitInSeconds) {
                    Log.d(TAG, "Limit reached! Blocking $packageName")
                    limit.isBlocked = true
                    appLimitDao.insertOrUpdateLimit(limit)
                    blockApp(packageName)
                    break 
                }

                delay(1000)

                limit.usageTime += 1
                appLimitDao.updateUsageTime(packageName, limit.usageTime)
            }
        }
    }

    private suspend fun blockApp(packageName: String) {
        withContext(Dispatchers.Main) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            val intent = Intent(this@AppBlockerService, BlockingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("packageName", packageName)
            }
            startActivity(intent)
        }
    }

    private suspend fun checkAndResetDailyLimit(limit: AppLimit) {
        val lastResetCalendar = Calendar.getInstance().apply { timeInMillis = limit.lastReset }
        val currentCalendar = Calendar.getInstance()

        if (lastResetCalendar.get(Calendar.DAY_OF_YEAR) != currentCalendar.get(Calendar.DAY_OF_YEAR) ||
            lastResetCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
            
            limit.usageTime = 0
            limit.isBlocked = false
            limit.lastReset = System.currentTimeMillis()
            appLimitDao.insertOrUpdateLimit(limit)
            Log.d(TAG, "Daily reset for ${limit.packageName}")
        }
    }

    private fun stopTracking() {
        if (trackingJob != null) {
            trackingJob?.cancel()
            trackingJob = null
        }
        currentPackage = null
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    override fun onInterrupt() {
        stopTracking()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task Removed. Restarting...")
        startForegroundService() 
        checkCurrentWindow() // Qayta tekshiramiz
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        stopTracking()
        serviceScope.cancel()
    }
}