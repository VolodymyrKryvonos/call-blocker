package com.call_blocke.app.worker_manager

import android.content.Context
import android.util.Log
import androidx.work.*
import com.rokobit.adstvv_unit.loger.SmartLog

class RestartServiceWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.e("RestartServiceWorker", "RestartServiceWorker")
        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager
            .getWorkInfosForUniqueWork(ServiceWorker.WORK_NAME)
        if (workInfo.isDone || workInfo.isCancelled) {
            SmartLog.e("Restart worker")
            workManager.cancelUniqueWork(ServiceWorker.WORK_NAME)
            workManager.beginUniqueWork(
                ServiceWorker.WORK_NAME, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ServiceWorker>().build()
            ).enqueue()
        }
        return Result.success()
    }
}