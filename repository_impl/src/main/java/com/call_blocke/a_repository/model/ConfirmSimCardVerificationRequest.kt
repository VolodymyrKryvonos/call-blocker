package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class ConfirmSimCardVerificationRequest(
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @SerializedName("sim_iccid")
    val iccId: String,
    @SerializedName("msisdn")
    val phoneNumber: String,
    @SerializedName("sim_slot")
    val simSlot: String,
    @SerializedName("verification_code")
    val verificationCode: String
)
