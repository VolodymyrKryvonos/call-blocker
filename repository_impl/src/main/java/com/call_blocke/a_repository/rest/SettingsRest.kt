package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.dto.ConnectionStatusDto
import com.call_blocke.a_repository.dto.ProfileDto
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.request.GetProfileRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface SettingsRest {

    @POST("users/sms-per-day")
    suspend fun setSmsPerDay(@Body model: SmsPerDayRequest)

    @POST("users/black-list")
    suspend fun blackList(@Body data: TasksRequest): ApiResponse<List<BlackListElement>>

    @POST("apps/reset-count-sim")
    suspend fun resetSim(@Body model: RefreshDataForSimRequest)

    @POST("apps/get-sim-info")
    suspend fun simInfo(@Body model: SimInfoRequest = SimInfoRequest()): ApiResponse<SimInfoResponse>

    @POST("apps/stop-service")
    suspend fun stopService(@Body model: SimInfoRequest = SimInfoRequest())

    @POST("apps/is-connected")
    suspend fun checkConnection(@Body model: SimInfoRequest = SimInfoRequest()): ConnectionStatusDto

    @POST("apps/get-profile")
    suspend fun getProfile(@Body model: GetProfileRequest): ApiResponse<ProfileDto>

    @POST("apps/validate-sim-card")
    suspend fun validateSimCard(@Body validateSimCardRequest: ValidateSimCardRequest)

    @POST("apps/get-number-info")
    suspend fun checkSimCard(@Body checkSimCardRequest: CheckSimCardRequest): CheckSimCardResponse

    @POST("apps/confirm-validation")
    suspend fun confirmSimCardValidation(@Body confirmSimCardValidationRequest: ConfirmSimCardValidationRequest)

    @POST("apps/signal-strength")
    suspend fun sendSignalStrengthInfo(@Body signalStrengthBody: SignalStrengthRequest)
}