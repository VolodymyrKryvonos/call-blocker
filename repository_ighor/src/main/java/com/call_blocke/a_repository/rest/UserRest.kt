package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface UserRest {

    @POST("login")
    suspend fun signIn(@Body model: LoginRequest): ApiResponse<LoginResponse>

    @POST("register")
    suspend fun signUp(@Body model: RegisterRequest): ApiResponse<LoginResponse>

    @POST("users/info")
    suspend fun userInfo(@Body mode: TasksRequest): ApiResponse<UserInfo>

}