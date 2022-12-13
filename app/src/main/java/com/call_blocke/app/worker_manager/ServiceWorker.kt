package com.call_blocke.app.worker_manager

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.app.TaskManager
import com.call_blocke.app.scheduler.SmsLimitRefreshScheduler
import com.call_blocke.app.util.ConnectionManager
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

class ServiceWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val wakeLock: PowerManager.WakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        }
    }


    companion object {
        val isRunning = MutableStateFlow(false)

        var job: Job? = null
        const val WORK_NAME = "ServiceWorker"

        fun start(context: Context) {
            SmartLog.e("${getDeviceName()} start service ${BuildConfig.VERSION_NAME}")
            SmartLog.e("Android ${Build.VERSION.SDK_INT}")
            startWorkers(context)
            SmartLog.e("Sms1 ${SmsBlockerDatabase.smsPerDaySimFirst}")
            SmartLog.e("Sms2 ${SmsBlockerDatabase.smsPerDaySimSecond}")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                SmartLog.e("SignalStrength = ${ConnectionManager.getSignalStrength()}")
            }
            SmartLog.e("NetworkGeneration = ${ConnectionManager.getNetworkGeneration()}")
        }

        private fun getDeviceName(): String {
            return "${Build.MANUFACTURER} ${Build.MODEL}"
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
            Firebase.crashlytics.log("stop service")
            SmartLog.d("stop service")
            SmsLimitRefreshScheduler.stop()

            job?.cancel()
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

            job = taskList!!.onEach { msg ->
                SmartLog.d("onEach ${msg.list.map { it.id }}")
                msg.list.forEach {
                    taskManager.processTask(it)
                }
            }.launchIn(this)
            taskManager.checkConnection()
        }
        return Result.success()
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
                .setSmallIcon(R.drawable.app_logo)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        } else {
            Notification.Builder(applicationContext)
                .setContentTitle("Task executor")
                .setContentText("On run")
                .setSmallIcon(R.drawable.app_logo)
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