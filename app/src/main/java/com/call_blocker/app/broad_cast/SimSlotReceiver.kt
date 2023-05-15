package com.call_blocker.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.call_blocker.app.service.ChangeSimCardNotifierService
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

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
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun trackSimCardWasIdentified(context: Context?) {
        val firstSim = SimUtil.firstSim(context)
        val secondSim = SimUtil.secondSim(context)
        if (firstSim?.iccId != SmsBlockerDatabase.firstSimId) {
            SmartLog.e("Sim1 was changed oldId = ${SmsBlockerDatabase.firstSimId} newId = $firstSim?.iccId")
            SmsBlockerDatabase.firstSimId = firstSim?.iccId
            SmsBlockerDatabase.firstSimChanged = true
        }
        if (secondSim?.iccId != SmsBlockerDatabase.secondSimId) {
            SmartLog.e("Sim2 was changed oldId = ${SmsBlockerDatabase.secondSimId} newId = $secondSim?.iccId")
            SmsBlockerDatabase.secondSimId = secondSim?.iccId
            SmsBlockerDatabase.secondSimChanged = true
        }
        ChangeSimCardNotifierService.startService(context ?: return)
    }

    private fun trackSimCardWasEjected(context: Context?) {
        SendingSMSWorker.stop(context = context ?: return)
    }
}