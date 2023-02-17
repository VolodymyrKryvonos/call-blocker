package com.call_blocke.app.util

import android.content.Context
import android.telephony.SubscriptionInfo
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.SimVerificationInfo
import com.call_blocke.rest_work_imp.model.SimVerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface SimCardVerificationChecker {
    val settingsRepository: SettingsRepository

    val firstSimVerificationInfo: MutableStateFlow<SimVerificationInfo>

    val secondSimVerificationInfo: MutableStateFlow<SimVerificationInfo>

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


class SimCardVerificationCheckerImpl : SimCardVerificationChecker {
    override val settingsRepository: SettingsRepository = RepositoryImp.settingsRepository
    override val firstSimVerificationInfo =
        MutableStateFlow(SimVerificationInfo(SimVerificationStatus.UNKNOWN))
    override val secondSimVerificationInfo =
        MutableStateFlow(SimVerificationInfo(SimVerificationStatus.UNKNOWN))
    override var coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob()
    }

    @Suppress("LABEL_NAME_CLASH")
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
            subscriptionInfo.iccId,
            subscriptionInfo.simSlotIndex,
            firstSimVerificationInfo,
            SmsBlockerDatabase.firstSimVerificationState,
            subscriptionInfo.number?.ifEmpty { null },
            createAutoVerificationSms
        )
    }

    private suspend fun checkSecondSim(
        subscriptionInfo: SubscriptionInfo,
        createAutoVerificationSms: Boolean = false
    ) {
        settingsRepository.checkSim(
            subscriptionInfo.iccId,
            subscriptionInfo.simSlotIndex,
            secondSimVerificationInfo,
            SmsBlockerDatabase.secondSimVerificationState,
            subscriptionInfo.number?.ifEmpty { null },
            createAutoVerificationSms
        )
    }

}