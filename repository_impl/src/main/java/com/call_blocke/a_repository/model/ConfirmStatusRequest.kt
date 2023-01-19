package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ConfirmStatusRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    val data: List<TaskStatusRequest>
)
