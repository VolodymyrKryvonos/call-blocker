package com.call_blocker.app.new_ui.screens.settings_screen

import com.call_blocker.app.new_ui.BaseViewModel
import com.call_blocker.app.new_ui.UiEvent
import com.call_blocker.app.new_ui.UiState
import com.call_blocker.db.SmsBlockerDatabase

class SettingsViewModel(private val smsBlockerDatabase: SmsBlockerDatabase) :
    BaseViewModel<SettingsScreenState, SettingsEvent>() {
    override fun setInitialState() = SettingsScreenState(smsBlockerDatabase.isUssdCommandOn)

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.TurnOffUssd -> smsBlockerDatabase.isUssdCommandOn = false
            SettingsEvent.TurnOnUssd -> smsBlockerDatabase.isUssdCommandOn = true
        }
    }
}

data class SettingsScreenState(val isUssdOn: Boolean = false) : UiState

sealed interface SettingsEvent : UiEvent {
    object TurnOnUssd : SettingsEvent
    object TurnOffUssd : SettingsEvent
}