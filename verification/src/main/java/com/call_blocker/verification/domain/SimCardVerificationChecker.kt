package com.call_blocker.verification.domain

import android.content.Context
import com.call_blocker.verification.data.VerificationRepository
import com.example.common.Resource
import com.example.common.SimUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface SimCardVerificationChecker {
    val verificationRepository: VerificationRepository

    var coroutineScope: CoroutineScope

    fun checkSimCards(
        context: Context
    )

    suspend fun checkFirstSim(
        context: Context
    )

    suspend fun checkSecondSim(
        context: Context
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
                checkFirstSim(context)
            }
            launch {
                checkSecondSim(context)
            }
        }
    }

    private fun checkSimCard(index: Int, context: Context) {
        coroutineScope.launch {
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
            verificationRepository.checkSimCard(
                context, index
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
                                simId = SimUtil.simInfo(context, index)?.iccId ?: "",
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
        context: Context
    ) {
        checkSimCard(0, context)
    }

    override suspend fun checkSecondSim(
        context: Context
    ) {
        checkSimCard(1, context)
    }

    override fun checkSimCardByIndex(index: Int, context: Context) {
        coroutineScope.launch {
            if (index == 0) {
                checkFirstSim(context)
            } else {
                checkSecondSim(context)
            }
        }
    }

    override fun waitForVerification(index: Int, context: Context) {
        waitForVerificationJob = coroutineScope.launch(Dispatchers.IO) {
            repeat(5) {
                delay(30 * 1000)
                checkSimCard(index, context)
            }
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
            stateHolder.emit(stateHolder.value.copy(status = VerificationStatus.Failed))
        }
    }

}