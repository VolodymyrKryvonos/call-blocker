package com.call_blocke.a_repository.dto

import com.call_blocke.a_repository.model.SSimInfo
import com.call_blocke.a_repository.model.SimInfoResponse
import com.squareup.moshi.Json


data class SimInfoDto(
    @Json(name = "first_sim")
    val firstSim: SimInfo,
    @Json(name = "second_sim")
    val secondSim: SimInfo,
) {
    fun toSimInfoResponse() = SimInfoResponse(
        simFirst = firstSim.toSSimInfo(),
        simSecond = secondSim.toSSimInfo(),
    )
}

data class SimInfo(
    @Json(name = "delivered")
    val delivered: Int,
    @Json(name = "iccid")
    val simIccid: String,
    @Json(name = "sms_per_day")
    val smsPerDay: Int,
    @Json(name = "updated_at")
    val updatedAt: String
) {
    fun toSSimInfo() = SSimInfo(
        delivered = delivered,
        updatedAt = updatedAt,
        smsPerDay = smsPerDay
    )
}