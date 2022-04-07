package com.call_blocke.app

import android.app.Application
import android.content.IntentFilter
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.call_blocke.app.broad_cast.SimSlotReceiver
import com.call_blocke.app.util.ConnectionManager
import com.call_blocke.app.worker_manager.ClearPhoneNumbersTableScheduler
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.google.firebase.FirebaseApp
import com.rokobit.adstvv_unit.loger.LogBuild
import java.util.concurrent.TimeUnit

class SmsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmsBlockerDatabase.init(context = this)
        RepositoryImp.init(this)
        FirebaseApp.initializeApp(applicationContext)
        ConnectionManager.innit(applicationContext)
        registerReceiver(
            SimSlotReceiver(),
            IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        )
        val work = PeriodicWorkRequestBuilder<ClearPhoneNumbersTableScheduler>(3, TimeUnit.DAYS)
            .setInitialDelay(3, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "ClearPhoneNumberTable",
                ExistingPeriodicWorkPolicy.KEEP,
                work
            )
        LogBuild.build(this)
    }
}