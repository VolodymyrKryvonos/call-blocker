package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.squareup.moshi.Json

data class SmsPerDayRequest(
    @Json(name = "sms_per_day_1")
    val smsPerDaySimFirst: Int,
    @Json(name = "sms_per_month_1")
    val smsPerMonthSimFirst: Int,
    @Json(name = "msisdn_1")
    val firstSimName: String,
    @Json(name = "first_sim_iccid")
    val firstSimICCID: String,
    @Json(name = "sms_per_day_2")
    val smsPerDaySimSecond: Int,
    @Json(name = "sms_per_month_2")
    val smsPerMonthSimSecond: Int,
    @Json(name = "msisdn_2")
    val secondSimName: String,
    @Json(name = "second_sim_iccid")
    val secondSimICCID: String,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @Json(name = "connection_type")
    val connectionType: String = ""
)
