package com.call_blocker.app.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.ForegroundInfo
import com.call_blocker.app.R
import com.call_blocker.app.ui.HolderActivity
import com.call_blocker.db.entity.TaskEntity

object NotificationService {
    private const val SERVICE_NOTIFICATION_CHANNEL_ID = "Service_Bottega_SMS"
    private const val EVENT_NOTIFICATION_CHANNEL_ID = "Event_Bottega_SMS"
    private const val CHANNEL_NAME = "Bottega SMS"
    private const val SERVICE_NOTIFICATION_ID = 772

    fun showVerificationFailedNotification(context: Context, task: TaskEntity) {
        val pendingIntent: PendingIntent =
            Intent(context, HolderActivity::class.java).let { notificationIntent ->
                notificationIntent.data = "sim_info".toUri()
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, EVENT_NOTIFICATION_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }
        notificationBuilder.setContentTitle(context.getString(R.string.verification_failed))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_EVENT)
            .setAutoCancel(true)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        notificationManager.notify(786 + (task.simSlot ?: 0), notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChangeSimDetectedNotification(context: Context): Notification {
        val notificationBuilder = Notification.Builder(context, SERVICE_NOTIFICATION_CHANNEL_ID)

        return notificationBuilder.setContentTitle("SIM card change detected")
            .setContentText("Updating...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    fun createForegroundInfo(context: Context): ForegroundInfo {
        val pendingIntent: PendingIntent =
            Intent(context, HolderActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, SERVICE_NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }


        val notification = notificationBuilder.setContentTitle("Task executor")
            .setContentText("On run")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()


        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                SERVICE_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ForegroundInfo(
                SERVICE_NOTIFICATION_ID,
                notification
            )
        }
    }

    fun showAutoVerificationFailedNotification(context: Context) {
        val pendingIntent: PendingIntent =
            Intent(context, HolderActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, EVENT_NOTIFICATION_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }
        notificationBuilder.setContentTitle(context.getString(R.string.auto_verification_failed))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.auto_verification_failed_info))
            )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        notificationManager.notify(799, notificationBuilder.build())

    }

}