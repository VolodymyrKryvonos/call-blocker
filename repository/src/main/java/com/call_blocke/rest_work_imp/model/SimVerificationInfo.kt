package com.call_blocke.rest_work_imp.model

data class SimVerificationInfo(
    val status: SimVerificationStatus,
    val isAutoVerificationAvailable: Boolean = false,
    val number: String = ""
)


enum class SimVerificationStatus {
    VALID,
    INVALID,
    UNKNOWN
}