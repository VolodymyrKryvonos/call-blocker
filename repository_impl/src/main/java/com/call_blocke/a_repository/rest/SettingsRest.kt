package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.dto.ConnectionStatusDto
import com.call_blocke.a_repository.dto.ProfileDto
import com.call_blocke.a_repository.dto.SimInfoDto
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.request.GetProfileRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface SettingsRest {

    @POST("users/sms-per-day")
    suspend fun setSmsPerDay(@Body model: SmsPerDayRequest)


    @POST("apps/reset-count-sim")
    suspend fun resetSim(@Body model: RefreshDataForSimRequest)

    @POST("apps/get-sim-info")
    suspend fun simInfo(@Body model: SimInfoRequest): SimInfoDto

    @POST("apps/stop-service")
    suspend fun stopService(@Body model: SimpleBody = SimpleBody())

    @POST("apps/is-connected")
    suspend fun checkConnection(@Body model: SimpleBody = SimpleBody()): ConnectionStatusDto

    @POST("apps/get-profile")
    suspend fun getProfile(@Body model: GetProfileRequest): ApiResponse<ProfileDto>

    @POST("apps/signal-strength")
    suspend fun sendSignalStrengthInfo(@Body signalStrengthBody: SignalStrengthRequest)

    @POST("apps/change-sim")
    suspend fun changeSimCard(@Body body: ChangeSimCardRequest): ChangSimCardResponse
}