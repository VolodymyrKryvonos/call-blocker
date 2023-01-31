package com.call_blocke.a_repository.model

import com.squareup.moshi.Json

data class TaskResponse(
    val delay: Int = 1,
    @Json(name = "sms")
    val smsList: List<TaskElement>,
    val sim: String,
    @Json(name = "sim_iccid")
    val simIccId: String?
)

data class SimDetail(
    val delivered: Int,
    val sms_per_day: Int
)

data class SimInfo(
    @Json(name = "msisdn_1")
    val first: SimDetail,
    @Json(name = "msisdn_2")
    val second: SimDetail
)