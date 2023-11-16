package com.call_blocker.app.worker_manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.call_blocker.loger.SmartLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClearLogsWorker(private var context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {
    override fun doWork(): Result {
        SmartLog.e("ClearLogsWorker")
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles() ?: return Result.retry()

        SmartLog.e("Logs Count ${filesList.size}")
        try {
            for (file in filesList) {
                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val creationDate = format.parse(file.nameWithoutExtension) ?: Date()

                SmartLog.e("File name ${file.name}")
                SmartLog.e("File creationDate $creationDate")
                if (System.currentTimeMillis() - creationDate.time >= 2 * 24 * 60 * 60 * 1000) {
                    SmartLog.e("File  ${file.name} deleted = ${file.delete()}")
                }
            }
        } catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }
}