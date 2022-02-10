package com.call_blocke.app.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.call_blocke.app.TaskManager
import com.rokobit.adstvv_unit.loger.SmartLog
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