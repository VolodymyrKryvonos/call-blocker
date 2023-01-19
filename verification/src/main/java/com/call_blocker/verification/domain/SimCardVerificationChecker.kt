package com.call_blocker.verification.domain

import android.content.Context
import android.telephony.SubscriptionInfo
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.model.SimVerificationInfo
import com.call_blocker.verification.data.model.SimVerificationStatus
import com.example.common.SimUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface SimCardVerificationChecker {
    val verificationRepository: VerificationRepository

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
    override val verificationRepository: VerificationRepository = VerificationRepositoryImpl()
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
        verificationRepository.checkSim(
            subscriptionInfo.iccId,
            subscriptionInfo.simSlotIndex,
            firstSimVerificationInfo,
            subscriptionInfo.number.ifEmpty { null },
            createAutoVerificationSms
        )
    }

    private suspend fun checkSecondSim(
        subscriptionInfo: SubscriptionInfo,
        createAutoVerificationSms: Boolean = false
    ) {
        verificationRepository.checkSim(
            subscriptionInfo.iccId,
            subscriptionInfo.simSlotIndex,
            secondSimVerificationInfo,
            subscriptionInfo.number.ifEmpty { null },
            createAutoVerificationSms
        )
    }

}