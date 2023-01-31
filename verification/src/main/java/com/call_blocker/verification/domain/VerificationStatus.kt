package com.call_blocker.verification.domain

enum class VerificationStatus {
    Unknown,
    Verified,
    Unverified,
    Processing,
    Failed;

    companion object {
        fun getUpdatedStatus(
            isVerified: Boolean,
            previousStatus: VerificationStatus
        ): VerificationStatus {
            return if (isVerified) Verified else if (previousStatus == Processing) Processing else Unverified
        }
    }
}