package com.call_blocker.verification.data.model

import com.squareup.moshi.Json

data class CheckSimCardResponse(
    val status: Boolean,
    @Json(name = "msisdn")
    val number: String? = null,
    @Json(name = "auto_verification")
    val autoVerification: Boolean? = false
)