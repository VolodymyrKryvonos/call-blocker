package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ConfirmStatusRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    val data: List<TaskStatusRequest>
)
