package com.call_blocke.app.worker_manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ClearLogsWorker(private var context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {
    override fun doWork(): Result {
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles() ?: return Result.retry()
        try {
            for (file in filesList) {
                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val creationDate = format.parse(file.nameWithoutExtension) ?: Date()
                if (System.currentTimeMillis() - creationDate.time >= 2 * 24 * 60 * 60 * 1000) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }
}