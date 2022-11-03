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
import com.call_blocke.app.util.ConnectionManager
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.PhoneNumber
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min


class TaskManager(private val context: Context) {

    private val taskRepository = RepositoryImp.taskRepository

    private val mHandler = android.os.Handler(Looper.getMainLooper())


    private val resolver: ContentResolver = context.contentResolver

    private var sentSmsNCountFirst = 0
    private var sentSmsNCountSecond = 0

    private val smsLimit by lazy {
        Settings.Global.getInt(resolver, "sms_outgoing_check_max_count", 1)
    }

    private val smsLimitInterval by lazy {
        (SmsBlockerDatabase.profile?.delaySmsSend?:120)*1000L
    }

    init {
        SmartLog.e("smsLimitInterval = $smsLimitInterval")
        mHandler.postDelayed({
            sentSmsNCountFirst = 0
            sentSmsNCountSecond = 0
        }, smsLimitInterval)
    }

    @Synchronized
    suspend fun doTask(task: TaskEntity): Boolean {
        SmartLog.d("doTask ${task.id}")

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            SmartLog.e("SignalStrength = ${ConnectionManager.getSignalStrength()}")
        }
        SmartLog.e("NetworkGeneration = ${ConnectionManager.getNetworkGeneration()}")
        if (task.simSlot == null || task.simSlot == -1) {
            taskRepository.taskOnError(task)
            return false
        }

        val sim = sim(task.simSlot!!)
        if (sim == null) {
            SmartLog.e("Sim card is null")
            taskRepository.taskOnError(task)
            return false
        }
//        test
//        taskRepository.taskOnProcess(taskEntity = task, simSlot = task.simSlot ?: return false)
//        if (task.simSlot == 0) {
//            while (sentSmsNCountFirst >= smsLimit) {
//                delay(50L)
//            }
//        } else if (task.simSlot == 1) {
//            while (sentSmsNCountSecond >= smsLimit) {
//                delay(50L)
//            }
//        }
//        if (task.simSlot == 0) {
//            sentSmsNCountFirst++
//        } else if (task.simSlot == 1) {
//            sentSmsNCountSecond++
//        }
//        taskRepository.taskOnDelivered(taskEntity = task)
//        return false


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
        } else {
            try {
                taskRepository.taskOnError(task)
            } catch (e: Exception) {
            }
        }

        return status
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
                        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                            SmsBlockerDatabase.phoneNumberDao.addNumber(PhoneNumber(address))
                        }
                    }
                    cont.resume(result)
                }
            }.also {
                context.registerReceiver(it, IntentFilter(sentRegisterName))
            }

            val sentPI =
                PendingIntent.getBroadcast(context, address.hashCode(), sentStatusIntent, 0)
            val msgText = toGSM7BitText(text)
            SmartLog.e("msgText = $msgText")
            try {
                SmartLog.e("Try to sent message $id")
//                val messageParts = smsManager.divideMessage(text)
//                val pendingIntents: ArrayList<PendingIntent> =
//                    ArrayList(messageParts.size)
//                val nMsgParts = messageParts.size
//
//                for (i in 0 until messageParts.size) {
//                    pendingIntents.add(PendingIntent.getBroadcast(context, 0, sentStatusIntent, 0))
//                }
//                smsManager.sendMultipartTextMessage(
//                    address,
//                    null,
//                    smsManager.divideMessage(text),
//                    pendingIntents,
//                    null
//                )
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


