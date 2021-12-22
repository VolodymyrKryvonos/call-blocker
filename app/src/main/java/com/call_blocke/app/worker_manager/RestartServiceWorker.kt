package com.call_blocke.app.worker_manager

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.call_blocke.app.MainActivity
import com.call_blocke.app.service.TaskExecutorService

class RestartServiceWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.e("RestartServiceWorker", "RestartServiceWorker")
        TaskExecutorService.restart(context)
        context.startActivity(Intent(context, MainActivity::class.java))
        return Result.success()
    }
}