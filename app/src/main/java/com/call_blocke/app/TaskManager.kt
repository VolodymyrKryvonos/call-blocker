package com.call_blocke.app

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import androidx.core.app.ActivityCompat
import com.call_blocke.app.util.NotificationService
import com.call_blocke.app.worker_manager.SendingSMSWorker
import com.call_blocke.db.AutoValidationResult
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.TaskMethod
import com.call_blocke.db.ValidationState
import com.call_blocke.db.entity.PhoneNumber
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.Resource
import com.example.common.ConnectionManager
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


class TaskManager(
    private val context: Context,
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
) : CoroutineScope {

    private var checkConnectionJob: Job? = null
    private var sendSignalStrengthJob: Job? = null
    private val taskRepository = RepositoryImp.taskRepository

    private val mHandler = android.os.Handler(Looper.getMainLooper())

    private val resolver: ContentResolver = context.contentResolver

    private var sentSmsNCountFirst = 0
    private var sentSmsNCountSecond = 0

    private val smsLimit by lazy {
        Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 1)
    }

    private val smsLimitInterval by lazy {
        (SmsBlockerDatabase.profile?.delaySmsSend ?: 40) * 1000L
    }

    suspend fun processTask(task: TaskEntity) {
        SmartLog.e(task.toString())
        when (task.method) {
            TaskMethod.GET_LOGS -> sendLogs()
            TaskMethod.UPDATE_USER_PROFILE -> updateProfile()
            else -> doTask(task)
        }
    }

    private fun updateProfile() {
        launch {
            RepositoryImp.settingsRepository.getProfile().collectLatest {
                when (it) {
                    is Resource.Error -> Unit
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        SmsBlockerDatabase.profile = it.data
                        sendSignalStrengthJob?.cancel()
                        sendSignalStrength()
                        RepositoryImp.taskRepository.reconnect()
                        if (SmsBlockerDatabase.profile?.isConnected == true) {
                            checkConnection()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun sendSignalStrength() {
        sendSignalStrengthJob?.cancel()
        sendSignalStrengthJob = launch {
            while (SendingSMSWorker.isRunning.value) {
                val delay =
                    SmsBlockerDatabase.profile?.delaySignalStrength?.toDuration(DurationUnit.SECONDS)
                if (delay != null) {
                    delay(delay)
                    RepositoryImp.settingsRepository.sendSignalStrengthInfo()
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun checkConnection() {
        checkConnectionJob?.cancel()
        checkConnectionJob = launch {
            while (SendingSMSWorker.isRunning.value) {
                val delay =
                    SmsBlockerDatabase.profile?.delayIsConnected?.toDuration(DurationUnit.SECONDS)
                SmartLog.e("Check connection delay ${delay?.inWholeSeconds} seconds")
                if (delay != null) {
                    delay(delay)
                    val connectionStatus = RepositoryImp.settingsRepository.checkConnection()
                    if (connectionStatus is Resource.Success) {
                        if (connectionStatus.data?.status == false) {
                            RepositoryImp.taskRepository.reconnect()
                        }
                    }
                }
            }
        }
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
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            SmartLog.e("SignalStrength = ${ConnectionManager.getSignalStrength()}")
        }
        SmartLog.e("NetworkGeneration = ${ConnectionManager.getNetworkGeneration()}")
    }

    private suspend fun processSendError(task: TaskEntity) {
        if (task.method == TaskMethod.VERIFY_PHONE_NUMBER) {
            NotificationService.showVerificationFailedNotification(context, task)
            emitValidationCompletion(task.simSlot)
        }
        if (task.method == TaskMethod.AUTO_VERIFY_PHONE_NUMBER) {
            if (task.simSlot == 0) {
                SmsBlockerDatabase.simFirstAutoValidationResult = AutoValidationResult.FAILED
            } else {
                SmsBlockerDatabase.simSecondAutoValidationResult = AutoValidationResult.FAILED
            }
            NotificationService.showAutoVerificationFailedNotification(context)
        }
        taskRepository.taskOnError(task)
    }

    private suspend fun emitValidationCompletion(simSlot: Int?) {
        if (simSlot == 0) {
            SmsBlockerDatabase.firstSimValidationState.emit(ValidationState.FAILED)
        } else {
            SmsBlockerDatabase.secondSimValidationState.emit(ValidationState.FAILED)
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
        simInfo: SubscriptionInfo,
        address: String,
        text: String,
        id: Int
    ): Boolean =
        suspendCoroutine { cont ->
            val sentRegisterName = "SMS_SENT_${System.currentTimeMillis()}"

            val smsManager: SmsManager = try {
                SmsManager.getSmsManagerForSubscriptionId(simInfo.subscriptionId)
            } catch (e: Exception) {
                SmartLog.e("OnSimSelect ${getStackTrace(e)} ${e.message}")
                e.printStackTrace()
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
                            SmsBlockerDatabase.phoneNumberDao.addNumber(PhoneNumber(address))
                        }
                    }
                    cont.resume(result)
                }
            }.also {
                context.registerReceiver(it, IntentFilter(sentRegisterName))
            }

            val sentPI =
                PendingIntent.getBroadcast(
                    context,
                    address.hashCode(),
                    sentStatusIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            val msgText = toGSM7BitText(text)
            SmartLog.e("msgText = $msgText")
            try {
                SmartLog.e("Try to sent message $id")
                smsManager.sendTextMessage(
                    address,
                    null,
                    msgText,
                    sentPI,
                    null
                )
            } catch (e: Exception) {
                SmartLog.e("OnSendTextMessage ${getStackTrace(e)} ${e.message}")
            }
        }

    private val gsmAlphabet =
        charArrayOf(
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
    } else
        text
}


