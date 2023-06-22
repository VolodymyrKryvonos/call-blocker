package com.call_blocker.app.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<T : UiState, in E : UiEvent> : ViewModel() {
    private val _state: MutableStateFlow<T> by lazy { MutableStateFlow(setInitialState()) }
    val state: StateFlow<T> by lazy { _state.asStateFlow() }

    abstract fun setInitialState(): T

    protected fun setState(reducer: T.() -> T) {
        _state.update { _state.value.reducer() }
    }

    abstract fun handleEvent(event: E)
}

interface UiState
interface UiEvent