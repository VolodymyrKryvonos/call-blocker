package com.call_blocke.a_repository.model

import com.call_blocke.rest_work_imp.model.SimVerificationInfo
import com.call_blocke.rest_work_imp.model.SimVerificationStatus
import com.google.gson.annotations.SerializedName

data class CheckSimCardResponse(
    val status: Boolean,
    @SerializedName("msisdn")
    val number: String? = null,
    @SerializedName("auto_verification")
    val autoVerification: Boolean? = false
) {
    fun toSimVerificationInfo() = SimVerificationInfo(
        status = if (status) {
            SimVerificationStatus.VALID
        } else {
            SimVerificationStatus.INVALID
        },
        isAutoVerificationAvailable = autoVerification == true,
        number = number ?: ""
    )
}
