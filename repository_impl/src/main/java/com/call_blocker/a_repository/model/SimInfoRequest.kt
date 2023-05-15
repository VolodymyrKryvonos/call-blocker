package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class SimpleBody(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID
)

data class SimInfoRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "first_sim_iccid")
    val firstSimId: String?,
    @Json(name = "second_sim_iccid")
    val secondSimId: String?,
    @Json(name = "country_code")
    val countryCode: String
)