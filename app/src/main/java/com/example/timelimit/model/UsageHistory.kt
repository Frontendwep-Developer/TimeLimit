package com.example.timelimit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_history")
data class UsageHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: Long, // timestamp for the day
    val usageTime: Long, // milliseconds
    val limitTime: Long = 0L // minutes
)

