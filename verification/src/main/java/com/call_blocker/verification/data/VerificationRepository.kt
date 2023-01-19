package com.call_blocker.verification.data

import com.call_blocker.verification.data.model.SimVerificationInfo
import com.example.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface VerificationRepository {
    suspend fun checkSim(
        simId: String,
        simSlot: Int,
        simVerificationInfo: MutableStateFlow<SimVerificationInfo>,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean
    )

    fun checkSimCard(
        iccId: String,
        simSlot: Int,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean = false
    ): Flow<SimVerificationInfo>

    fun confirmVerification(
        iccid: String,
        simSlot: String,
        verificationCode: String,
        phoneNumber: String,
        uniqueId: String
    ): Flow<Resource<Unit>>

    fun validateSimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ): Flow<Resource<Unit>>
}