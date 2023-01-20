package com.call_blocker.verification.domain

import com.call_blocker.verification.data.VerificationRepository
import com.example.common.Resource
import kotlinx.coroutines.flow.collectLatest

class SimCardVerifier {
    private val verificationRepository: VerificationRepository = VerificationRepositoryImpl()

    suspend fun verifySimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ) {
        verificationRepository.verifySimCard(
            phoneNumber,
            simID,
            simSlot
        ).collectLatest {
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(simSlot)
            if (it is Resource.Success) {
                stateHolder.emit(
                    VerificationInfo(
                        status = VerificationStatus.Processing,
                        simId = simID
                    )
                )
            }
        }
    }


    suspend fun confirmVerification(
        simID: String,
        simSlot: Int,
        verificationCode: String,
        phoneNumber: String
    ) {
        verificationRepository.confirmVerification(
            simID,
            simSlot,
            verificationCode,
            phoneNumber
        ).collectLatest {
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(simSlot)
            if (it is Resource.Success) {
                stateHolder.emit(
                    VerificationInfo(
                        status = VerificationStatus.Processing,
                        simId = simID
                    )
                )
            }
        }
    }
}