package com.example.timelimit.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val icon: Drawable,
    var isLimited: Boolean,
    var usageTime: Long,      // Changed to Long to store seconds, not minutes
    var limitTime: Int = 0          // Limit remains in minutes
)