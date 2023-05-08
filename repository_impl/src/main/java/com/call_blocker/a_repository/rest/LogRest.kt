package com.call_blocker.a_repository.rest

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LogRest {

    @Multipart
    @POST("apps/logs")
    suspend fun sendLogs(
        @Part("unique_id") deviceId: RequestBody,
        @Part file: MultipartBody.Part
    )

}