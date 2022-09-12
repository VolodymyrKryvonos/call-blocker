package com.call_blocke.app.broad_cast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.call_blocke.app.R
import com.call_blocke.app.worker_manager.ServiceWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.SimUtil
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace

class SimSlotReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            SmartLog.d("SimSlotReceiver")

            val firstSimId = SimUtil.firstSimId(context)
            val secondSimId = SimUtil.secondSimId(context)

            if (intent?.getStringExtra("ss") != "READY") {
                ServiceWorker.stop(context = context ?: return)
                return
            }
            SmartLog.d("firstSimId $firstSimId")
            SmartLog.d("secondSimId $secondSimId")
            SmartLog.d("SmsBlockerDatabase.firstSimId ${SmsBlockerDatabase.firstSimId}")
            SmartLog.d("SmsBlockerDatabase.secondSimId ${SmsBlockerDatabase.secondSimId}")

            if (firstSimId != SmsBlockerDatabase.firstSimId) {
                SmartLog.e("Sim1 was changed oldId = ${SmsBlockerDatabase.firstSimId} newId = $firstSimId")
                SmsBlockerDatabase.firstSimId = firstSimId
                SmsBlockerDatabase.firstSimChanged = true
            }
            if (secondSimId != SmsBlockerDatabase.secondSimId) {
                SmartLog.e("Sim2 was changed oldId = ${SmsBlockerDatabase.secondSimId} newId = $secondSimId")
                SmsBlockerDatabase.secondSimId = secondSimId
                SmsBlockerDatabase.secondSimChanged = true
            }

            ServiceWorker.stop(context = context ?: return)
        } catch (e: Exception) {
            SmartLog.e(getStackTrace(e))
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun notify(context: Context) {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context, "my_service", "My Background Service")
            } else {
                ""
            }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.sim_slot_change_title))
                .setContentText(context.getString(R.string.sim_slot_change_desc))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        } else {
            Notification.Builder(context)
                .setContentTitle(context.getString(R.string.sim_slot_change_title))
                .setContentText(context.getString(R.string.sim_slot_change_desc))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String
    ): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        chan.importance = NotificationManager.IMPORTANCE_HIGH
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}