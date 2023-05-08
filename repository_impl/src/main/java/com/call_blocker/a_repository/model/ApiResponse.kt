package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class ApiResponse<DATA>(
    val data: DATA
)

data class SocketMessage<DATA>(
    val method: String? = null,
    val data: DATA?,
    val options: Options
)

data class Options(
    @Json(name = "date_time")
    val dateTime: String? = null
)
