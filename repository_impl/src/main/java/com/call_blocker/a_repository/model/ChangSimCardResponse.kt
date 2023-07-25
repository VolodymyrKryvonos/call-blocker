package com.call_blocker.a_repository.model


import com.squareup.moshi.Json

data class ChangSimCardResponse(
    @Json(name = "first_sim_iccid")
    val firstSimIccid: String?,
    @Json(name = "first_sim_limit")
    val firstSimLimit: Int,
    @Json(name = "second_sim_iccid")
    val secondSimIccid: String?,
    @Json(name = "second_sim_limit")
    val secondSimLimit: Int,
    @Json(name = "status")
    val status: Boolean
)