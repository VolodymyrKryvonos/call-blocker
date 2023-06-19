package com.call_blocker.app.new_ui.screens.sim_card_info_screen

import android.telephony.SubscriptionInfo
import com.call_blocker.app.new_ui.UiState
import com.call_blocker.verification.domain.VerificationInfo

data class SimCardInfoScreenState(
    val currentPage: Int = 0,
    val deliveredFirstSim: Int = 0,
    val deliveredSecondSim: Int = 0,
    val firstSimDayLimit: Int = 0,
    val secondSimDayLimit: Int = 0,
    val firstSimMonthLimit: Int = 0,
    val secondSimMonthLimit: Int = 0,
    val firstSimSubInfo: SubscriptionInfo? = null,
    val secondSimSubInfo: SubscriptionInfo? = null,
    val firstSimConnectedOn: String = "",
    val secondSimConnectedOn: String = "",
    val firstSimVerificationState: VerificationInfo = VerificationInfo(),
    val secondSimVerificationState: VerificationInfo = VerificationInfo(),
) : UiState

data class SimInfoState(
    val delivered: Int = 0,
    val limit: Int = 0,
    val simSubInfo: SubscriptionInfo,
    val simVerificationState: VerificationInfo = VerificationInfo(),
    val connectedOn: String = ""
)