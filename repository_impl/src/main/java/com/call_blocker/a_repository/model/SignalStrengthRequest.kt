package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class SignalStrengthRequest(
    @Json(name = "signal_strength")
    val signalStrength: Int,
    @Json(name = "signal_generation")
    val signalGeneration: String,
    @Json(name = "first_sim_iccid")
    val firstSimId: String?,
    @Json(name = "second_sim_iccid")
    val secondSimId: String?,
    @Json(name = "first_sim_operator")
    val firstSimOperator: String?,
    @Json(name = "second_sim_operator")
    val secondSimOperator: String?,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "ip_address")
    val ipAddress: String
)