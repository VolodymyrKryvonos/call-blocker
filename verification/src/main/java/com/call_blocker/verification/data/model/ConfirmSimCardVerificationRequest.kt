package com.call_blocker.verification.data.model

import com.squareup.moshi.Json

data class ConfirmSimCardVerificationRequest(
    @Json(name = "sim_iccid")
    val iccId: String,
    @Json(name = "msisdn")
    val phoneNumber: String,
    @Json(name = "verification_code")
    val verificationCode: String
)
