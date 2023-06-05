package com.call_blocker.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.telephony.SubscriptionInfo
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.repository.RepositoryImp.replyRepository
import com.call_blocker.verification.domain.SimCardVerifier
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class SmsReceiver : BroadcastReceiver() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
        val currentSMS = getIncomingMessage(pduObjects, bundle, context)
        SmartLog.e("processSms $currentSMS")
        storeReply(currentSMS)
        checkIsVerificationSms(currentSMS)
    }

    private suspend fun checkIsVerificationSms(sms: ReplyMessage): Boolean {
        SmartLog.e("checkIsVerificationSms $sms")

        if (!sms.smsText.startsWith("thLpR5")) {
            return false
        }

        val verificationWords = sms.smsText.split(" ")

        val verificationSms = VerificationSms("", verificationWords[1], verificationWords[2])

        SmartLog.e("verificationSms $verificationSms")
        val verifier = SimCardVerifier()
        verifier.confirmVerification(
            simID = verificationSms.simIccid,
            verificationCode = verificationSms.verificationCode ?: "",
            phoneNumber = sms.senderNumber,
        )
        return true
    }

    private suspend fun storeReply(sms: ReplyMessage) {
        SmartLog.e("storeReply ${sms.smsText} ${sms.senderNumber}")
        replyRepository.storeReply(
            sms.senderNumber,
            sms.smsText,
            sms.receiveDate,
            sms.simIccid,
            sms.simSlot
        )
    }

    private fun getIncomingMessage(
        pduObjects: Array<*>,
        bundle: Bundle,
        context: Context
    ): ReplyMessage {
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

        bundle.keySet().forEach {
            SmartLog.e("Bundle sms $it: ${bundle[it]}")
        }
        val simInfo = getSimSubscriptionInfo(bundle, context)

        return ReplyMessage(
            receivedDate,
            smsText.toString(),
            senderNumber,
            simInfo?.iccId,
            simInfo?.simSlotIndex
        )
    }

    private fun getSimSubscriptionInfo(bundle: Bundle, context: Context): SubscriptionInfo? {
        val subscriptionId = bundle.getInt("subscription", -1)
        val subscriptionIndex = bundle.getInt("android.telephony.extra.SUBSCRIPTION_INDEX", -1)
        val slotIndex = bundle.getInt("android.telephony.extra.SLOT_INDEX", -1)

        val subscriptionInfoList = SimUtil.getSIMInfo(context)
        when {
            subscriptionId != -1 -> {
                for (subscriptionInfo in subscriptionInfoList ?: emptyList()) {
                    if (subscriptionInfo.subscriptionId == subscriptionId) {
                        return subscriptionInfo
                    }
                }
            }

            subscriptionIndex != -1 -> {
                for (subscriptionInfo in subscriptionInfoList ?: emptyList()) {
                    if (subscriptionInfo.subscriptionId == subscriptionIndex) {
                        return subscriptionInfo
                    }
                }
            }

            else -> {
                for (subscriptionInfo in subscriptionInfoList ?: emptyList()) {
                    if (subscriptionInfo.simSlotIndex == slotIndex) {
                        return subscriptionInfo
                    }
                }
            }
        }
        return null
    }

}

data class ReplyMessage(
    val receiveDate: Long,
    val smsText: String,
    val senderNumber: String,
    val simIccid: String?,
    val simSlot: Int?
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