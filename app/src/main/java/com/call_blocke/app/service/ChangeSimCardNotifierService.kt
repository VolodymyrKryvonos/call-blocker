package com.call_blocke.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.TelephonyManager
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocker.model.NewLimits
import com.example.common.CountryCodeExtractor
import com.example.common.SimUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ChangeSimCardNotifierService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    private var notificationJob: Job? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationJob?.cancel()
        notificationJob = launch {
            delay(10 * 1000)
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCode =
                CountryCodeExtractor.getCountryCode(
                    SimUtil.getSIMInfo(this@ChangeSimCardNotifierService),
                    tm
                )
            val firstSim = SimUtil.firstSim(this@ChangeSimCardNotifierService)
            val secondSim = SimUtil.secondSim(this@ChangeSimCardNotifierService)
            setLimits(
                RepositoryImp.settingsRepository.changeSimCard(
                    firstSim?.iccId,
                    secondSim?.iccId,
                    firstSim?.carrierName?.toString(),
                    secondSim?.carrierName?.toString(),
                    countryCode
                )
            )
        }
        return START_NOT_STICKY
    }

    private fun setLimits(newLimits: NewLimits?) {
        newLimits ?: return
        SmsBlockerDatabase.smsPerDaySimFirst = newLimits.firstSimLimit
        SmsBlockerDatabase.smsPerDaySimSecond = newLimits.secondSimLimit
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

}