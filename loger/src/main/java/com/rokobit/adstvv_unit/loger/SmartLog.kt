package com.rokobit.adstvv_unit.loger

import android.util.Log
import com.google.gson.Gson

object SmartLog {

    const val GEN_TAG = "AdsTv"

    fun d(message: String) {
        Log.d(GEN_TAG, message)
    }

    fun d(message: Any) {
        when (message) {
            is Int -> d(message.toString())
            is Float -> d(message.toString())
            else -> d(Gson().toJson(message))
        }
    }

    fun e(message: String) {
        Log.e(GEN_TAG, message)
    }

    fun e(message: Any) {
        when (message) {
            is Int -> e(message.toString())
            is Float -> e(message.toString())
            else -> e(Gson().toJson(message))
        }
    }

    fun e(message: Exception) {
        Log.e(GEN_TAG, "error", message)
    }

}