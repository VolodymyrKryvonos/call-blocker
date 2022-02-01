package com.call_blocke.app.worker_manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.File
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

class ServiceWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        }
    }

    companion object {
        val isRunning = MutableLiveData(false)

        const val WORK_NAME = "ServiceWorker"

        fun start(context: Context) {
            SmartLog.d("start service ${BuildConfig.VERSION_NAME}")
            startWorkers(context)
        }

        private fun startWorkers(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.beginUniqueWork(
                WORK_NAME, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ServiceWorker>().build()
            ).enqueue()

            val work = PeriodicWorkRequestBuilder<RestartServiceWorker>(5, TimeUnit.MINUTES)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()
            workManager
                .enqueueUniquePeriodicWork(
                    "RestartServiceWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )

            workManager.enqueueUniquePeriodicWork(
                "ClearLogsWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ClearLogsWorker>(12, TimeUnit.HOURS)
                    .setInitialDelay(0, TimeUnit.MILLISECONDS)
                    .build()
            )
        }

        fun stop(context: Context) {
            SmartLog.d("stop service")
            WorkManager.getInstance(context).cancelAllWork()
            TaskExecutorImp.job?.cancel()
            isRunning.value = false
        }
    }

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
        SmartLog.e("Start worker")
        wakeLock.acquire(1000 * 60 * 35)
        isRunning.postValue(true)
        setForeground(createForegroundInfo())
        withContext(Dispatchers.IO) {
            RepositoryImp.taskRepository.connectionStatusFlow.onEach {
                SmartLog.e("Connect status $it")
                if (it) {
                    RepositoryImp.taskRepository.sendTaskStatuses()
                }
            }.launchIn(this)
            TaskExecutorImp.job = taskList.onEach { msg ->
                SmartLog.d("onEach ${msg.list.map { it.id }}")
                msg.list.forEach {
                    if (it.message == "GET_LOGS") {
                        sendLogs()
                    } else {
                        taskManager.doTask(it)
                    }
                }
            }.launchIn(this)
        }

        while (TaskExecutorImp.job?.isActive == true) {
            delay(1000 * 60 * 30)
            wakeLock.release()
            wakeLock.acquire(1000 * 60 * 35)
            RepositoryImp.taskRepository.reconnect()
        }
        return Result.success()
    }

    private suspend fun sendLogs() {
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles()
        if (filesList != null) {
            for (file in filesList) {
                file?.let { RepositoryImp.logRepository.sendLogs(it, SmsBlockerDatabase.deviceID) }
            }
        }
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
}