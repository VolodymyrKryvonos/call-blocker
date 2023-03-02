package com.call_blocke.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.call_blocke.app.service.ChangeSimCardNotifierService
import com.call_blocke.app.worker_manager.SendingSMSWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace

enum class SimSlotState {
    ABSENT,
    LOCKED,
    READY,
    IMSI,
    LOADED,
    NOT_READY,
}

class SimSlotReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            SmartLog.d("SimSlotReceiver")
            when (intent?.getStringExtra("ss")) {
                SimSlotState.ABSENT.name -> {
                    trackSimCardWasEjected(context)
                }
                SimSlotState.IMSI.name -> {
                    trackSimCardWasIdentified(context)
                }
            }
        } catch (e: Exception) {
            SmartLog.e(getStackTrace(e))
        }
    }

    private fun trackSimCardWasIdentified(context: Context?) {
        SendingSMSWorker.stop(context = context ?: return)
        val firstSimId = SimUtil.firstSimId(context)
        val secondSimId = SimUtil.secondSimId(context)
        if (firstSimId != SmsBlockerDatabase.firstSimId) {
            SmartLog.e("Sim1 was changed oldId = ${SmsBlockerDatabase.firstSimId} newId = $firstSimId")
            SmsBlockerDatabase.firstSimId = firstSimId
            SmsBlockerDatabase.firstSimChanged = true
        }
        if (secondSimId != SmsBlockerDatabase.secondSimId) {
            SmartLog.e("Sim2 was changed oldId = ${SmsBlockerDatabase.secondSimId} newId = $secondSimId")
            SmsBlockerDatabase.secondSimId = secondSimId
            SmsBlockerDatabase.secondSimChanged = true
        }
        ChangeSimCardNotifierService.startService(context ?: return)
    }

    private fun trackSimCardWasEjected(context: Context?) {
        SendingSMSWorker.stop(context = context ?: return)
    }
}