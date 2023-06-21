package com.call_blocker.app.new_ui.screens.home_screen

import android.content.Context
import com.call_blocker.app.new_ui.UiEvent

sealed interface HomeScreenEvents : UiEvent {
    object CloseUpdateDialogEvent : HomeScreenEvents
    object CheckIsLatestVersionEvent : HomeScreenEvents
    data class RunExecutorEvent(val context: Context) : HomeScreenEvents
    data class StopExecutorEvent(val context: Context) : HomeScreenEvents
    data class ReloadSystemInfoEvent(val context: Context) : HomeScreenEvents
    data class VerifySimCardEvent(val context: Context, val simSlot: Int) : HomeScreenEvents
    data class LogOutEvent(val context: Context) : HomeScreenEvents
    data class ResetSimEvent(val context: Context, val simSlot: Int) : HomeScreenEvents
    data class CheckSimCardsEvent(val context: Context) : HomeScreenEvents
}