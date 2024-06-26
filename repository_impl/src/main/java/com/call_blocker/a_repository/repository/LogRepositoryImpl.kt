package com.call_blocker.a_repository.repository

import com.call_blocker.a_repository.rest.LogRest
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.LogRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class LogRepositoryImpl(private val logRest: LogRest) : LogRepository {

    override suspend fun sendLogs(file: File, deviceId: String) {
        val fileToSend = File(file.parent, "fileToSend.html")
        file.copyTo(fileToSend, true)
        val requestFile = fileToSend.asRequestBody("text/html".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        try {
            logRest.sendLogs(deviceId = deviceId.toRequestBody(), file = body)
        } catch (e: Exception) {
            SmartLog.e("Send logs ${getStackTrace(e)}")
        }
    }
}