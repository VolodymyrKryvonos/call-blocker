package com.call_blocker.verification.domain

import android.content.Context
import com.call_blocker.verification.data.VerificationRepository
import com.example.common.Resource
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.flow.collectLatest

class SimCardVerifier {
    private val verificationRepository: VerificationRepository = VerificationRepositoryImpl()

    suspend fun verifySimCard(
        context: Context,
        simSlot: Int
    ) {
        verificationRepository.verifySimCard(
            context,
            simSlot
        ).collectLatest {
            val stateHolder = VerificationInfoStateHolder.getStateHolderBySimSlotIndex(simSlot)
            if (it is Resource.Success) {
                stateHolder.emit(
                    VerificationInfo(
                        status = VerificationStatus.Processing,
                        simId = SimUtil.simInfo(context, simSlot)?.iccId ?: ""
                    )
                )
            }
        }
    }


    suspend fun confirmVerification(
        simID: String,
        verificationCode: String,
        phoneNumber: String
    ) {
        verificationRepository.confirmVerification(
            simID,
            verificationCode,
            phoneNumber
        ).collectLatest {
            SmartLog.e("Confirm verification state, $it")
        }
    }
}