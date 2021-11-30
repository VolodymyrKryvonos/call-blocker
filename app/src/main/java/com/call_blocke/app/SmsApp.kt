package com.call_blocke.app

import android.app.Activity
import android.app.Application
import android.content.IntentFilter
import android.os.Bundle
import com.call_blocke.app.broad_cast.SimSlotReceiver
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.rokobit.adstvv_unit.loger.LogBuild
import com.rokobit.adstvv_unit.loger.SmartLog

class SmsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmsBlockerDatabase.init(context = this)
        RepositoryImp.init(this)

        registerReceiver(
            SimSlotReceiver(),
            IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        )

        LogBuild.build(this)
    }

    override fun onLowMemory() {
        SmartLog.d("onLowMemory")
        super.onLowMemory()
    }

    override fun onTerminate() {
        SmartLog.d("onAppKilled")
        super.onTerminate()
    }
}