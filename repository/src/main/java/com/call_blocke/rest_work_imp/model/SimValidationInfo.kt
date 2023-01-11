package com.call_blocke.rest_work_imp.model

data class SimValidationInfo(
    val status: SimValidationStatus,
    val number: String = ""
)


enum class SimValidationStatus{
    VALID,
    INVALID,
    PROCESSING,
    AUTO_VALIDATION,
    UNKNOWN
}