package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class SignalStrengthRequest(
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @SerializedName("signal_strength")
    val signalStrength: Int,
    @SerializedName("signal_generation")
    val signalGeneration: String
)