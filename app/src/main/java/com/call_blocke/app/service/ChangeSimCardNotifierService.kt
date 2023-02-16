package com.call_blocke.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.call_blocke.app.util.NotificationService
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ChangeSimCardNotifierService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    private var notificationJob: Job? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1111, NotificationService.createChangeSimDetectedNotification(this))
        }
        notificationJob?.cancel()
        notificationJob = launch {
            delay(10 * 1000)
            RepositoryImp.settingsRepository.changeSimCard(
                this@ChangeSimCardNotifierService
            )
            SmsBlockerDatabase.firstSimChanged = false
            SmsBlockerDatabase.secondSimChanged = false
            this@ChangeSimCardNotifierService.stopForeground(STOP_FOREGROUND_REMOVE)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    companion object {
        fun startService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(
                    Intent(
                        context,
                        ChangeSimCardNotifierService::class.java
                    )
                )
            } else {
                context.startService(Intent(context, ChangeSimCardNotifierService::class.java))
            }
        }
    }
}