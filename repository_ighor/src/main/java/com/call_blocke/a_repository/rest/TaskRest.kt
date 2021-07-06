package com.call_blocke.a_repository.rest

import com.call_blocke.a_repository.model.ApiResponse
import com.call_blocke.a_repository.model.TaskResponse
import com.call_blocke.a_repository.model.TaskStatusRequest
import com.call_blocke.a_repository.model.TasksRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskRest {

    @POST("tasks/find")
    suspend fun tasks(@Body data: TasksRequest): ApiResponse<List<TaskResponse>>

    @POST("sms/deliver")
    suspend fun confirmStatus(@Body data: TaskStatusRequest): ApiResponse<String>

}