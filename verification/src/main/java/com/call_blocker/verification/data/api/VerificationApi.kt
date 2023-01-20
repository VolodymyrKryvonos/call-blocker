package com.call_blocker.verification.data.api

import com.call_blocker.verification.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationApi {

    @POST("apps/validate-sim-card")
    suspend fun verifySimCard(@Body validateSimCardRequest: VerifySimCardRequest)

    @POST("apps/get-number-info")
    suspend fun checkSimCard(@Body checkSimCardRequest: CheckSimCardRequest): CheckSimCardResponse

    @POST("apps/confirm-validation")
    suspend fun confirmSimCardVerification(@Body body: ConfirmSimCardVerificationRequest)

    @POST("apps/start-auto-verification")
    suspend fun startAutoVerification(@Body autoVerificationRequest: AutoVerificationRequest)
}