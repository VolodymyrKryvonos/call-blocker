package com.call_blocker.app.worker_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.call_blocker.db.SmsBlockerDatabase

class ClearPhoneNumbersTableScheduler(private val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        try {
            SmsBlockerDatabase.init(context)
            SmsBlockerDatabase.phoneNumberDao.clear()
        } catch (e: SmsBlockerDatabase.DbModuleException) {
            return Result.failure()
        }
        return Result.success()
    }
}