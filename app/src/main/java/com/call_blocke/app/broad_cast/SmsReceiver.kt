package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp.replyRepository
import com.rokobit.adstvv_unit.loger.SmartLog
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
        val smsText = StringBuilder()
        var senderNumber = ""
        var receivedDate = 0L
        var currentSMS: SmsMessage?
        for (aObject in pduObjects) {
            currentSMS = aObject?.let { getIncomingMessage(it, bundle) }
            senderNumber = currentSMS!!.displayOriginatingAddress
            SmartLog.e("SMS part $senderNumber: ${currentSMS.displayMessageBody}")
            val isExist = SmsBlockerDatabase.phoneNumberDao.isExist(senderNumber) > 0
            if (!isExist)
                return
            receivedDate = currentSMS.timestampMillis
            smsText.append(currentSMS.displayMessageBody)
        }
        SmartLog.e("SmsReceiver $smsText $senderNumber")
        replyRepository.storeReply(senderNumber, smsText.toString(), receivedDate)
    }

    private fun getIncomingMessage(aObject: Any, bundle: Bundle): SmsMessage? {
        val format = bundle.getString("format")
        return SmsMessage.createFromPdu(aObject as ByteArray, format)
    }

}