package com.call_blocker.app

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import androidx.core.app.ActivityCompat
import com.call_blocker.app.util.NotificationService
import com.call_blocker.app.util.wakeScreen
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.ConnectionManager
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.db.TaskMethod
import com.call_blocker.db.entity.PhoneNumber
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.db.entity.TaskStatus
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.LogRepository
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.rest_work_imp.UssdRepository
import com.call_blocker.ussd_sender.SessionResult
import com.call_blocker.ussd_sender.UssdService
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import com.call_blocker.verification.domain.VerificationStatus
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class TaskManager(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val ussdRepository: UssdRepository,
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository,
    private val simCardVerificationChecker: SimCardVerificationChecker,
    private val smsBlockerDatabase: SmsBlockerDatabase
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    init {
        ConnectionManager.init(context)
    }

    private var checkConnectionJob: Job? = null
    private var sendSignalStrengthJob: Job? = null

    private val mHandler = Handler(Looper.getMainLooper())

    private val resolver: ContentResolver = context.contentResolver

    private var sentSmsNCountFirst = 0
    private var sentSmsNCountSecond = 0

    private val smsLimit by lazy {
        Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 1)
    }

    private val smsLimitInterval by lazy {
        (smsBlockerDatabase.profile?.delaySmsSend ?: 40) * 1000L
    }

    suspend fun processTask(task: TaskEntity) {
        SmartLog.e(task.toString())
        when (task.method) {
            TaskMethod.AUTO_VERIFY_PHONE_NUMBER, TaskMethod.VERIFY_PHONE_NUMBER -> doTask(task)
            TaskMethod.SEND_USSD_CODE -> sendUssdCode(task)
            TaskMethod.GET_LOGS -> {
                sendLogs()
            }

            TaskMethod.UPDATE_USER_PROFILE -> updateProfile()
            else -> doTask(task)
        }
    }


    private fun sendUssdCode(task: TaskEntity) {
        SmartLog.e("sendUssdCode $task")
        task.simSlot = SimUtil.simSlotById(context, task.simIccId)
        val sessionCallback: (SessionResult) -> Unit = { result ->
            SmartLog.e("sendUssdCode result: $result")
            storeUssdResult(
                task,
                when (result) {
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
                UssdService.startSession(
                    task.message, task.simSlot ?: 0, context, sessionCallback
                )
            } else {
                try {
                    UssdService.selectMenu(task.message, sessionCallback)
                } catch (e: Exception) {
                    UssdService.startSession(
                        task.message, task.simSlot ?: 0, context, sessionCallback
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

    private suspend fun doTask(task: TaskEntity): Boolean {
        SmartLog.d("doTask ${task.id}")
        task.simSlot = SimUtil.simSlotById(context, task.simIccId)

        logSignalStrength()
        if (task.simSlot == null || task.simSlot == -1) {
            processSendError(task)
            return false
        }

        val sim = sim(task.simSlot!!)
        if (sim == null) {
            SmartLog.e("Sim card is null")
            processSendError(task)
            return false
        }
        try {
            taskRepository.taskOnProcess(taskEntity = task, simSlot = task.simSlot ?: return false)
        } catch (_: Exception) {
        }
        delaySmsSending(task)
        if (task.simSlot == 0) {
            sentSmsNCountFirst++
        } else if (task.simSlot == 1) {
            sentSmsNCountSecond++
        }
        val status = sendSms(sim, task)
        sendStatus(status, task)
        return status
    }

    private suspend fun delaySmsSending(task: TaskEntity) {
        if (task.simSlot == 0) {
            while (sentSmsNCountFirst >= smsLimit) {
                delay(50L)
            }
        } else if (task.simSlot == 1) {
            while (sentSmsNCountSecond >= smsLimit) {
                delay(50L)
            }
        }

        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            sentSmsNCountFirst = 0
            sentSmsNCountSecond = 0
        }, smsLimitInterval)
    }

    private suspend fun sendStatus(status: Boolean, task: TaskEntity) {
        if (status) {
            if (task.method == TaskMethod.VERIFY_PHONE_NUMBER || task.method == TaskMethod.AUTO_VERIFY_PHONE_NUMBER) {
                simCardVerificationChecker.coroutineScope = this
                simCardVerificationChecker.waitForVerification(task.simSlot ?: -1, context)
            }
            try {
                taskRepository.taskOnDelivered(task)
            } catch (e: Exception) {
                SmartLog.e("Failed send status ${task.id} ${TaskStatus.DELIVERED}")
            }
        } else {
            try {
                processSendError(task)
            } catch (e: Exception) {
                SmartLog.e("Failed send status ${task.id} ${TaskStatus.ERROR}")
            }
        }
    }

    private fun logSignalStrength() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            SmartLog.e("SignalStrength = ${ConnectionManager.getSignalStrength()}")
        }
        SmartLog.e("NetworkGeneration = ${ConnectionManager.getNetworkGeneration()}")
    }

    private suspend fun processSendError(task: TaskEntity) {
        if (task.method == TaskMethod.VERIFY_PHONE_NUMBER) {
            NotificationService.showVerificationFailedNotification(context, task)
            updateVerificationState(task.simSlot)
        }
        if (task.method == TaskMethod.AUTO_VERIFY_PHONE_NUMBER) {
            NotificationService.showAutoVerificationFailedNotification(context)
            updateVerificationState(task.simSlot)

        }
        taskRepository.taskOnError(task)
    }

    private suspend fun updateVerificationState(simSlot: Int?) {
        if (simSlot != null) {
            VerificationInfoStateHolder.getStateHolderBySimSlotIndex(simSlot).apply {
                emit(this.value.copy(status = VerificationStatus.Failed))
            }
        }
    }

    private fun sim(id: Int): SubscriptionInfo? = if (id == 0) {
        SimUtil.firstSim(context)
    } else {
        SimUtil.secondSim(context)
    }

    private suspend fun sendSms(simInfo: SubscriptionInfo, task: TaskEntity): Boolean {
        return sendSms(simInfo, task.sendTo, task.message, task.id)
    }

    private suspend fun sendSms(
        simInfo: SubscriptionInfo, address: String, text: String, id: Int
    ): Boolean = suspendCoroutine { cont ->
        val sentRegisterName = "SMS_SENT_${System.currentTimeMillis()}"

        val smsManager: SmsManager = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(SmsManager::class.java) as SmsManager).createForSubscriptionId(
                    simInfo.subscriptionId
                )
            } else {
                SmsManager.getSmsManagerForSubscriptionId(simInfo.subscriptionId)
            }
        } catch (e: Exception) {
            SmartLog.e("OnSimSelect ${getStackTrace(e)} ${e.message}")
            cont.resume(false)
            return@suspendCoroutine
        }

        val sentStatusIntent = Intent(sentRegisterName)

        object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, arg1: Intent) {
                SmartLog.e("resultCode = $resultCode  $id")
                context.unregisterReceiver(this)
                val result = resultCode == Activity.RESULT_OK
                if (result) {
                    launch {
                        smsBlockerDatabase.phoneNumberDao.addNumber(PhoneNumber(address))
                    }
                }
                cont.resume(result)
            }
        }.also {
            context.registerReceiver(it, IntentFilter(sentRegisterName))
        }

        val sentPI = PendingIntent.getBroadcast(
            context, address.hashCode(), sentStatusIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val msgText = toGSM7BitText(text)
        SmartLog.e("msgText = $msgText")
        try {
            SmartLog.e("Try to sent message $id")
            smsManager.sendTextMessage(
                address, null, msgText, sentPI, null
            )
        } catch (e: Exception) {
            SmartLog.e("OnSendTextMessage ${getStackTrace(e)} ${e.message}")
        }
    }

    private val gsmAlphabet = charArrayOf(
        '|',
        '€',
        '^',
        '{',
        '}',
        '[',
        '~',
        ']',
        '\\',
        '@',
        'Δ',
        '0',
        '¡',
        'P',
        '¿',
        'p',
        '£',
        '_',
        '!',
        '1',
        'A',
        'Q',
        'a',
        'q',
        '$',
        'Φ',
        '"',
        '2',
        'B',
        'R',
        'b',
        'r',
        '¥',
        'Γ',
        '#',
        '3',
        'C',
        'S',
        'c',
        's',
        'è',
        'Λ',
        '¤',
        '4',
        'D',
        'T',
        'd',
        't',
        'é',
        'Ω',
        '%',
        '5',
        'E',
        'U',
        'e',
        'u',
        'ù',
        'Π',
        '&',
        '6',
        'F',
        'V',
        'f',
        'v',
        'ì',
        'Ψ',
        '\'',
        '7',
        'G',
        'W',
        'g',
        'w',
        'ò',
        'Σ',
        '(',
        '8',
        'H',
        'X',
        'h',
        'x',
        'Ç',
        'Θ',
        ')',
        '9',
        'I',
        'Y',
        'i',
        'y',
        'Ξ',
        '*',
        ':',
        'J',
        'Z',
        'j',
        'z',
        'Ø',
        '+',
        ';',
        'K',
        'Ä',
        'k',
        'ä',
        'ø',
        'Æ',
        ',',
        '<',
        'L',
        'Ö',
        'l',
        'ö',
        'æ',
        '-',
        '=',
        'M',
        'Ñ',
        'm',
        'ñ',
        'Å',
        'ß',
        '.',
        '>',
        'N',
        'Ü',
        'n',
        'ü',
        'å',
        'É',
        '/',
        '?',
        'O',
        '§',
        'o',
        'à',
        '\n',
        ' ',
        '\r'
    )

    private fun toGSM7BitText(text: String) = if (text.any { !gsmAlphabet.contains(it) }) {
        text.substring(0, min(70, text.length))
    } else text
}


