package com.call_blocke.a_repository.request

import com.squareup.moshi.Json

data class GetProfileRequest(
    @Json(name = "app_ver")
    val appVersion: String,
    @Json(name = "protocol_ver")
    val protocolVersion: String,
    @Json(name = "unique_id")
    val uniqueId: String
)