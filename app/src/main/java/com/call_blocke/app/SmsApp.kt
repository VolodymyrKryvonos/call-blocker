package com.call_blocke.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
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

const val SERVICE_NOTIFICATION_CHANNEL_ID = "Service_Bottega_SMS"
const val EVENT_NOTIFICATION_CHANNEL_ID = "Event_Bottega_SMS"
private const val CHANNEL_NAME = "Bottega SMS"

class SmsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmsBlockerDatabase.init(context = this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val serviceChannel = NotificationChannel(
            SERVICE_NOTIFICATION_CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val eventChannel = NotificationChannel(
            EVENT_NOTIFICATION_CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        serviceChannel.lightColor = Color.BLUE
        serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        eventChannel.lightColor = Color.BLUE
        eventChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(eventChannel)
    }
}