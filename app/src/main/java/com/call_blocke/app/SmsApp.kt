package com.call_blocke.app

import android.app.Application
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.call_blocke.app.util.NotificationService
import com.call_blocke.app.worker_manager.ClearPhoneNumbersTableScheduler
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.example.common.ConnectionManager
import com.example.ussd_sender.UssdService
import com.google.firebase.FirebaseApp
import com.rokobit.adstvv_unit.loger.LogBuild
import java.util.concurrent.TimeUnit


class SmsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmsBlockerDatabase.init(context = this)
        SmsBlockerDatabase.isUssdCommandOn = UssdService.hasAccessibilityPermission(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationService.createNotificationChannel(applicationContext)
        }
        RepositoryImp.init(this)
        FirebaseApp.initializeApp(applicationContext)
        ConnectionManager.init(applicationContext)
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