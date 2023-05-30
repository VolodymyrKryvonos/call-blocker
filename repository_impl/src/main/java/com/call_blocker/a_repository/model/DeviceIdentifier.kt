package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class DeviceIdentifier(
    @Json(name = "device_id")
    val deviceId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "IMEI")
    val imei: String? = null
)