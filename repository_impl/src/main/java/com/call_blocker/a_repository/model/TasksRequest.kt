package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class TasksRequest(
    val campaign: String = "App sms",
    @Json(name = "connection_type")
    val connectionType: String,
    val carrier: String = "",
    @Json(name = "first_sim_iccid")
    val firstSimId: String?,
    @Json(name = "second_sim_iccid")
    val secondSimId: String?,
    @Json(name = "country_code")
    val countryCode: String
)
