package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class CheckSimCardRequest(
    @SerializedName("sim_iccid")
    val iccId: String
)