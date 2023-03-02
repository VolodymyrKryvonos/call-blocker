package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ChangeSimCardRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "first_sim_iccid")
    val firstSimId: String?,
    @Json(name = "second_sim_iccid")
    val secondSimId: String?,
    @Json(name = "operator_1")
    val firstSimOperator: String?,
    @Json(name = "operator_2")
    val secondSimOperator: String?,
    @Json(name = "country_code")
    val countryCode: String,
)
