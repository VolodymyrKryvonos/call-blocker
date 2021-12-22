package com.call_blocke.app.broad_cast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.call_blocke.app.R
import com.call_blocke.app.service.TaskExecutorService
import com.call_blocke.db.SmsBlockerDatabase

class SimSlotReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SimSlotReceiver", "SimSlotReceiver")

        if (intent?.getStringExtra("ss") != "READY")
            return

        TaskExecutorService.stop(context = context ?: return)

        SmsBlockerDatabase.isSimChanged = true

        notify(context = context)
    }

    private fun notify(context: Context) {
        val channelId =
            createNotificationChannel(context, "my_service", "My Background Service")

        val notification: Notification = Notification.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.sim_slot_change_title))
            .setContentText(context.getString(R.string.sim_slot_change_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH)
            .build()

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