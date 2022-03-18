package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class ResendUnprocessedRequest(
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @SerializedName("sms_ids")
    val ids: List<Int>
)
