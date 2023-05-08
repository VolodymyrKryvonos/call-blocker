package com.call_blocker.verification.data

import android.content.Context
import com.call_blocker.common.Resource
import com.call_blocker.verification.data.model.CheckSimCardResponse
import kotlinx.coroutines.flow.Flow

interface VerificationRepository {

    fun checkSimCard(
        context: Context,
        simSlot: Int,
    ): Flow<Resource<CheckSimCardResponse>>

    fun confirmVerification(
        iccid: String,
        verificationCode: String,
        phoneNumber: String
    ): Flow<Resource<Unit>>

    fun verifySimCard(
        context: Context,
        simSlot: Int
    ): Flow<Resource<Unit>>
}