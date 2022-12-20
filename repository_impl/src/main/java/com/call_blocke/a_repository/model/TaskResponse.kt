package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    val delay: Int = 1,
    @SerializedName("sms")
    val smsList: List<TaskElement>,
    val sim: String,
    @SerializedName("sim_iccid")
    val simIccId: String
)

data class SimDetail(
    val delivered: Int,
    val sms_per_day: Int
)

data class SimInfo(
    @SerializedName("msisdn_1")
    val first: SimDetail,
    @SerializedName("msisdn_2")
    val second: SimDetail
)