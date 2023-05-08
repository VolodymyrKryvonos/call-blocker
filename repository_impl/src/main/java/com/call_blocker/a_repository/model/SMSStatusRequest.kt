package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class TaskStatusRequest(
    val id: Int,
    val error: String,
    @Json(name = "delivery_status")
    val statusCode: Int, //1-0
    val simId: String,
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID
)
