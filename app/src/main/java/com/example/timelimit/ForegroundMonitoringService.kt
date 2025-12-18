package com.example.timelimit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundMonitoringService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "ForegroundMonitoringChannel")
            .setContentTitle("TimeLimit")
            .setContentText("Monitoring faol")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)

        // AppMonitoringService ni ishga tushirish
        val monitoringIntent = Intent(this, AppMonitoringService::class.java)
        startService(monitoringIntent)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ForegroundMonitoringChannel",
                "Foreground Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}