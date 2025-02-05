package com.call_blocker.loger

import android.util.Log
import com.call_blocker.loger.utils.getStackTrace
import timber.log.Timber

object SmartLog {

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e("SmartLog", message)
        }
        Timber.d(message)
    }

    fun e(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e("SmartLog", message)
        }
        Timber.e(message)
    }

    fun e(message: String, e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("SmartLog", "$message ${getStackTrace(e)}")
        }
        Timber.e("$message ${getStackTrace(e)}")
    }


    fun e(message: Exception) {
        if (BuildConfig.DEBUG) {
            Log.e("SmartLog", getStackTrace(message))
        }
        Timber.e(message, "error")
    }

}