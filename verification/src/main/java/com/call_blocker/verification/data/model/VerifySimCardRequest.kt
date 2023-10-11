package com.call_blocker.verification.data.model

import com.squareup.moshi.Json

data class VerifySimCardRequest(
    @Json(name = "sim_iccid")
    val simICCID: String,
    @Json(name = "msisdn")
    val simNumber: String,
    @Json(name = "sim_slot")
    val simSlot: String,
    @Json(name = "country_code")
    val countryCode: String
)
