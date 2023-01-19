package com.call_blocker.verification.data.model

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