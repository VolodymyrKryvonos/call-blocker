package com.call_blocke.a_repository.request


import com.google.gson.annotations.SerializedName

data class GetProfileRequest(
    @SerializedName("app_ver")
    val appVersion: String,
    @SerializedName("protocol_ver")
    val protocolVersion: String,
    @SerializedName("unique_id")
    val uniqueId: String
)