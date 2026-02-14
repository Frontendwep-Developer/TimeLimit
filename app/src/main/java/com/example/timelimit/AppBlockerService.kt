package com.example.timelimit

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.timelimit.data.AppLimit
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.*
import java.util.Calendar

class AppBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var appLimitDao: AppLimitDao
    private lateinit var powerManager: PowerManager
    
    // Xotirada vaqtni hisoblash uchun
    private var currentTrackingPackage: String? = null
    private var trackingJob: Job? = null
    private var currentUsageSeconds: Int = 0
    private var currentLimitSeconds: Int = 0
    
    // Bloklangan ilovalar keshlanadi
    private var blockedPackagesCache = mutableSetOf<String>()
    private var lastExitedPackage: String? = null
    private var exitTimestamp: Long = 0

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BlockingActivity.ACTION_EXIT_APP) {
                val pkg = intent.getStringExtra("packageName") ?: currentTrackingPackage
                Log.i(TAG, "Exit signal for: $pkg")
                lastExitedPackage = pkg
                exitTimestamp = System.currentTimeMillis()
                
                stopTracking()
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    companion object {
        private const val TAG = "AppBlockerService"
        private const val EXIT_GRACE_PERIOD = 3000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "========== SERVICE CONNECTED ==========")
        
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
            exitReceiver, IntentFilter(BlockingActivity.ACTION_EXIT_APP)
        )
        
        startForegroundService()
        observeDatabaseChanges()
    }

    private fun observeDatabaseChanges() {
        serviceScope.launch {
            appLimitDao.getAllLimits().collect { limits ->
                val newBlockedSet = mutableSetOf<String>()
                limits.forEach { if (it.isBlocked) newBlockedSet.add(it.packageName) }
                blockedPackagesCache = newBlockedSet
                Log.d(TAG, "Blocked cache updated: ${blockedPackagesCache.size} apps")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Logcat da har bir oyna o'zgarishini ko'rsatish
            Log.i(TAG, "WINDOW CHANGE: $packageName")

            // 1. Grace Period (Chiqishdan keyingi vaqt)
            val now = System.currentTimeMillis()
            if (packageName == lastExitedPackage && (now - exitTimestamp) < EXIT_GRACE_PERIOD) {
                return
            }

            // 2. Bloklanganmi?
            if (blockedPackagesCache.contains(packageName)) {
                Log.w(TAG, "BLOCKED APP DETECTED: $packageName")
                showBlockingScreen(packageName)
                stopTracking()
                return
            }

            // 3. Chetlab o'tishlar
            if (packageName == this.packageName || isLauncher(packageName) || packageName.contains("BlockingActivity")) {
                stopTracking()
                return
            }

            // 4. Yangi ilova ochilsa tracking boshlash
            if (packageName != currentTrackingPackage) {
                startTracking(packageName)
            }
        }
    }

    private fun startTracking(pkg: String) {
        stopTracking()
        currentTrackingPackage = pkg
        
        serviceScope.launch {
            val limit = withContext(Dispatchers.IO) { appLimitDao.getLimit(pkg) }
            if (limit != null) {
                Log.i(TAG, "START TRACKING: $pkg | Limit: ${limit.limitTime}m | Used: ${limit.usageTime}s")
                
                checkReset(limit)
                currentUsageSeconds = limit.usageTime
                currentLimitSeconds = limit.limitTime * 60

                trackingJob = launch {
                    while (currentTrackingPackage == pkg && isActive) {
                        delay(1000)
                        if (powerManager.isInteractive) {
                            currentUsageSeconds++
                            
                            // Har 5 soniyada bazaga yozamiz (CPU va Batareya uchun foydali)
                            if (currentUsageSeconds % 5 == 0) {
                                withContext(Dispatchers.IO) {
                                    appLimitDao.updateUsageTime(pkg, currentUsageSeconds)
                                }
                                Log.d(TAG, "Usage update: $pkg -> $currentUsageSeconds s")
                            }

                            // Limit tekshiruvi
                            if (currentUsageSeconds >= currentLimitSeconds) {
                                Log.w(TAG, "!!! LIMIT REACHED: $pkg !!!")
                                withContext(Dispatchers.IO) {
                                    appLimitDao.insertOrUpdateLimit(limit.copy(usageTime = currentUsageSeconds, isBlocked = true))
                                }
                                showBlockingScreen(pkg)
                                break
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "No limit for: $pkg")
            }
        }
    }

    private fun stopTracking() {
        val pkg = currentTrackingPackage
        val usage = currentUsageSeconds
        if (pkg != null) {
            serviceScope.launch(Dispatchers.IO) {
                appLimitDao.updateUsageTime(pkg, usage)
            }
        }
        trackingJob?.cancel()
        trackingJob = null
        currentTrackingPackage = null
        currentUsageSeconds = 0
        Log.d(TAG, "Tracking stopped")
    }

    private fun showBlockingScreen(packageName: String) {
        serviceScope.launch(Dispatchers.Main) {
            val intent = Intent(this@AppBlockerService, BlockingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("packageName", packageName)
            }
            try {
                startActivity(intent)
                Log.i(TAG, "BlockingActivity SUCCESS for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "BlockingActivity FAILED: ${e.message}")
            }
        }
    }

    private suspend fun checkReset(limit: AppLimit) {
        val lastReset = Calendar.getInstance().apply { timeInMillis = limit.lastReset }
        val now = Calendar.getInstance()
        if (lastReset.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
            withContext(Dispatchers.IO) {
                appLimitDao.insertOrUpdateLimit(limit.copy(usageTime = 0, isBlocked = false, lastReset = System.currentTimeMillis()))
            }
        }
    }

    private fun isLauncher(pkg: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == pkg
    }

    private fun startForegroundService() {
        val channelId = "AppBlockerChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "TimeLimit Core", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        startForeground(1002, NotificationCompat.Builder(this, channelId)
            .setContentTitle("TimeLimit")
            .setContentText("Monitoring faol")
            .setSmallIcon(R.drawable.ic_timer).build())
    }

    override fun onInterrupt() { stopTracking() }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(exitReceiver)
        stopTracking()
        serviceScope.cancel()
    }
}
