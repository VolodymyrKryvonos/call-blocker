package com.call_blocker.verification.domain

import kotlinx.coroutines.flow.*

object VerificationInfoStateHolder {
    private val firstSimVerificationInfo = MutableStateFlow(VerificationInfo())
    private val secondSimVerificationInfo = MutableStateFlow(VerificationInfo())

    fun getStateHolderBySimSlotIndex(index: Int): MutableStateFlow<VerificationInfo> {
        return if (index == 0) firstSimVerificationInfo else secondSimVerificationInfo
    }

    fun isSimCardsNeedVerification() = channelFlow {
        firstSimVerificationInfo.combine(secondSimVerificationInfo) { first, second ->
            Pair(first, second)
        }.collectLatest {
            send(it.first.isNeedVerification() || it.second.isNeedVerification())
        }
    }

    fun checkIsFirstSimVerified() =
        firstSimVerificationInfo.map { it.isNeedVerification() }

    fun checkIsSecondSimVerified() =
        secondSimVerificationInfo.map { it.isNeedVerification() }

    suspend fun checkIsAutoVerificationEnabled() {
        firstSimVerificationInfo.emit(
            firstSimVerificationInfo.value
                .copy(
                    isAutoVerificationEnabled = firstSimVerificationInfo.value.isAutoVerificationEnabled ||
                            secondSimVerificationInfo.value.status == VerificationStatus.Verified
                )
        )
        secondSimVerificationInfo.emit(
            secondSimVerificationInfo.value
                .copy(
                    isAutoVerificationEnabled = secondSimVerificationInfo.value.isAutoVerificationEnabled ||
                            firstSimVerificationInfo.value.status == VerificationStatus.Verified
                )
        )
    }
}