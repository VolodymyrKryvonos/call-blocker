package com.rokobit.adstvv_unit.loger

import android.util.Log
import com.google.gson.Gson
import timber.log.Timber

object SmartLog {

    const val GEN_TAG = "AdsTv"

    fun d(message: String) {
        Timber.d(message)
        Log.e("onMessageLocal1", message)
    }

    fun d(message: Any) {
        when (message) {
            is Int -> d(message.toString())
            is Float -> d(message.toString())
            else -> d(Gson().toJson(message))
        }
    }

    fun e(message: String) {
        Timber.e(message)
    }

    fun e(message: Any) {
        when (message) {
            is Int -> e(message.toString())
            is Float -> e(message.toString())
            else -> e(Gson().toJson(message))
        }
    }

    fun e(message: Exception) {
        Timber.e(message, "error")
    }

}