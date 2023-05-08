package com.call_blocker.a_repository.rest

import com.call_blocker.a_repository.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskRest {


    @POST("sms/deliver")
    suspend fun confirmStatus(@Body data: ConfirmStatusRequest)

    @POST("apps/re-send-sms")
    suspend fun getReceivedMessagesID(@Body data: ResendUnprocessedRequest)

}