package com.example.timelimit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "limited_apps")
data class LimitedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val limitTime: Long, // minutes
    val isBlocked: Boolean = false,
    val blockUntil: Long? = null, // timestamp
    val createdAt: Long = System.currentTimeMillis()
)

