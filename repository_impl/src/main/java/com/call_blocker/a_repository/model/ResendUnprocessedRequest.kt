package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class ResendUnprocessedRequest(
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "sms_ids")
    val ids: List<Int>
)
