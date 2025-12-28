package com.example.timelimit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timelimit.data.AppLimitDao
import com.example.timelimit.model.AppInfoForStats
import com.example.timelimit.model.DailyUsage

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val appLimitDao: AppLimitDao

    val limitedApps: LiveData<List<AppInfoForStats>>
    private val _selectedAppPackage = MutableLiveData<String>()

    val selectedAppInfo: LiveData<AppInfoForStats>
    val weeklyTotalUsage: LiveData<Long>
    val dailyUsageData: LiveData<List<DailyUsage>>
    val todayUsage: LiveData<Long>
    val remainingTime: LiveData<Long>
    val dailyProgress: LiveData<Int>
    val openingsCount: LiveData<Int>
    val averageTimeChange: LiveData<Float>

    init {
        val db = (application as TimeLimitApp).database
        appLimitDao = db.appLimitDao()

        limitedApps = MutableLiveData() // Placeholder
        selectedAppInfo = MutableLiveData() // Placeholder
        weeklyTotalUsage = MutableLiveData() // Placeholder
        dailyUsageData = MutableLiveData() // Placeholder
        todayUsage = MutableLiveData() // Placeholder
        remainingTime = MutableLiveData() // Placeholder
        dailyProgress = MutableLiveData() // Placeholder
        openingsCount = MutableLiveData() // Placeholder
        averageTimeChange = MutableLiveData() // Placeholder
    }

    fun selectApp(packageName: String) {
        _selectedAppPackage.value = packageName
    }

    fun loadWeeklyStatsForApp(packageName: String) {
        // This is a placeholder. In a real app, you would load data from the database here.
    }
}