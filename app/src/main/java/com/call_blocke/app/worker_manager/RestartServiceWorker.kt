package com.call_blocke.app.worker_manager

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
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
            ServiceWorker.start(context)
        }
        return Result.success()
    }
}