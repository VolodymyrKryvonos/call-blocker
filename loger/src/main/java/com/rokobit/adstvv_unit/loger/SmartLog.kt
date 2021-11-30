package com.rokobit.adstvv_unit.loger

import com.google.gson.Gson
import timber.log.Timber

object SmartLog {

    const val GEN_TAG = "AdsTv"

    fun d(message: String) {
        Timber.plant(FileLoggingTree())
        Timber.d(message)
    }

    fun d(message: Any) {
        Timber.plant(FileLoggingTree())
        when (message) {
            is Int -> d(message.toString())
            is Float -> d(message.toString())
            else -> d(Gson().toJson(message))
        }
    }

    fun e(message: String) {
        Timber.plant(FileLoggingTree())
        Timber.e(message)
    }

    fun e(message: Any) {
        Timber.plant(FileLoggingTree())
        when (message) {
            is Int -> e(message.toString())
            is Float -> e(message.toString())
            else -> e(Gson().toJson(message))
        }
    }

    fun e(message: Exception) {
        Timber.plant(FileLoggingTree())
        Timber.e(message, "error")
    }

}