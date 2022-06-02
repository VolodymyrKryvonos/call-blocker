package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.ReplyBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ReplyRest {
    @POST("replays/store")
    suspend fun sendReply(@Body body: ReplyBody)
}