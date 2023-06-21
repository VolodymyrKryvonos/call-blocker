package com.call_blocker.app.new_ui.screens.settings_screen

import com.call_blocker.app.new_ui.BaseViewModel
import com.call_blocker.app.new_ui.UiEvent
import com.call_blocker.app.new_ui.UiState

class SettingsViewModel :
    BaseViewModel<SettingsScreenState, SettingsEvent>() {
    override fun setInitialState() = SettingsScreenState()

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.TurnOffUssd -> Unit
            SettingsEvent.TurnOnUssd -> Unit
            is SettingsEvent.UpdateIsUssdEnableEvent -> setState { state.value.copy(isUssdOn = event.isUssdOn) }
        }
    }
}

data class SettingsScreenState(val isUssdOn: Boolean = false) : UiState

sealed interface SettingsEvent : UiEvent {
    object TurnOnUssd : SettingsEvent
    object TurnOffUssd : SettingsEvent

    data class UpdateIsUssdEnableEvent(val isUssdOn: Boolean) : SettingsEvent
}