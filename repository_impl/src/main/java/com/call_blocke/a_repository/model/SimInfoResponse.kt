package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class SimInfoResponse(
    @SerializedName("msisdn_1")
    val simFirst: SSimInfo?,

    @SerializedName("msisdn_2")
    val simSecond: SSimInfo?
)

data class SSimInfo(
    @SerializedName("updated_at")
    val updatedAt: String,

    val delivered: Int,

    @SerializedName("sms_per_day")
    val smsPerDay: Int
)

data class DateData(
    val date: String
)