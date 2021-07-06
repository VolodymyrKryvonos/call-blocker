package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class TasksRequest(
    val campaign: String,
    @SerializedName("connection_type")
    val connectionType: String,
    val carrier: String
)
