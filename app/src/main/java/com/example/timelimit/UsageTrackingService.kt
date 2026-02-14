package com.example.timelimit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class UsageTrackingService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "UsageTrackingChannel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Bu servis faqat ilovani o'chib ketishidan himoya qilish uchun fonda turadi.
        // Asosiy bloklash va hisoblash mantiqi AppBlockerService (Accessibility) ichiga ko'chirildi.
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "TimeLimit Guard", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TimeLimit")
            .setContentText("Monitoring xizmati ishlamoqda")
            .setSmallIcon(R.drawable.ic_timer)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
