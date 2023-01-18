package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class ValidateSimCardRequest(
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,

    @SerializedName("sim_iccid")
    val simICCID: String,

    @SerializedName("msisdn")
    val simNumber: String,

    @SerializedName("sim_slot")
    val simSlot: String,

    @SerializedName("country_code")
    val countryCode: String
)
