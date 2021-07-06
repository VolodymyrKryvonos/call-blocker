package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.ApiResponse
import com.call_blocke.a_repository.model.LoginRequest
import com.call_blocke.a_repository.model.LoginResponse
import com.call_blocke.a_repository.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface UserRest {

    @POST("login")
    suspend fun signIn(@Body model: LoginRequest): ApiResponse<LoginResponse>

    @POST("register")
    suspend fun signUp(@Body model: RegisterRequest): ApiResponse<LoginResponse>

}