package com.call_blocke.app.worker_manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.app.service.TaskExecutorImp
import com.call_blocke.app.service.TaskExecutorService
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ServiceWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    private val taskList: Flow<TaskMessage> by lazy {
        TaskExecutorImp
            .buildTaskList()
    }

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    override suspend fun doWork(): Result {
        registerNetworkCallback()
        TaskExecutorService.isRunning.postValue(true)
        setForeground(createForegroundInfo())
        withContext(Dispatchers.IO) {
            TaskExecutorImp.job = taskList.onEach { msg ->
                SmartLog.d("onEach ${msg.list.map { it.id }}")
                msg.list.forEach {
                    taskManager.doTask(it)
                }
            }.launchIn(this)
        }
        while (TaskExecutorImp.job?.isActive == true) {
            delay(1000 * 60 * 30)
            RepositoryImp.taskRepository.reconnect()
        }
        return Result.success()
    }


    private fun createForegroundInfo(): ForegroundInfo {
        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, 0)
            }

        val channelId =
            createNotificationChannel("my_worker", "My Background Worker")

        val notification: Notification = Notification.Builder(applicationContext, channelId)
            .setContentTitle("Task executor")
            .setContentText("On run")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        return ForegroundInfo(1001, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(chan)
        return channelId
    }


    private fun registerNetworkCallback() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
        }
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {

        }
    }


    private val networkCallback = object :
        ConnectivityManager.NetworkCallback() {
        var lost = false
        override fun onAvailable(network: Network) {
            SmartLog.e("Connected to the internet")
            if (lost) {
                RepositoryImp.taskRepository.reconnect()
            }
            lost = false
            GlobalScope.launch {
                delay(10000)
                RepositoryImp.taskRepository.sendTaskStatuses()
            }
            super.onAvailable(network)
        }

        override fun onLost(network: Network) {
            lost = true
            SmartLog.e("Lost internet connection")
            super.onLost(network)
        }
    }

    companion object {
        const val WORK_NAME = "ServiceWorker"
    }
}