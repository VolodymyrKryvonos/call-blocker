package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class SmsPerDayRequest(
    @SerializedName("sms_per_day_1")
    val smsPerDaySimFirst: Int,
    @SerializedName("sms_per_month_1")
    val smsPerMonthSimFirst: Int,
    @SerializedName("msisdn_1")
    val firstSimName: String,
    @SerializedName("first_sim_iccid")
    val firstSimICCID: String,
    @SerializedName("sms_per_day_2")
    val smsPerDaySimSecond: Int,
    @SerializedName("sms_per_month_2")
    val smsPerMonthSimSecond: Int,
    @SerializedName("msisdn_2")
    val secondSimName: String,
    @SerializedName("second_sim_iccid")
    val secondSimICCID: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    @SerializedName("connection_type")
    val connectionType: String = ""
)
