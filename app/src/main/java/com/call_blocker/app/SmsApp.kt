package com.call_blocker.app

import android.app.Application
import android.os.Build
import com.call_blocker.a_repository.di.repositoryModule
import com.call_blocker.app.di.appModule
import com.call_blocker.app.util.NotificationService
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.di.databaseModule
import com.call_blocker.loger.LogBuild
import com.call_blocker.ussd_sender.UssdService
import com.call_blocker.verification.verificationModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class SmsApp : Application() {


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SmsApp)
            modules(databaseModule, repositoryModule, appModule, verificationModule)
        }
        val smsBlockerDatabase: SmsBlockerDatabase = get()
        smsBlockerDatabase.isUssdCommandOn = UssdService.hasAccessibilityPermission(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationService.createNotificationChannel(applicationContext)
        }
        FirebaseApp.initializeApp(applicationContext)
        LogBuild.build(this)
    }
}