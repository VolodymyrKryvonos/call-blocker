package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class TaskStatusRequest(
    val id: Int,
    val error: String,
    @SerializedName("delivery_status")
    val statusCode: Int, //1-0
    val simId: String
)
