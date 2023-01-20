package com.call_blocker.verification.domain

data class VerificationInfo(
    val status: VerificationStatus = VerificationStatus.Unknown,
    val simId: String = "",
    val isAutoVerificationEnabled: Boolean = false,
    val phoneNumber: String? = null,
) {
    fun isVerificationInProgress() = status == VerificationStatus.Processing

    fun isNeedVerification() = status == VerificationStatus.Unverified
}
