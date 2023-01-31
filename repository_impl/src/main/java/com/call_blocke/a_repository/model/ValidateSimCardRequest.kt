package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ValidateSimCardRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,

    @Json(name = "sim_iccid")
    val simICCID: String,

    @Json(name = "msisdn")
    val simNumber: String,

    @Json(name = "sim_slot")
    val simSlot: String,

    @Json(name = "country_code")
    val countryCode: String
)
