package com.example.timelimit

import android.app.Application

class TimeLimitApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

}