package com.call_blocker.app.sms_sender

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import com.call_blocker.app.util.NotificationService
import com.call_blocker.common.ConnectionManager
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.db.TaskMethod
import com.call_blocker.db.entity.PhoneNumber
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.db.entity.TaskStatus
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import com.call_blocker.verification.domain.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class SmsSender(
    private val context: Context,
    private val simCardVerificationChecker: SimCardVerificationChecker,
    private val smsBlockerDatabase: SmsBlockerDatabase,
    private val taskRepository: TaskRepository,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

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

    suspend fun doTask(task: TaskEntity): Boolean {
        SmartLog.d("doTask ${task.id}")
        task.simSlot = SimUtil.simSlotById(context, task.simIccId)
        ConnectionManager.logSignalStrength()
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
    ): Boolean = coroutineScope {
        suspendCoroutine { cont ->
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