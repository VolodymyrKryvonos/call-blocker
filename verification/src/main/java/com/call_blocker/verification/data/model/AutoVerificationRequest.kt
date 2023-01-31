package com.call_blocker.verification.data.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class AutoVerificationRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "sim_iccid")
    val simID: String,
    @Json(name = "sim_slot")
    val simSlot: String,
    @Json(name = "country_code")
    val countryCode: String
)