package com.rokobit.adstvv_unit.loger

import android.content.Context
import timber.log.Timber

object LogBuild {

    fun build(context: Context) {
        Timber.plant(FileLoggingTree(context))
    }

}