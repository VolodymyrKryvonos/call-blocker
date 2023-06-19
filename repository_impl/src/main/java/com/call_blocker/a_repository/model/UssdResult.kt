package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class UssdResult(
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "sim_iccid")
    val simId: String,
    @Json(name = "result")
    val result: String,
    @Json(name = "ussd_command")
    val ussdCommand: String
)