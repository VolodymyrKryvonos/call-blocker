package com.call_blocker.rest_work_imp

import java.io.File

interface LogRepository {

    suspend fun sendLogs(file: File, deviceId: String)

}