package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class TasksRequest(
    val campaign: String = "App sms",
    @Json(name = "connection_type")
    val connectionType: String,
    val carrier: String = "",
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID
)
