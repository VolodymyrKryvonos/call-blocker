package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.*
import retrofit2.http.Body
import retrofit2.http.GET
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
}