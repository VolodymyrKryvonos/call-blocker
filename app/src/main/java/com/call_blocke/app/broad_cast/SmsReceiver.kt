package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.call_blocker.verification.domain.SimCardVerifier
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException


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
                        processSms(pduObjects, bundle)
                    }
                }
            }
        }
    }

    private suspend fun processSms(pduObjects: Array<*>, bundle: Bundle) {
        val currentSMS = getIncomingMessage(pduObjects, bundle)

        if (!checkIsVerificationSms(currentSMS)) {
            storeReply(currentSMS)
        }
    }

    private suspend fun checkIsVerificationSms(sms: ReplyMessage): Boolean {
        SmartLog.e("checkIsVerificationSms $sms")
        val verificationSms = try {
            moshi.adapter(VerificationSms::class.java).fromJson(sms.smsText)
        } catch (e: IOException) {
            SmartLog.e(getStackTrace(e))
            return false
        }
        verificationSms ?: return false

        SmartLog.e("verificationSms $verificationSms")
        val verifier = SimCardVerifier()
        verifier.confirmVerification(
            simID = verificationSms.simIccid,
            verificationCode = verificationSms.verificationCode ?: "",
            phoneNumber = sms.senderNumber,
            simSlot = if (verificationSms.simSlot == "msisdn_1") {
                0
            } else {
                1
            }
        )
        return true
    }

    private suspend fun storeReply(sms: ReplyMessage) {
        val sendToReply =
            SmsBlockerDatabase.phoneNumberDao.isExist(sms.senderNumber) > 0
        if (!sendToReply)
            return
        SmartLog.e("storeReply ${sms.smsText} ${sms.senderNumber}")
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