package com.example.timelimit

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DailyUsage(val day: String, val usageMillis: Long)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsManager = application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val _weeklyTotalUsage = MutableLiveData<String>()
    val weeklyTotalUsage: LiveData<String> = _weeklyTotalUsage

    private val _dailyUsageData = MutableLiveData<List<DailyUsage>>()
    val dailyUsageData: LiveData<List<DailyUsage>> = _dailyUsageData

    private val _todayUsage = MutableLiveData<String>()
    val todayUsage: LiveData<String> = _todayUsage

    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> = _remainingTime

    private val _dailyProgress = MutableLiveData<Int>()
    val dailyProgress: LiveData<Int> = _dailyProgress

    private val _openingsCount = MutableLiveData<String>()
    val openingsCount: LiveData<String> = _openingsCount

    private val _averageTimeChange = MutableLiveData<String>()
    val averageTimeChange: LiveData<String> = _averageTimeChange


    fun loadWeeklyStatsForApp(packageName: String, dailyLimitMinutes: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val endTime = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, -7)
            val startTime = cal.timeInMillis

            val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val appUsage = usageStats.filter { it.packageName == packageName }

            var totalWeeklyMillis = 0L
            val dailyData = mutableListOf<DailyUsage>()
            val weekDays = arrayOf("YA", "DU", "SE", "CH", "PA", "JU", "SH")

            for (i in 0..6) {
                val dayCal = Calendar.getInstance()
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                val dayOfWeek = weekDays[dayCal.get(Calendar.DAY_OF_WEEK) - 1]

                val dayUsage = appUsage.find { 
                    val usageCal = Calendar.getInstance()
                    usageCal.timeInMillis = it.firstTimeStamp
                    usageCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR) &&
                    usageCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR)
                }?.totalTimeInForeground ?: 0L
                
                totalWeeklyMillis += dayUsage
                dailyData.add(DailyUsage(dayOfWeek, dayUsage))
            }
            
            val hours = TimeUnit.MILLISECONDS.toHours(totalWeeklyMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(totalWeeklyMillis) % 60
            _weeklyTotalUsage.postValue("${hours}s ${minutes}m")
            _dailyUsageData.postValue(dailyData.reversed()) // To have Sunday last

            // Today's stats
            val todayUsageMillis = dailyData.firstOrNull { it.day == weekDays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1] }?.usageMillis ?: 0L
            val todayMinutes = TimeUnit.MILLISECONDS.toMinutes(todayUsageMillis)
            val remainingMinutes = if (dailyLimitMinutes > todayMinutes) dailyLimitMinutes - todayMinutes else 0
            val progress = if (dailyLimitMinutes > 0) ((todayMinutes.toDouble() / dailyLimitMinutes) * 100).toInt() else 0

            _todayUsage.postValue("${todayMinutes}m / ${dailyLimitMinutes}m")
            _remainingTime.postValue("$remainingMinutes daqiqa qoldi")
            _dailyProgress.postValue(progress)
            
            // Openings & Avg Time (placeholder)
            _openingsCount.postValue("12 marta")
            _averageTimeChange.postValue("-5%")
        }
    }
}