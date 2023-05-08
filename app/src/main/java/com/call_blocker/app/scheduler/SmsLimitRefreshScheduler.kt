package com.call_blocker.app.scheduler

import com.call_blocker.db.SmsBlockerDatabase
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object SmsLimitRefreshScheduler {
    var executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun startExecutionAt(targetHour: Int, targetMin: Int, targetSec: Int) {
        val taskWrapper = Runnable {
            SmsBlockerDatabase.smsTodaySentFirstSim = 0
            SmsBlockerDatabase.smsTodaySentSecondSim = 0
            SmsBlockerDatabase.lastRefreshTime =
                Calendar.getInstance(TimeZone.getTimeZone("CET")).timeInMillis
            startExecutionAt(targetHour, targetMin, targetSec)
        }
        val delay = computeNextDelay(targetHour, targetMin, targetSec)
        executorService.schedule(taskWrapper, delay, TimeUnit.MILLISECONDS)
    }

    private fun computeNextDelay(targetHour: Int, targetMin: Int, targetSec: Int): Long {
        val c = Calendar.getInstance(TimeZone.getTimeZone("CET"))
        c.set(Calendar.HOUR_OF_DAY, targetHour)
        c.set(Calendar.MINUTE, targetMin)
        c.set(Calendar.SECOND, targetSec)
        c.add(Calendar.DATE, 1)
        return c.timeInMillis - System.currentTimeMillis()
    }

    fun stop() {
        executorService.shutdown()
        executorService = Executors.newScheduledThreadPool(1)
    }

}