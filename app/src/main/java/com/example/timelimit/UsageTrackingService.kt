package com.example.timelimit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class UsageTrackingService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "UsageTrackingChannel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("UsageTrackingService", "Service starting...")
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            while (isActive) {
                trackUsage()
                delay(1000) // Check every second
            }
        }

        return START_STICKY
    }

    private fun trackUsage() {
        // TODO: Implement usage tracking logic with UsageStatsManager here
        Log.d("UsageTrackingService", "Tracking usage...")
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, 
                "Usage Tracking Service", 
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TimeLimit is active")
            .setContentText("Monitoring app usage in the background.")
            .setSmallIcon(R.drawable.ic_dashboard) // Replace with your app icon
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't provide binding, so return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("UsageTrackingService", "Service stopping...")
        serviceJob.cancel()
    }
}