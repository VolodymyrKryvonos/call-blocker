package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class SmsPerDayRequest(
    @SerializedName("sms_per_day_1")
    val forSimFirst: Int,

    @SerializedName("sms_per_day_2")
    val forSimSecond: Int,

    @SerializedName("msisdn_1")
    val firstSimName: String,

    @SerializedName("msisdn_2")
    val secondSimName: String,

    @SerializedName("country_code")
    val countryCode: String
)
