package com.call_blocker.app.worker_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.rest_work_imp.LogRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClearLogsWorker(private var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    private val logRepository: LogRepository by inject()
    private val smsBlockerDatabase: SmsBlockerDatabase by inject()
    override suspend fun doWork(): Result {
        SmartLog.e("ClearLogsWorker")
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles() ?: return Result.retry()

        SmartLog.e("Logs Count ${filesList.size}")
        try {
            for (file in filesList) {
                if (file.name == "fileToSend")
                    continue
                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val creationDate = format.parse(file.nameWithoutExtension) ?: Date()
                logRepository.sendLogs(file, smsBlockerDatabase.deviceID)
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