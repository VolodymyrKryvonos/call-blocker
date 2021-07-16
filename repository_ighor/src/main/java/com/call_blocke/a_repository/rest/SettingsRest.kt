package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.ApiResponse
import com.call_blocke.a_repository.model.BlackListElement
import com.call_blocke.a_repository.model.SmsPerDayRequest
import com.call_blocke.a_repository.model.TasksRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SettingsRest {

    @POST("users/sms-per-day")
    suspend fun setSmsPerDay(@Body model: SmsPerDayRequest): ApiResponse<String>

    @POST("users/black-list")
    suspend fun blackList(@Body data: TasksRequest): ApiResponse<List<BlackListElement>>

}