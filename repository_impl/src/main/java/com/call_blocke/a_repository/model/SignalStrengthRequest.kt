package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class SignalStrengthRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "signal_strength")
    val signalStrength: Int,
    @Json(name = "signal_generation")
    val signalGeneration: String
)