package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.app.util.Const
import com.call_blocke.app.util.NotificationService
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.ValidationState
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.call_blocke.rest_work_imp.model.Resource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
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
        val currentSMS = getIncomingMessage(pduObjects, bundle)
        if (!checkIsVerificationSms(currentSMS, context)) {
            storeReply(currentSMS)
        }
    }

    private suspend fun checkIsVerificationSms(sms: ReplyMessage, context: Context): Boolean {
        val verificationSms = try {
            Gson().fromJson(sms.smsText, VerificationSms::class.java)
        } catch (_: JsonSyntaxException) {
            return false
        }
        RepositoryImp.settingsRepository.confirmValidation(
            simSlot = verificationSms.simSlot,
            iccid = verificationSms.simIccid,
            verificationCode = verificationSms.verificationCode ?: ""
        ).collectLatest {
            if (verificationSms.uniqueId == SmsBlockerDatabase.deviceID) {
                if (it is Resource.Success) {
                    emitValidationCompletion(verificationSms.simSlot, ValidationState.SUCCESS)
                    NotificationService.showPhoneNumberVerifiedNotification(
                        sms.senderNumber,
                        context,
                        verificationSms.simSlot.last().digitToIntOrNull() ?: -1
                    )
                    return@collectLatest
                }
                if (it is Resource.Error) {
                    emitValidationCompletion(verificationSms.simSlot, ValidationState.FAILED)
                }
            }
        }
        return true
    }

    private suspend fun emitValidationCompletion(simSlot: String, status: ValidationState) {
        if (simSlot == Const.firstSim) {
            SmsBlockerDatabase.firstSimValidationState.emit(status)
        } else {
            SmsBlockerDatabase.secondSimValidationState.emit(status)
        }
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
    @SerializedName("sim")
    val simSlot: String,
    @SerializedName("sim_iccid")
    val simIccid: String,
    @SerializedName("verification_code")
    val verificationCode: String?,
    @SerializedName("unique_id")
    val uniqueId: String
)