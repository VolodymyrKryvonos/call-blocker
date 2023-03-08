package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.rest.LogRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.LogRepository
import com.call_blocker.common.rest.AppRest
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class LogRepositoryImpl : LogRepository {
    private val logRest: LogRest
        get() = AppRest(
            bearerToken = SmsBlockerDatabase.userToken ?: "",
            service = LogRest::class.java
        ).build()

    override suspend fun sendLogs(file: File, deviceId: String) {
        val fileToSend = File(file.parent, "fileToSend.html")
        file.copyTo(fileToSend, true)
        val requestFile = fileToSend.asRequestBody("text/html".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        try {
            logRest.sendLogs(deviceId.toRequestBody(), body)
        } catch (e: Exception) {
            SmartLog.e("Send logs ${getStackTrace(e)}")
        }

    }
}