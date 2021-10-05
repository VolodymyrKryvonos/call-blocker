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

    private fun simInfo(context: Context): Pair<Int, SubscriptionInfo>? {
        val simList = SimUtil.getSIMInfo(context)

        if (simList.isEmpty())
            return null

        if (simList.size == 1)
            return Pair(0, simList[0])

        val pair = Pair(
            SmsBlockerDatabase.lastSimSlotUsed,
            simList[SmsBlockerDatabase.lastSimSlotUsed]
        )

        SmsBlockerDatabase.lastSimSlotUsed.let {
            if (it == 0)
                SmsBlockerDatabase.lastSimSlotUsed = 1
            else
                SmsBlockerDatabase.lastSimSlotUsed = 0
        }

        return pair
    }

    @DelicateCoroutinesApi
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null)
            return

        val extras = intent.extras

        GlobalScope.launch(Dispatchers.IO) {
            val smsExtras = extras!!["pdus"] as Array<*>

            for (smsExtra in smsExtras) {
                val smsMessage = SmsMessage.createFromPdu(smsExtra as ByteArray)

                val replay = taskRepository.findReplay(
                    smsMessage.originatingAddress.toString(),
                    smsMessage.messageBody
                ) ?: continue

                val simInfo: SubscriptionInfo = simInfo(context = context)!!.second

                sendSms(
                    context = context,
                    simInfo = simInfo,
                    address = replay.rOutMsisdn,
                    text = replay.rTextReply
                )
            }
        }
    }

}