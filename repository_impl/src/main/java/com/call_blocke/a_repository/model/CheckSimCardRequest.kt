package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class CheckSimCardRequest(
    @SerializedName("sim_iccid")
    val iccId: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("sim_slot")
    val simSlot: String,
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @SerializedName("create_auto_verification_sms")
    val createAutoVerificationSms: Boolean = false,
    @SerializedName("msisdn")
    val phoneNumber: String? = null,
)