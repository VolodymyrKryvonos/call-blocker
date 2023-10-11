package com.call_blocker.a_repository.rest

import com.call_blocker.a_repository.dto.UserInfoDto
import com.call_blocker.a_repository.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface UserRest {

    @POST("login")
    suspend fun signIn(@Body model: LoginRequest): ApiResponse<LoginResponse>

    @POST("reset")
    suspend fun reset(@Body model: ResetRequest): ApiResponse<ResetResponse>

    @POST("register")
    suspend fun signUp(@Body model: RegisterRequest): ApiResponse<LoginResponse>

    @POST("users/info")
    suspend fun userInfo(@Body mode: TasksRequest): UserInfoDto

}