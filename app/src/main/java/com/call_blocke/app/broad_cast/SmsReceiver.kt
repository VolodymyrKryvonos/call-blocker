package com.call_blocke.app.broad_cast

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.app.EVENT_NOTIFICATION_CHANNEL_ID
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.call_blocke.repository.RepositoryImp.settingsRepository
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
        return try {
            val verificationSms = Gson().fromJson(sms.smsText, VerificationSms::class.java)
            settingsRepository.confirmValidation(
                simSlot = verificationSms.simSlot,
                iccid = verificationSms.simIccid,
                verificationCode = verificationSms.verificationCode
            ).collectLatest {
                if (it is Resource.Success) {
                    showPhoneNumberVerifiedNotification(sms.senderNumber, context)
                    SmsBlockerDatabase.isValidationCompleted.emit(true)
                    return@collectLatest
                }
                if (it is Resource.Error) {
                    SmsBlockerDatabase.isValidationCompleted.emit(true)
                }
            }
            true
        } catch (_: JsonSyntaxException) {
            false
        }
    }

    private fun showPhoneNumberVerifiedNotification(
        phoneNumber: String,
        context: Context
    ) {
        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, EVENT_NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }
        notificationBuilder.setContentTitle(context.getString(R.string.verification_completed))
            .setContentText(context.getString(R.string.your_verification_completed, phoneNumber))
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_EVENT)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        notificationManager.notify(785, notificationBuilder.build())
    }

    private suspend fun storeReply(sms: ReplyMessage) {
        SmartLog.e("storeReply $sms")
        val sendToReply =
            SmsBlockerDatabase.phoneNumberDao.isExist(sms.senderNumber) > 0
        if (!sendToReply)
            return
        SmartLog.e("SmsReceiver ${sms.smsText} ${sms.senderNumber}")
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
            SmartLog.e("SimReceiverID: ${currentFrame.indexOnIcc}")
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
    val verificationCode: String
)