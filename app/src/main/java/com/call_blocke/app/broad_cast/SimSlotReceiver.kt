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
import com.rokobit.adstvv_unit.loger.SmartLog

class SimSlotReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        SmartLog.d("SimSlotReceiver")

        val firstSimId = SimUtil.firstSimId(context)
        val secondSimId = SimUtil.secondSimId(context)

        if (intent?.getStringExtra("ss") != "READY") {
            SmsBlockerDatabase.firstSimId = firstSimId
            SmsBlockerDatabase.secondSimId = secondSimId
            return
        }

        if (SimUtil.firstSimId(context) != SmsBlockerDatabase.firstSimId) {
            SmsBlockerDatabase.firstSimId = firstSimId
            SmsBlockerDatabase.firstSimChanged = true
        }
        if (SimUtil.secondSimId(context) != SmsBlockerDatabase.secondSimId) {
            SmsBlockerDatabase.secondSimId = secondSimId
            SmsBlockerDatabase.secondSimChanged = true
        }

        ServiceWorker.stop(context = context ?: return)

        SmsBlockerDatabase.isSimChanged = true

        notify(context = context)
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
    private fun createNotificationChannel(context: Context,
                                          channelId: String,
                                          channelName: String): String {
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