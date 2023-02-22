package com.call_blocker.verification.domain

import android.content.Context
import android.telephony.SubscriptionInfo
import com.call_blocker.verification.data.VerificationRepository
import com.example.common.Resource
import com.example.common.SimUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.coroutines.CoroutineContext

interface SimCardVerificationChecker {
    val verificationRepository: VerificationRepository

    var coroutineScope: CoroutineScope

    fun checkSimCards(
        context: Context
    )

    suspend fun checkFirstSim(
        subscriptionInfo: SubscriptionInfo
    )

    suspend fun checkSecondSim(
        subscriptionInfo: SubscriptionInfo
    )

    fun checkSimCardByIndex(index: Int, context: Context)
    fun waitForVerification(index: Int, context: Context)
}


class SimCardVerificationCheckerImpl : SimCardVerificationChecker {
    override val verificationRepository: VerificationRepository = VerificationRepositoryImpl()
    override var coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob()
    }

    private var waitForVerificationJob: Job? = null

    override fun checkSimCards(context: Context) {
        coroutineScope.launch(Dispatchers.IO) outerBlock@{
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

    private fun checkSimCard(index: Int, subscriptionInfo: SubscriptionInfo) {
        coroutineScope.launch {
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
            verificationRepository.checkSimCard(
                subscriptionInfo.iccId,
                subscriptionInfo.simSlotIndex,
                subscriptionInfo.number?.ifEmpty { null }
            ).collectLatest {
                when (it) {
                    is Resource.Success -> {
                        val newStatus = VerificationStatus.getUpdatedStatus(
                            it.data?.status == true,
                            stateHolder.value.status
                        )
                        stateHolder.emit(
                            stateHolder.value.copy(
                                status = newStatus,
                                simId = subscriptionInfo.iccId,
                                isAutoVerificationEnabled = it.data?.autoVerification == true,
                                phoneNumber = it.data?.number
                            )
                        )
                        if (newStatus == VerificationStatus.Verified) {
                            waitForVerificationJob?.cancel()
                        }
                    }
                    else -> Unit
                }
                VerificationInfoStateHolder.checkIsAutoVerificationEnabled()
            }
        }
    }

    override suspend fun checkFirstSim(
        subscriptionInfo: SubscriptionInfo
    ) {
        checkSimCard(0, subscriptionInfo)
    }

    override suspend fun checkSecondSim(
        subscriptionInfo: SubscriptionInfo
    ) {
        checkSimCard(1, subscriptionInfo)
    }

    override fun checkSimCardByIndex(index: Int, context: Context) {
        val sim = SimUtil.simInfo(context, index) ?: return
        coroutineScope.launch {
            if (index == 0) {
                checkFirstSim(sim)
            } else {
                checkSecondSim(sim)
            }
        }
    }

    override fun waitForVerification(index: Int, context: Context) {
        waitForVerificationJob = coroutineScope.launch(Dispatchers.IO) {
            val simInfo = SimUtil.simInfo(context, index) ?: return@launch
            repeat(5) {
                delay(30 * 1000)
                checkSimCard(index, simInfo)
            }
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
            stateHolder.emit(stateHolder.value.copy(status = VerificationStatus.Failed))
        }
    }

}