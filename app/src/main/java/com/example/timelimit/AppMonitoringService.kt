package com.example.timelimit

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.timer

class AppMonitoringService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var appLimitDao: AppLimitDao
    private lateinit var sharedPreferences: SharedPreferences
    private var monitoringTimer: Timer? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val PREFS_NAME = "BlockedAppsPrefs"
        const val BLOCKED_APPS_KEY = "blocked_apps_set"
        private const val TAG = "AppMonitoringService"
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "Monitoring Service Created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Monitoring Service Started.")
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        if (monitoringTimer != null) return // Agar timer ishlab turgan bo'lsa, yangisini ochmaymiz
        monitoringTimer = timer(period = 5000) {
            checkForegroundApp()
        }
    }

    private fun checkForegroundApp() {
        val foregroundApp = getForegroundApp()

        // O'zimizni yoki Bosh ekranni tekshirmaymiz
        if (foregroundApp == null || foregroundApp == packageName || isLauncher(foregroundApp)) {
            return
        }

        serviceScope.launch {
            val blockedApps = sharedPreferences.getStringSet(BLOCKED_APPS_KEY, emptySet()) ?: emptySet()

            // 1. Ilova allaqachon bloklanganmi?
            if (foregroundApp in blockedApps) {
                showBlockingScreen(foregroundApp)
                return@launch
            }

            // 2. Ilova uchun limit mavjudmi va limit oshganmi?
            val appLimit = appLimitDao.getLimit(foregroundApp)
            if (appLimit != null && appLimit.limitTime > 0) {
                val usageTimeSeconds = getUsageTimeForPackage(foregroundApp)
                val limitTimeSeconds = appLimit.limitTime * 60

                if (usageTimeSeconds >= limitTimeSeconds) {
                    Log.d(TAG, "Limit reached for $foregroundApp. Blocking...")
                    // Ilovani bloklanganlar ro'yxatiga qo'shamiz
                    val newBlockedApps = blockedApps.toMutableSet()
                    newBlockedApps.add(foregroundApp)
                    sharedPreferences.edit().putStringSet(BLOCKED_APPS_KEY, newBlockedApps).apply()

                    // Bloklash ekranini ko'rsatamiz
                    showBlockingScreen(foregroundApp)
                }
            }
        }
    }

    private fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        return usageStats?.sortedByDescending { it.lastTimeUsed }?.firstOrNull()?.packageName
    }

    private fun getUsageTimeForPackage(packageName: String): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val packageStats = usageStats.find { it.packageName == packageName }
        return (packageStats?.totalTimeInForeground ?: 0) / 1000 // sekundda qaytarish
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun showBlockingScreen(packageName: String) {
        val intent = Intent(this, BlockingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("packageName", packageName)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringTimer?.cancel()
        serviceJob.cancel()
        Log.d(TAG, "Monitoring Service Destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}