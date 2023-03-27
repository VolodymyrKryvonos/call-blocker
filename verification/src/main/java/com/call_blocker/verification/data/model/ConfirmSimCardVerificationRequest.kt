package com.call_blocker.verification.data.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ConfirmSimCardVerificationRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "sim_iccid")
    val iccId: String,
    @Json(name = "msisdn")
    val phoneNumber: String,
    @Json(name = "verification_code")
    val verificationCode: String
)
