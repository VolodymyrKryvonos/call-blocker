package com.rokobit.adstvv_unit.loger

import timber.log.Timber

object SmartLog {

    fun d(message: String) {
        Timber.d(message)
    }

    fun e(message: String) {
        Timber.e(message)
    }

    fun e(message: Exception) {
        Timber.e(message, "error")
    }

}