package com.call_blocker.app

import android.content.Context
import com.call_blocker.app.sms_sender.SmsSender
import com.call_blocker.app.util.wakeScreen
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.ConnectionManager
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.db.TaskMethod
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.loger.SmartLog
import com.call_blocker.rest_work_imp.LogRepository
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.rest_work_imp.UssdRepository
import com.call_blocker.ussd_sender.SessionResult
import com.call_blocker.ussd_sender.UssdService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class TaskManager(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val ussdRepository: UssdRepository,
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
    private val smsBlockerDatabase: SmsBlockerDatabase,
    private val smsSender: SmsSender
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    init {
        ConnectionManager.init(context)
    }

    private var checkConnectionJob: Job? = null
    private var sendSignalStrengthJob: Job? = null

    suspend fun processTask(task: TaskEntity) {
        SmartLog.e(task.toString())
        when (task.method) {
            TaskMethod.AUTO_VERIFY_PHONE_NUMBER, TaskMethod.VERIFY_PHONE_NUMBER -> smsSender.doTask(
                task
            )

            TaskMethod.SEND_USSD_CODE -> sendUssdCode(task)
            TaskMethod.GET_LOGS -> {
                sendLogs()
            }

            TaskMethod.UPDATE_USER_PROFILE -> updateProfile()
            else -> smsSender.doTask(task)
        }
    }


    private fun sendUssdCode(task: TaskEntity) {
        SmartLog.e("sendUssdCode $task")
        task.simSlot = SimUtil.simSlotById(context, task.simIccId)
        val sessionCallback: (SessionResult) -> Unit = { result ->
            SmartLog.e("sendUssdCode result: $result")
            storeUssdResult(
                task, when (result) {
                    is SessionResult.Error -> result.message
                    is SessionResult.Success -> result.message
                    SessionResult.Timeout -> "Session timeout"
                }
            )
        }
        launch {
            if (!smsBlockerDatabase.isUssdCommandOn) {
                storeUssdResult(task, "Ussd commands is disabled")
                return@launch
            }
            SmartLog.e("sendUssdCode ${task.message}")
            withContext(Dispatchers.Main) {
                wakeScreen(context)
            }
            if (task.message == "stop") {
                UssdService.closeSession()
                return@launch
            }
            if (!UssdService.isSessionAlive) {
                UssdService.startSession(task.message, task.simSlot ?: 0, context, sessionCallback)
            } else {
                try {
                    UssdService.selectMenu(task.message, sessionCallback)
                } catch (e: Exception) {
                    UssdService.startSession(
                        task.message,
                        task.simSlot ?: 0,
                        context,
                        sessionCallback
                    )
                }
            }
        }
    }

    private fun storeUssdResult(task: TaskEntity, result: String) {
        launch {
            ussdRepository.storeUssdResponse(
                ussdCommand = task.message,
                result = result,
                simId = task.simIccId,
                countryCode = CountryCodeExtractor.getCountryCode(context)
            ).collectLatest {
                if (it.isSuccess) {
                    SmartLog.e("Store ussd command success")
                } else {
                    SmartLog.e("Store ussd command failure")
                }
            }
        }
    }

    private fun updateProfile() {
        launch {
            settingsRepository.getProfile().collectLatest {
                when (it) {
                    is Resource.Error -> Unit
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        smsBlockerDatabase.profile = it.data
                        sendSignalStrengthJob?.cancel()
                        sendSignalStrength()
                        taskRepository.reconnect()
                        if (smsBlockerDatabase.profile?.isConnected == true) {
                            checkConnection()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    fun sendSignalStrength() {
        sendSignalStrengthJob?.cancel()
        sendSignalStrengthJob = launch {
            while (SendingSMSWorker.isRunning.value) {
                val delay =
                    smsBlockerDatabase.profile?.delaySignalStrength?.toDuration(DurationUnit.SECONDS)
                if (delay != null) {
                    delay(delay)
                    settingsRepository.sendSignalStrengthInfo(
                        context
                    )
                }
            }
        }
    }

    fun checkConnection() {
        checkConnectionJob?.cancel()
        checkConnectionJob = launch {
            while (SendingSMSWorker.isRunning.value) {
                val delay =
                    smsBlockerDatabase.profile?.delayIsConnected?.toDuration(DurationUnit.SECONDS)
                SmartLog.e("Check connection delay ${delay?.inWholeSeconds} seconds")
                if (delay != null) {
                    delay(delay)
                    val connectionStatus = settingsRepository.checkConnection(
                        context
                    )
                    if (connectionStatus is Resource.Success) {
                        if (connectionStatus.data?.status == false) {
                            taskRepository.reconnect()
                        }
                    }
                } else {
                    delay(60000)
                }
            }
        }
    }

    private suspend fun sendLogs() {
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles()
        if (filesList != null) {
            for (file in filesList) {
                if (!file.path.contains("fileToSend")) file?.let {
                    logRepository.sendLogs(
                        it, smsBlockerDatabase.deviceID
                    )
                }
            }
        }
    }
}


