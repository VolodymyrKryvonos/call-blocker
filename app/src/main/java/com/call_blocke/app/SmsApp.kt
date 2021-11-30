package com.call_blocke.app

import android.app.Application
import android.content.IntentFilter
import com.call_blocke.app.broad_cast.SimSlotReceiver
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.rokobit.adstvv_unit.loger.LogBuild

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

}