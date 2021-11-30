package com.call_blocke.app

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TaskManager(private val context: Context) {

    private val taskRepository = RepositoryImp.taskRepository

    private val mHandler = android.os.Handler(Looper.getMainLooper())
    private val mHandler2 = android.os.Handler(Looper.getMainLooper())


    private val resolver: ContentResolver = context.contentResolver

    private var sentSmsNCountFirst = 0
    private var sentSmsNCountSecond = 0

    private val smsLimit by lazy {
        Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 5)
    }

    private val smsLimitInterval by lazy {
        Settings.Global.getLong(resolver, "sms_outgoing_check_interval_ms", 30000)
    }

    @Synchronized
    suspend fun doTask(task: TaskEntity): Boolean {
        val sim = sim(task.simSlot ?: return false) ?: return false

        try {
            taskRepository.taskOnProcess(taskEntity = task, simSlot = task.simSlot ?: return false)
        } catch (e: Exception) {
        }

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

        if (task.simSlot == 0) {
            sentSmsNCountFirst++
        } else if (task.simSlot == 1) {
            sentSmsNCountSecond++
        }

        val status = sendSms(sim, task)

        if (status) {
            try {
                taskRepository.taskOnDelivered(task)
            } catch (e: Exception) {
            }
        }
        else {
            try {
                taskRepository.taskOnError(task)
            } catch (e: Exception) {
            }
        }

        return status
    }

    private fun sim(id: Int): SubscriptionInfo? {
        val simList = SimUtil.getSIMInfo(context)

        if (simList.size <= id)
            return null

        return simList[id]
    }

    private suspend fun sendSms(simInfo: SubscriptionInfo, task: TaskEntity): Boolean {
        return sendSms(simInfo, task.sendTo, task.message)
    }

    private suspend fun sendSms(simInfo: SubscriptionInfo, address: String, text: String): Boolean = suspendCoroutine { cont ->
        val sentRegisterName = "SMS_SENT"

        val smsManager: SmsManager = try {
            SmsManager.getSmsManagerForSubscriptionId(simInfo.subscriptionId)
        } catch (e: Exception) {
            e.printStackTrace()
            cont.resume(false)
            return@suspendCoroutine
        }

        val sentStatusIntent = Intent(sentRegisterName)

        object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, arg1: Intent) {
                cont.resume(resultCode == Activity.RESULT_OK)
                context.unregisterReceiver(this)
            }
        }.also {
            context.registerReceiver(it, IntentFilter(sentRegisterName))
        }

        val sentPI = PendingIntent.getBroadcast(context, address.hashCode(), sentStatusIntent, 0)

        smsManager.sendTextMessage(
            address,
            null,
            text,
            sentPI,
            null
        )
    }

}