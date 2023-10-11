package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class SimInfoResponse(
    @Json(name = "msisdn_1")
    val simFirst: SSimInfo?,

    @Json(name = "msisdn_2")
    val simSecond: SSimInfo?
)

data class SSimInfo(
    @Json(name = "updated_at")
    val updatedAt: String,

    val delivered: Int,

    @Json(name = "sms_per_day")
    val smsPerDay: Int
)