package com.call_blocke.app

import android.app.Application
import android.content.IntentFilter
import com.call_blocke.app.broad_cast.SimSlotReceiver
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.onesignal.OneSignal
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

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId("52a0a88c-bf79-4fdc-90d3-00728ef22fb0")
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