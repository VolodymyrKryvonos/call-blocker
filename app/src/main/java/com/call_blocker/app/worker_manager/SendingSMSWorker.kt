package com.call_blocker.app.worker_manager

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.managers.SignalStrengthManager
import com.call_blocker.app.managers.TaskManager
import com.call_blocker.app.util.NotificationService
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class SendingSMSWorker(private val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {
    private val taskManager: TaskManager by inject()
    private val signalStrengthManager: SignalStrengthManager by inject()
    override suspend fun doWork(): Result {
        try {
            SmartLog.e("Start worker")
            isRunning.emit(true)
            setForeground(NotificationService.createForegroundInfo(context))
            withContext(Dispatchers.IO) {
                taskRepository.connectionStatusFlow.onEach {
                    SmartLog.e("Connect status $it")
                    if (it) {
                        taskRepository.sendTaskStatuses()
                        signalStrengthManager.listenSignalStrength()
                    } else {
                        signalStrengthManager.stopListeningSignalStrength()
                    }
                }.launchIn(this)

                launch { taskRepository.collectMessagesToPriorityQueue() }
                launch {
                    while (isRunning.value) {
                        val message = taskRepository.messageQueue.poll()
                        if (message == null) {
                            delay(500)
                            continue
                        }
                        taskManager.processTask(message)
                    }
                }
                taskManager.checkConnection()
            }
        } catch (e: Exception) {
            SmartLog.e("Worker  ${getStackTrace(e)}")
        }
        return Result.success()
    }


    companion object : KoinComponent {

        val isRunning = MutableStateFlow(false)
        var job: Job? = null
        const val WORK_NAME = "SendingSMSWorker"

        private val smsBlockerDatabase: SmsBlockerDatabase by inject()
        private val taskRepository: TaskRepository by inject()
        private val settingsRepository: SettingsRepository by inject()
        fun start(context: Context) {
            SmartLog.e("${getDeviceName()} start service ${BuildConfig.VERSION_NAME}")
            SmartLog.e("Android ${Build.VERSION.SDK_INT}")
            startWorkers(context)
            SmartLog.e("Sms1 ${smsBlockerDatabase.smsPerDaySimFirst}")
            SmartLog.e("Sms2 ${smsBlockerDatabase.smsPerDaySimSecond}")
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
            WorkManager.getInstance(context).cancelUniqueWork("RestartServiceWorker")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                isRunning.emit(false)
                launch {
                    taskRepository.resendReceived()
                }
                launch {
                    settingsRepository.notifyServerUserStopService()
                }
            }
        }
    }
}