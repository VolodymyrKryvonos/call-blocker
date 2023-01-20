package com.call_blocker.verification.data

import com.call_blocker.verification.data.model.CheckSimCardResponse
import com.example.common.Resource
import kotlinx.coroutines.flow.Flow

interface VerificationRepository {

    fun checkSimCard(
        iccId: String,
        simSlot: Int,
        phoneNumber: String?
    ): Flow<Resource<CheckSimCardResponse>>

    fun confirmVerification(
        iccid: String,
        simSlot: Int,
        verificationCode: String,
        phoneNumber: String
    ): Flow<Resource<Unit>>

    fun verifySimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ): Flow<Resource<Unit>>
}