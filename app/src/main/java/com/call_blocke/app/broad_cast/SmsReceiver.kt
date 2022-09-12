package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class SmsReceiver : BroadcastReceiver() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle?
        SmsBlockerDatabase.init(context)
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            bundle = intent.extras
            if (bundle != null) {
                val pduObjects = bundle["pdus"] as Array<*>?
                if (pduObjects != null) {
                    coroutineScope.launch {
                        storeReply(pduObjects, bundle)
                    }
                    abortBroadcast()
                }
            }
        }
    }

    private suspend fun storeReply(pduObjects: Array<*>, bundle: Bundle) {
        val currentSMS = getIncomingMessage(pduObjects, bundle)
        SmartLog.e("storeReply  ${currentSMS.toString()}")
        val sendToReply =
            SmsBlockerDatabase.phoneNumberDao.isExist(currentSMS.senderNumber) > 0 || try {
                Gson().fromJson(currentSMS.smsText, SmsDetectorBody::class.java)
                true
            } catch (e: JsonSyntaxException) {
                SmartLog.e(getStackTrace(e))
                e.printStackTrace()
                false
            }
        if (!sendToReply)
            return
        SmartLog.e("SmsReceiver ${currentSMS.smsText} ${currentSMS.senderNumber}")
        replyRepository.storeReply(
            currentSMS.senderNumber,
            currentSMS.smsText,
            currentSMS.receiveDate
        )
    }

    private fun getIncomingMessage(pduObjects: Array<*>, bundle: Bundle): ReplyMessage {
        val smsText = StringBuilder()
        var senderNumber = ""
        var receivedDate = 0L
        val format = bundle.getString("format")
        for (aObject in pduObjects) {
            val currentFrame = SmsMessage.createFromPdu(aObject as ByteArray, format)
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

data class SmsDetectorBody(
    @SerializedName("sim_iccid")
    val simIccid: String,
    @SerializedName("unique_id")
    val uniqueId: String
)