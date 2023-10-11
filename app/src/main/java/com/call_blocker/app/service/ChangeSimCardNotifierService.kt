package com.call_blocker.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.call_blocker.app.util.NotificationService
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.rest_work_imp.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

class ChangeSimCardNotifierService : Service(), CoroutineScope, KoinComponent {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    private var notificationJob: Job? = null
    private val smsBlockerDatabase: SmsBlockerDatabase by inject()
    private val settingsRepository: SettingsRepository by inject()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1111, NotificationService.createChangeSimDetectedNotification(this))
        }
        notificationJob?.cancel()
        notificationJob = launch {
            delay(15 * 1000)
            settingsRepository.changeSimCard(
                this@ChangeSimCardNotifierService
            )
            smsBlockerDatabase.firstSimChanged = false
            smsBlockerDatabase.secondSimChanged = false
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