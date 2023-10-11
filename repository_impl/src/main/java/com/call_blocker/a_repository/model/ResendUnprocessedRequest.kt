package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class ResendUnprocessedRequest(
    @Json(name = "sms_ids")
    val ids: List<Int>
)
