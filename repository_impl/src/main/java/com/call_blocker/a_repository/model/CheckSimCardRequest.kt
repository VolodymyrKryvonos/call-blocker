package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class CheckSimCardRequest(
    @Json(name = "sim_iccid")
    val iccId: String,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "sim_slot")
    val simSlot: String,
    @Json(name = "create_auto_verification_sms")
    val createAutoVerificationSms: Boolean = false,
    @Json(name = "msisdn")
    val phoneNumber: String? = null,
)