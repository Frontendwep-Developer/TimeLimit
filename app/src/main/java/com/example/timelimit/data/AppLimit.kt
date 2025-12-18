package com.example.timelimit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val limitTime: Int, // in minutes
    var usageTime: Int,    // in seconds (total usage for the day)
    var isBlocked: Boolean = false,
    var lastReset: Long = System.currentTimeMillis(),
    var usageOffset: Int = 0 // in seconds (usage before limit was set)
)
