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
import androidx.work.*
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.app.scheduler.SmsLimitRefreshScheduler
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


object TaskExecutorImp {
    var job: Job? = null
}

class ServiceWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        }
    }

    companion object {
        val isRunning = MutableStateFlow(false)

        const val WORK_NAME = "ServiceWorker"

        fun start(context: Context) {
            val today = Calendar.getInstance(TimeZone.getTimeZone("CET"))
            val lastRefresh =
                Calendar.getInstance().also { it.timeInMillis = SmsBlockerDatabase.lastRefreshTime }
            if (today.get(Calendar.DATE) != lastRefresh.get(Calendar.DATE)) {
                SmsBlockerDatabase.smsTodaySentFirstSim = 0
                SmsBlockerDatabase.smsTodaySentSecondSim = 0
            }
            SmartLog.e("${getDeviceName()} start service ${BuildConfig.VERSION_NAME}")
            startWorkers(context)
            SmsLimitRefreshScheduler.startExecutionAt(0, 0, 0)
        }

        private fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return "$manufacturer $model"
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
            SmsLimitRefreshScheduler.stop()

            TaskExecutorImp.job?.cancel()
            WorkManager.getInstance(context).cancelAllWork()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                isRunning.emit(false)
                RepositoryImp.taskRepository.resendReceived()
            }
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    private var taskList: Flow<TaskMessage>? = null

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    override suspend fun doWork(): Result {
        SmartLog.e("Start worker")
        taskList = RepositoryImp.taskRepository.taskMessage()
        wakeLock.acquire(1000 * 60 * 35)
        isRunning.emit(true)
        setForeground(createForegroundInfo())
        withContext(Dispatchers.IO) {
            RepositoryImp.taskRepository.connectionStatusFlow.onEach {
                SmartLog.e("Connect status $it")
                if (it) {
                    RepositoryImp.taskRepository.sendTaskStatuses()
                }
            }.launchIn(this)
            TaskExecutorImp.job = taskList!!.onEach { msg ->
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
            if (!RepositoryImp.taskRepository.connectionStatusFlow.last()) {
                SmartLog.e("Timeout reconnect")
                RepositoryImp.taskRepository.reconnect()
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_worker", "My Background Worker")
            } else {
                ""
            }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(applicationContext, channelId)
                .setContentTitle("Task executor")
                .setContentText("On run")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle("Task executor")
                .setContentText("On run")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        }
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