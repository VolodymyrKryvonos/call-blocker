package com.call_blocker.a_repository.rest

import com.call_blocker.a_repository.model.UssdResult
import retrofit2.http.Body
import retrofit2.http.POST

interface UssdRest {
    @POST("ussd/store")
    suspend fun storeUssdResult(@Body ussdResult: UssdResult)
}