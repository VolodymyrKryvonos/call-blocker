package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

class SimpleBody()

data class SimInfoRequest(
    @Json(name = "first_sim_iccid")
    val firstSimId: String?,
    @Json(name = "second_sim_iccid")
    val secondSimId: String?,
    @Json(name = "country_code")
    val countryCode: String
)