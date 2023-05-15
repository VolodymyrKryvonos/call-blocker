package com.call_blocker.app

import android.app.Application
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.call_blocker.app.util.NotificationService
import com.call_blocker.app.worker_manager.ClearPhoneNumbersTableScheduler
import com.call_blocker.common.ConnectionManager
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.LogBuild
import com.call_blocker.repository.RepositoryImp
import com.call_blocker.ussd_sender.UssdService
import com.google.firebase.FirebaseApp
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