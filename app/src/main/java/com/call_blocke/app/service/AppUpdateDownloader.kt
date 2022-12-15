package com.call_blocke.app.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters


class AppUpdateDownloader(
    private val context: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        return Result.failure()
    }

}