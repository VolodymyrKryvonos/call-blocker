package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class UssdResult(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "sim_iccid")
    val simId: String,
    @Json(name = "result")
    val result: String,
    @Json(name = "ussd_command")
    val ussdCommand: String
)