package com.call_blocke.app.new_ui.screens.home_screen

import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocker.verification.domain.VerificationInfo

data class HomeScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val uniqueId: String = SmsBlockerDatabase.deviceID,
    val isRunning: Boolean = false,
    val isConnected: Boolean = false,
    val amount: Float = 0f,
    val leftToSend: Int = 0,
    val delivered: Int = 0,
    val undelivered: Int = 0,
    val deliveredFirstSim: Int = 0,
    val deliveredSecondSim: Int = 0,
    val firstSimDayLimit: Int = 0,
    val secondSimDayLimit: Int = 0,
    val isFirstSimAvailable: Boolean = false,
    val isSecondSimAvailable: Boolean = false,
    val firstSimVerificationState: VerificationInfo = VerificationInfo(),
    val secondSimVerificationState: VerificationInfo = VerificationInfo(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun getUserName(): String {
        return "$firstName $lastName"
    }

    fun getInitials(): String {
        val result = StringBuilder()
        if (firstName.isNotEmpty())
            result.append(firstName[0])
        if (lastName.isNotEmpty())
            result.append(lastName[0])
        return result.toString()
    }
}
