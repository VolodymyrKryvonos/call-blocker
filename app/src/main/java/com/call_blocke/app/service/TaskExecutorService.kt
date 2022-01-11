package com.call_blocke.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.call_blocke.app.BuildConfig.VERSION_NAME
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.app.worker_manager.RestartServiceWorker
import com.call_blocke.app.worker_manager.ServiceWorker
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

object TaskExecutorImp {
    private var taskList: Flow<TaskMessage>? = null

    var job: Job? = null
    fun buildTaskList(): Flow<TaskMessage> {
        if (taskList == null)
            taskList = RepositoryImp.taskRepository.taskMessage

        return taskList!!
    }
}

class TaskExecutorService : Service() {

    private var player: MediaPlayer? = null

    companion object {
        val isRunning = MutableLiveData(false)

        fun start(context: Context) {

            SmartLog.d("start service $VERSION_NAME")
            WorkManager.getInstance(context).beginUniqueWork(
                ServiceWorker.WORK_NAME, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ServiceWorker>().build()
            ).enqueue()
            val work = PeriodicWorkRequestBuilder<RestartServiceWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "RestartServiceWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )
        }

        fun stop(context: Context) {
            SmartLog.d("stop service")
            WorkManager.getInstance(context).cancelAllWork()
            TaskExecutorImp.job?.cancel()
            isRunning.value = false
        }

        fun restart(context: Context) {
            SmartLog.d("restart service")
            stop(context)
            Handler(Looper.getMainLooper()).postDelayed({ start(context) }, 5000)
        }
    }


    private val taskList: Flow<TaskMessage> by lazy {
        TaskExecutorImp
            .buildTaskList()
    }

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    private val networkCallback = object :
        ConnectivityManager.NetworkCallback() {
        var lost = false
        override fun onAvailable(network: Network) {
            SmartLog.e("Connected to the internet")
            if (isRunning.value == true && lost) {
                restart(applicationContext)
            }
            lost = false
            super.onAvailable(network)
        }

        override fun onLost(network: Network) {
            lost = true
            SmartLog.e("Lost internet connection")
            super.onLost(network)
        }
    }
    private var job: Job? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SmartLog.d("onStartCommand")

        startForeground()

        isRunning.postValue(true)

        job = taskList.onEach { msg ->
            SmartLog.d("onEach ${msg.list.map { it.id }}")

            msg.list.forEach {
                taskManager.doTask(it)
            }
        }.launchIn(GlobalScope)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()
        registerNetworkCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        SmartLog.d("Service onDestroy")

        isRunning.postValue(false)
        job?.cancel()
        job = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun registerNetworkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {

        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {

        }
    }

    private fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val channelId =
            createNotificationChannel("my_service", "My Background Service")

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Task executor")
            .setContentText("On run")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        this.startForeground(88, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun notifySmsLimitDone() {
        val channelId =
            createNotificationChannel("my_service", "My Background Service")

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle(applicationContext.getString(R.string.service_sms_limit_done_title))
            .setContentText(applicationContext.getString(R.string.service_sms_limit_done_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, notification)
    }
}