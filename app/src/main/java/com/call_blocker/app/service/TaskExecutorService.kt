package com.call_blocker.app.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.call_blocker.app.TaskManager
import com.call_blocker.loger.SmartLog
import kotlinx.coroutines.Job


class TaskExecutorService : Service() {

    private var player: MediaPlayer? = null

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    private var job: Job? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SmartLog.d("onStartCommand")

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        SmartLog.d("Service onDestroy")
        job = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}