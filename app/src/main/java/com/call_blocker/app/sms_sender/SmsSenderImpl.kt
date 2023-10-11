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

class SmsSenderImpl(
    private val context: Context,
    private val simCardVerificationChecker: SimCardVerificationChecker,
    private val smsBlockerDatabase: SmsBlockerDatabase,
    private val taskRepository: TaskRepository,
) : CoroutineScope, SmsSender {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    private val mHandler = Handler(Looper.getMainLooper())

    private val resolver: ContentResolver = context.contentResolver
    private var sentSmsNCountFirst = 0
    private var sentSmsNCountSecond = 0
    private val smsChecker = GsmSmsChecker()

    private val smsLimit by lazy {
        Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 1)
    }

    private val smsLimitInterval by lazy {
        (smsBlockerDatabase.profile?.delaySmsSend ?: 40) * 1000L
    }

    override suspend fun doTask(task: TaskEntity): Boolean {
        SmartLog.d("doTask ${task.id}")
        task.simSlot = SimUtil.simSlotById(context, task.simIccId)
        ConnectionManager.logSignalStrength()
        if (task.simSlot == null || task.simSlot == -1) {
            processSendError(task)
            return false
        }

        val sim = SimUtil.simInfo(context, task.simSlot!!)
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


    private suspend fun sendSms(
        simInfo: SubscriptionInfo, task: TaskEntity
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
                    SmartLog.e("resultCode = $resultCode  ${task.id}")
                    context.unregisterReceiver(this)
                    val result = resultCode == Activity.RESULT_OK
                    if (result) {
                        launch {
                            smsBlockerDatabase.phoneNumberDao.addNumber(PhoneNumber(task.sendTo))
                        }
                    }
                    cont.resume(result)
                }
            }.also {
                context.registerReceiver(it, IntentFilter(sentRegisterName))
            }

            val sentPI = PendingIntent.getBroadcast(
                context, task.sendTo.hashCode(), sentStatusIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val msgText = smsChecker.toGSM7BitText(task.message)
            SmartLog.e("msgText = $msgText")
            try {
                SmartLog.e("Try to sent message ${task.id}")
                smsManager.sendTextMessage(
                    task.sendTo, null, msgText, sentPI, null
                )
            } catch (e: Exception) {
                SmartLog.e("OnSendTextMessage ${getStackTrace(e)} ${e.message}")
            }
        }
    }
}