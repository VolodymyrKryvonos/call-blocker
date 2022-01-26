package com.call_blocke.app.worker_manager

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.rokobit.adstvv_unit.loger.SmartLog
import java.util.*


object UStats {

    suspend fun getStats(context: Context) {
        val usm = getUsageStatsManager(context)
        val calendar: Calendar = Calendar.getInstance()
        val endTime: Long = calendar.timeInMillis
        calendar.add(Calendar.HOUR, -1)
        val startTime: Long = calendar.timeInMillis
        val uEvents = usm.queryEvents(startTime, endTime)
        while (uEvents.hasNextEvent()) {
            val e = UsageEvents.Event()
            uEvents.getNextEvent(e)
            SmartLog.e("Process: " + e.packageName)
        }
    }

    fun getUsageStatsList(context: Context): List<UsageStats> {
        val usm = getUsageStatsManager(context)
        val calendar: Calendar = Calendar.getInstance()
        val endTime: Long = calendar.timeInMillis
        calendar.add(Calendar.HOUR, -1)
        val startTime: Long = calendar.timeInMillis
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
    }

    @SuppressLint("WrongConstant")
    private fun getUsageStatsManager(context: Context): UsageStatsManager {
        return context.getSystemService("usagestats") as UsageStatsManager
    }
}