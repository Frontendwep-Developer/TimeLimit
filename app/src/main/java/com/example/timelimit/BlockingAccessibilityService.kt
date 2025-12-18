package com.example.timelimit

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.timelimit.data.AppLimitDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BlockingAccessibilityService : AccessibilityService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var appLimitDao: AppLimitDao

    override fun onServiceConnected() {
        super.onServiceConnected()
        appLimitDao = AppDatabase.getDatabase(this).appLimitDao()
        Log.d("BlockingService", "Blocking Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // O'zimizni bloklamaslik uchun
            if (packageName == this.packageName) return

            checkAndBlock(packageName)
        }
    }

    private fun checkAndBlock(packageName: String) {
        serviceScope.launch {
            val limit = appLimitDao.getLimit(packageName)
            // Agar limit topilsa va u bloklangan bo'lsa
            if (limit != null && limit.isBlocked) {
                 Log.d("BlockingService", "Blocking $packageName")
                 val intent = Intent(this@BlockingAccessibilityService, BlockingActivity::class.java).apply {
                     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     putExtra("packageName", packageName)
                 }
                 startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Service uzilib qolsa
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}