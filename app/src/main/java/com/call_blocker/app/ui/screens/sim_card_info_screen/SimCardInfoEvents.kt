package com.call_blocker.app.ui.screens.sim_card_info_screen

import android.content.Context
import com.call_blocker.app.ui.UiEvent

sealed interface SimCardInfoEvents : UiEvent {
    data class VerifySimCardEvent(val simSlot: Int, val context: Context) : SimCardInfoEvents
    data class ResetSimCardEvent(val simSlotID: Int, val context: Context) : SimCardInfoEvents
    data class SetNewLimitsEvent(
        val context: Context,
        val index: Int,
        val dayLimit: Int,
        val monthLimit: Int
    ) : SimCardInfoEvents

    data class CheckSimCardsEvent(val context: Context) : SimCardInfoEvents
    data class SetCurrentPageEvent(val page: Int) : SimCardInfoEvents
    data class ReloadSimInfoEvent(val context: Context) : SimCardInfoEvents
}