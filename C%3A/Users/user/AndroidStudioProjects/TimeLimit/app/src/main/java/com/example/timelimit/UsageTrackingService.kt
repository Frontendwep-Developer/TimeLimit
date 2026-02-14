"package com.example.timelimit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
        const val CHANNEL_ID = \"UsageTrackingChannel\"
        const val TAG = \"UsageTrackingService\"
    }

    override fun onCreate() {
        super.onCreate()
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, \"Service has started.\")
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            while (isActive) {
                try {
                    checkUsageAndBlock(usageStatsManager)
                } catch (e: Exception) {
                    Log.e(TAG, \"Error tracking usage\", e)
                }
                delay(5000) // Check every 5 seconds
            }
        }
        return START_STICKY
    }

    private suspend fun checkUsageAndBlock(usageStatsManager: UsageStatsManager) {
        // Hozirgi barcha limitlar ro'yxatini olamiz
        val limitedApps = appLimitDao.getAllLimitsAsList()
        if (limitedApps.isEmpty()) return

        // Bugungi kun boshidan beri statistikani olamiz
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        // Barcha ilovalar uchun statistika
        val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

        for (appLimit in limitedApps) {
            val stats = usageStatsMap[appLimit.packageName]
            
            // Agar stats null bo'lsa, demak bugun hali ishlatilmagan -> 0
            val totalUsageToday = if (stats != null) {
                TimeUnit.MILLISECONDS.toSeconds(stats.totalTimeInForeground).toInt()
            } else {
                0
            }

            Log.d(TAG, \"App: ${appLimit.packageName}, Usage: $totalUsageToday s, Limit: ${appLimit.limitTime} m\")

            // Bazadagi vaqtni yangilaymiz (agar o'zgargan bo'lsa)
            if (appLimit.usageTime != totalUsageToday) {
                appLimitDao.updateUsageTime(appLimit.packageName, totalUsageToday)
            }

            // Bloklash tekshiruvi
            // DIQQAT: Limit 0 bo'lsa, bloklamaymiz (chunki 0 = cheksiz yoki o'rnatilmagan bo'lishi mumkin)
            // Agar siz 0 ni \"darhol bloklash\" deb tushunsangiz, shartni o'zgartirish kerak.
            val limitInSeconds = appLimit.limitTime * 60
            
            if (limitInSeconds > 0 && totalUsageToday >= limitInSeconds && !appLimit.isBlocked) {
                Log.d(TAG, \"Blocking ${appLimit.packageName}\")
                appLimit.isBlocked = true
                appLimitDao.insertOrUpdateLimit(appLimit)
                showBlockingScreen(appLimit.packageName)
            }
        }
    }
    
    private fun showBlockingScreen(packageName: String) {
        val intent = Intent(this, BlockingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(\"packageName\", packageName)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                \"Usage Tracking Service\",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(\"TimeLimit Tracking\")
            .setContentText(\"Monitoring app usage...\")
            .setSmallIcon(R.drawable.ic_timer)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}"