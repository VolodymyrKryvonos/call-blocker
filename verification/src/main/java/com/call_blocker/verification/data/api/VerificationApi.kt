package com.call_blocker.verification.data.api

import com.call_blocker.verification.data.model.CheckSimCardRequest
import com.call_blocker.verification.data.model.CheckSimCardResponse
import com.call_blocker.verification.data.model.ConfirmSimCardVerificationRequest
import com.call_blocker.verification.data.model.ValidateSimCardRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationApi {

    @POST("apps/validate-sim-card")
    suspend fun validateSimCard(@Body validateSimCardRequest: ValidateSimCardRequest)

    @POST("apps/get-number-info")
    suspend fun checkSimCard(@Body checkSimCardRequest: CheckSimCardRequest): CheckSimCardResponse

    @POST("apps/confirm-validation")
    suspend fun confirmSimCardVerification(@Body body: ConfirmSimCardVerificationRequest)

}