package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.telephony.SubscriptionInfo
import com.call_blocke.app.service.sendSms
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val taskRepository = RepositoryImp.taskRepository

    private var lastSimSlot
        get() = SmsBlockerDatabase.lastSimSlotUsed
        set(value) {
            SmsBlockerDatabase.lastSimSlotUsed = value
        }

    @DelicateCoroutinesApi
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent!!.extras

        /*GlobalScope.launch(Dispatchers.IO) {
            val smsExtras = extras!!["pdus"] as Array<*>

            for (smsExtra in smsExtras) {
                val smsMessage = SmsMessage.createFromPdu(smsExtra as ByteArray)

                val replay = taskRepository.findReplay(
                    smsMessage.originatingAddress.toString(),
                    smsMessage.messageBody
                )

                val simInfo: SubscriptionInfo = SimUtil.getSIMInfo(context)[lastSimSlot]

                if (context != null) {
                    updateSimSlot(context)

                    sendSms(
                        context = context,
                        simInfo = simInfo,
                        address = replay.rOutMsisdn,
                        text = replay.rTextReply
                    )

                }
            }
        }*/

    }

    private fun updateSimSlot(context: Context) {
        val sims = SimUtil.getSIMInfo(context)

        if (sims.size > 1) {
            lastSimSlot = if (lastSimSlot == 0) 1 else 0
        }

        lastSimSlot = 0
    }
}