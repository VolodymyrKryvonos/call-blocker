package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase
import com.google.gson.annotations.SerializedName

data class RefreshDataForSimRequest(
    @SerializedName("unique_id")
    val uniqueId: String = SmsBlockerDatabase.deviceID,

    @SerializedName("sim_id")
    val simName: String,

    @SerializedName("sim_iccid")
    val simICCID: String,

    @SerializedName("msisdn")
    val simNumber: String

)