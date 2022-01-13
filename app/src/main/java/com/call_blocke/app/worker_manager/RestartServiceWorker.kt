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
        val workInfos = workManager
            .getWorkInfosForUniqueWork(ServiceWorker.WORK_NAME).get()
        if (workInfos.size > 1) {
            SmartLog.e("More then one service started")
            return Result.retry()
        }
        SmartLog.e("Worker state ${workInfos.firstOrNull()?.state?.name}")
        if (workInfos.firstOrNull()?.state?.isFinished == true) {
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