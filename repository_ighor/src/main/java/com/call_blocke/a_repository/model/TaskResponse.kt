package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("sms")
    val smsList: List<TaskElement>
)
