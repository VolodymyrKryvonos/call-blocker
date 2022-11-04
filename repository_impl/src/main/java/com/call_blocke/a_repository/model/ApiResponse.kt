package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<DATA>(
    val data: DATA,
    val options: Options
)

data class SocketMessage<DATA>(
    val method: String? = null,
    val data: DATA?,
    val options: Options
)

data class Options(
    @SerializedName("date_time")
    val dateTime: String? = null
)
