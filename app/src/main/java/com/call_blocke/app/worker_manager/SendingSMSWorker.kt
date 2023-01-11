package com.call_blocke.app.worker_manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.TaskManager
import com.call_blocke.app.util.NotificationService
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskMessage
import com.example.common.ConnectionManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class SendingSMSWorker(private val context: Context, parameters: WorkerParameters) :
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
                OneTimeWorkRequestBuilder<SendingSMSWorker>().build()
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
            job?.cancel()
            WorkManager.getInstance(context).cancelAllWork()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                isRunning.emit(false)
                RepositoryImp.taskRepository.resendReceived()
            }
        }
    }


    private var taskList: Flow<TaskMessage>? = null

    private val taskManager by lazy {
        TaskManager(applicationContext)
    }

    override suspend fun doWork(): Result {
        SmartLog.e("Start worker")
        taskList = RepositoryImp.taskRepository.taskMessage()
        wakeLock.acquire(1000 * 60 * 35)
        isRunning.emit(true)
        setForeground(NotificationService.createForegroundInfo(context))
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
            taskManager.sendSignalStrength()
        }
        return Result.success()
    }

}