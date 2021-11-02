package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    val delay: Int = 1,
    @SerializedName("sms")
    val smsList: List<TaskElement>
)
