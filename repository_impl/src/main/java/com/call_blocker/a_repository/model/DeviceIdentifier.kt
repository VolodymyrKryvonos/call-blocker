package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class DeviceIdentifier(
    @Json(name = "device_id")
    val deviceId: String,
    @Json(name = "IMEI")
    val imei: String? = null
)