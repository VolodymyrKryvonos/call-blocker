package com.call_blocke.a_repository.model

import com.call_blocke.a_repository.unit.NetworkInfo
import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class TasksRequest(
    val campaign: String = "App sms",
    @SerializedName("connection_type")
    val connectionType: String = NetworkInfo.connectionType(),
    val carrier: String = "",
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID
)
