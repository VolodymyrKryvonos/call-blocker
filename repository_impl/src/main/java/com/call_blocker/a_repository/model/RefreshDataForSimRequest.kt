package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class RefreshDataForSimRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "sim_id")
    val simName: String,
    @Json(name = "sim_iccid")
    val simICCID: String,
    @Json(name = "msisdn")
    val simNumber: String,
    @Json(name = "country_code")
    val countryCode: String
)