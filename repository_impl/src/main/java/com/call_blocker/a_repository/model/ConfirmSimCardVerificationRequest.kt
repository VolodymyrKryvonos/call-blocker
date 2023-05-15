package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ConfirmSimCardVerificationRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "sim_iccid")
    val iccId: String,
    @Json(name = "msisdn")
    val phoneNumber: String,
    @Json(name = "sim_slot")
    val simSlot: String,
    @Json(name = "verification_code")
    val verificationCode: String
)