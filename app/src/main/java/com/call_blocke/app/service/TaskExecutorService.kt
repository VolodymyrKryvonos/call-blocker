package com.call_blocke.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.repository.RepositoryImp
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@DelicateCoroutinesApi
class TaskExecutorService : Service() {

    companion object {
        val isRunning = MutableLiveData(false)

        fun start(context: Context) {
            SmartLog.d("user start service")
            context.startService(Intent(context, TaskExecutorService::class.java))
        }

        fun stop(context: Context) {
            SmartLog.d("user stop service")
            context.stopService(Intent(context, TaskExecutorService::class.java))
        }
    }

    private val taskRepository = RepositoryImp.taskRepository

    private val taskList = taskRepository
        .taskMessage()
        .catch { e ->
            stop(applicationContext)
            start(applicationContext)
            SmartLog.e("Restart service on error ${e.stackTrace} ${e.message}")
        }

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SmartLog.d("onStartCommand")

        registerNetworkCallback()
        startForeground()

        isRunning.postValue(true)

        job = taskList
            .onEach { msg ->
                msg.list.map {
                taskManager.doTask(it)
            }
            }
            .launchIn(GlobalScope)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        SmartLog.d("onDestroy")

        isRunning.postValue(false)
        job?.cancel()
        taskList.cancellable()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun registerNetworkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                SmartLog.e("Connected to the internet")
                if (isRunning.value == true) {
                    stop(applicationContext)
                    start(applicationContext)
                }
                super.onAvailable(network)
            }

            override fun onLost(network: Network) {
                SmartLog.e("Lost internet connection")
                super.onLost(network)
            }
        })
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
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
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