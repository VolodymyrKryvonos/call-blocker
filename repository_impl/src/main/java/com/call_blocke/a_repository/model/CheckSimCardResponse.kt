package com.call_blocke.a_repository.model

import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.google.gson.annotations.SerializedName

data class CheckSimCardResponse(
    val status: Boolean,
    @SerializedName("msisdn")
    val number: String? = null,
    @SerializedName("auto_verification")
    val autoVerification: Boolean? = false
) {
    fun toSimValidationInfo() = SimValidationInfo(
        status = if (status) {
            SimValidationStatus.VALID
        } else {
            if (autoVerification == true) {
                SimValidationStatus.AUTO_VALIDATION
            } else
                SimValidationStatus.INVALID
        },
        number = number ?: ""
    )
}
