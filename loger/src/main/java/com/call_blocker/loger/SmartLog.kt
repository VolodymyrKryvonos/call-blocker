package com.call_blocker.loger

import com.call_blocker.loger.utils.getStackTrace
import timber.log.Timber

object SmartLog {

    fun d(message: String) {
        Timber.d(message)
    }

    fun e(message: String) {
        Timber.e(message)
    }

    fun e(message: String, e: Throwable) {
        Timber.e("$message ${getStackTrace(e)}")
    }


    fun e(message: Exception) {
        Timber.e(message, "error")
    }

}