package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.app.util.Const
import com.call_blocke.app.util.NotificationService
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.VerificationState
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.call_blocke.rest_work_imp.model.Resource
import com.rokobit.adstvv_unit.loger.SmartLog
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern


class SmsReceiver : BroadcastReceiver() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    override fun onReceive(context: Context, intent: Intent) {
        SmsBlockerDatabase.init(context)
        val bundle: Bundle?
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            bundle = intent.extras
            if (bundle != null) {
                val pduObjects = bundle["pdus"] as Array<*>?
                if (pduObjects != null) {
                    coroutineScope.launch {
                        processSms(pduObjects, bundle, context)
                    }
                }
            }
        }
    }

    private suspend fun processSms(pduObjects: Array<*>, bundle: Bundle, context: Context) {
        val currentSMS = getIncomingMessage(pduObjects, bundle)

        if (!checkIsVerificationSms(currentSMS, context)) {
            storeReply(currentSMS)
        }
    }


    private suspend fun checkIsVerificationSms(sms: ReplyMessage, context: Context): Boolean {
        SmartLog.e("checkIsVerificationSms $sms")

        val regex = ":.(.*?)[,.]"
        val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
        val matcher: Matcher = pattern.matcher(sms.smsText)
        val verificationCode = if (matcher.find()) {
            matcher.group(1)
        } else {
            return false
        }
        val simSlot = if (matcher.find()) {
            matcher.group(1)
        } else {
            return false
        }
        val iccid = if (matcher.find()) {
            matcher.group(1)
        } else {
            return false
        }
        val verificationSms = VerificationSms(simSlot, iccid, verificationCode)

        SmartLog.e("verificationSms $verificationSms")
        RepositoryImp.settingsRepository.confirmVerification(
            simSlot = verificationSms.simSlot,
            iccid = verificationSms.simIccid,
            verificationCode = verificationSms.verificationCode ?: "",
            phoneNumber = sms.senderNumber,
            uniqueId = verificationSms.uniqueId ?: SmsBlockerDatabase.deviceID
        ).collectLatest {
            if (verificationSms.uniqueId == SmsBlockerDatabase.deviceID) {
                if (it is Resource.Success) {
                    emitVerificationCompletion(verificationSms.simSlot, VerificationState.SUCCESS)
                    NotificationService.showPhoneNumberVerifiedNotification(
                        sms.senderNumber,
                        context,
                        verificationSms.simSlot.last().digitToIntOrNull() ?: -1
                    )
                    return@collectLatest
                }
                if (it is Resource.Error) {
                    emitVerificationCompletion(verificationSms.simSlot, VerificationState.FAILED)
                }
            }
        }
        return true
    }

    private suspend fun emitVerificationCompletion(simSlot: String, status: VerificationState) {
        if (simSlot == Const.firstSim) {
            SmsBlockerDatabase.firstSimVerificationState.emit(status)
        } else {
            SmsBlockerDatabase.secondSimVerificationState.emit(status)
        }
    }

    private suspend fun storeReply(sms: ReplyMessage) {
        replyRepository.storeReply(
            sms.senderNumber,
            sms.smsText,
            sms.receiveDate
        )
    }

    private fun getIncomingMessage(pduObjects: Array<*>, bundle: Bundle): ReplyMessage {
        val smsText = StringBuilder()
        var senderNumber = ""
        var receivedDate = 0L
        val format = bundle.getString("format")

        for (aObject in pduObjects) {
            val currentFrame = SmsMessage.createFromPdu(aObject as ByteArray, format)
            SmartLog.e("OriginatingAddress: ${currentFrame.displayOriginatingAddress}")
            smsText.append(
                currentFrame.displayMessageBody
            )
            senderNumber = currentFrame.displayOriginatingAddress
            receivedDate = currentFrame.timestampMillis
        }
        return ReplyMessage(receivedDate, smsText.toString(), senderNumber)
    }

}

data class ReplyMessage(
    val receiveDate: Long,
    val smsText: String,
    val senderNumber: String
)

data class VerificationSms(
    @Json(name = "sim")
    val simSlot: String,
    @Json(name = "sim_iccid")
    val simIccid: String,
    @Json(name = "verification_code")
    val verificationCode: String?,
    @Json(name = "unique_id")
    val uniqueId: String? = SmsBlockerDatabase.deviceID
)