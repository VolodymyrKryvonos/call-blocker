package com.call_blocke.app.util

import android.content.Context
import android.telephony.SubscriptionInfo
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface SimCardValidationChecker {
    val settingsRepository: SettingsRepository

    val firstSimValidationInfo: MutableStateFlow<SimValidationInfo>

    val secondSimValidationInfo: MutableStateFlow<SimValidationInfo>

    var coroutineScope: CoroutineScope

    fun checkSimCards(
        context: Context
    )

    fun checkSimCard(
        index: Int,
        context: Context,
        createAutoVerificationSms: Boolean = false
    )
}


class SimCardValidationCheckerImpl(
    override val settingsRepository: SettingsRepository
) : SimCardValidationChecker {

    override val firstSimValidationInfo =
        MutableStateFlow(SimValidationInfo(SimValidationStatus.UNKNOWN, ""))
    override val secondSimValidationInfo =
        MutableStateFlow(SimValidationInfo(SimValidationStatus.UNKNOWN, ""))
    override var coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob()
    }

    override fun checkSimCards(context: Context) {
        coroutineScope.launch(Dispatchers.IO) {
            launch {
                val simCard = SimUtil.firstSim(context) ?: return@launch
                checkFirstSim(simCard)
            }
            launch {
                val simCard = SimUtil.secondSim(context) ?: return@launch
                checkSecondSim(simCard)
            }
        }
    }

    override fun checkSimCard(index: Int, context: Context, createAutoVerificationSms: Boolean) {
        coroutineScope.launch {
            if (index == 0) {
                val simCard = SimUtil.firstSim(context) ?: return@launch
                checkFirstSim(simCard, createAutoVerificationSms)
            } else {
                val simCard = SimUtil.secondSim(context) ?: return@launch
                checkSecondSim(simCard, createAutoVerificationSms)
            }
        }
    }

    private suspend fun checkFirstSim(
        subscriptionInfo: SubscriptionInfo,
        createAutoVerificationSms: Boolean = false
    ) {
        settingsRepository.checkSim(
            subscriptionInfo,
            firstSimValidationInfo,
            SmsBlockerDatabase.firstSimValidationState,
            createAutoVerificationSms
        )
    }

    private suspend fun checkSecondSim(
        subscriptionInfo: SubscriptionInfo,
        createAutoVerificationSms: Boolean = false
    ) {
        settingsRepository.checkSim(
            subscriptionInfo,
            secondSimValidationInfo,
            SmsBlockerDatabase.secondSimValidationState,
            createAutoVerificationSms
        )
    }

}