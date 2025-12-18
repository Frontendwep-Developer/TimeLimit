package com.example.timelimit

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        
        startForegroundService()
        checkCurrentWindow()
        
        Log.d("AppBlockerService", "Service Connected & Ready")
    }
    
    private fun checkCurrentWindow() {
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null && rootNode.packageName != null) {
                val currentPkg = rootNode.packageName.toString()
                val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
                event.packageName = currentPkg
                onAccessibilityEvent(event)
            }
        } catch (e: Exception) {
            Log.e("AppBlockerService", "Error checking active window", e)
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
            .setContentTitle("TimeLimit himoyasi")
            .setContentText("Ilovalar nazorat qilinmoqda")
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

            stopTracking()

            if (newPackage != packageName && !isLauncher(newPackage)) {
                startTracking(newPackage)
            }
        }
    }

    private fun startTracking(packageName: String) {
        currentPackage = packageName
        
        trackingJob = serviceScope.launch {
            val limit = appLimitDao.getLimit(packageName) ?: return@launch

            checkAndResetDailyLimit(limit)

            if (limit.limitTime <= 0) return@launch

            if (limit.isBlocked) {
                blockApp(packageName)
                return@launch
            }

            while (isActive) {
                val limitInSeconds = limit.limitTime * 60
                
                if (limit.usageTime >= limitInSeconds) {
                    limit.isBlocked = true
                    appLimitDao.insertOrUpdateLimit(limit)
                    blockApp(packageName)
                    break 
                }

                delay(1000)

                limit.usageTime += 1
                appLimitDao.updateUsageTime(packageName, limit.usageTime)
                Log.d("AppBlockerService", "$packageName: ${limit.usageTime}/${limitInSeconds}")
            }
        }
    }

    private suspend fun blockApp(packageName: String) {
        withContext(Dispatchers.Main) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            val intent = Intent(this@AppBlockerService, BlockingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
            Log.d("AppBlockerService", "Daily limit reset for ${limit.packageName}")
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
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
    
    // Ilova task managerdan o'chirilganda chaqiriladi
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("AppBlockerService", "Task Removed. Restarting foreground service...")
        startForegroundService() // Xizmatni yangilaymiz
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }
}