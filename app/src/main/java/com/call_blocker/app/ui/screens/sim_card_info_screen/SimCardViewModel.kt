package com.call_blocker.app.ui.screens.sim_card_info_screen

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.ui.BaseViewModel
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.SimUtil
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.call_blocker.verification.domain.SimCardVerifier
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SimCardViewModel(
    private val simCardVerifier: SimCardVerifier,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    verificationRepository: VerificationRepository
) : BaseViewModel<SimCardInfoScreenState, SimCardInfoEvents>(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl(verificationRepository) {
    override fun setInitialState() = SimCardInfoScreenState()

    init {
        coroutineScope = viewModelScope
        viewModelScope.launch {
            launch {
                VerificationInfoStateHolder.getStateHolderBySimSlotIndex(0).collectLatest {
                    setState { state.value.copy(firstSimVerificationState = it) }
                }
            }
            launch {
                VerificationInfoStateHolder.getStateHolderBySimSlotIndex(1).collectLatest {
                    setState { state.value.copy(secondSimVerificationState = it) }
                }
            }
        }
    }

    override fun handleEvent(event: SimCardInfoEvents) {
        Log.e("SimCardViewModel", "$event")
        when (event) {
            is SimCardInfoEvents.CheckSimCardsEvent -> checkSimCards(event.context)
            is SimCardInfoEvents.ResetSimCardEvent -> resetSim(event)
            is SimCardInfoEvents.SetNewLimitsEvent -> setNewLimitForSim(event)
            is SimCardInfoEvents.VerifySimCardEvent -> verifySimCard(event)
            is SimCardInfoEvents.SetCurrentPageEvent -> setState { state.value.copy(currentPage = event.page) }
            is SimCardInfoEvents.ReloadSimInfoEvent -> simsInfo(event.context)
        }
    }

    fun simsInfo(context: Context) {
        viewModelScope.launch {
            setState {
                state.value.copy(
                    firstSimSubInfo = SimUtil.firstSim(context),
                    secondSimSubInfo = SimUtil.secondSim(context)
                )
            }
            val response = settingsRepository.simInfo(
                context
            )
            var deliveredFirstSim = 0
            var deliveredSecondSim = 0
            var limitFirstSim = 0
            var limitSecondSim = 0

            var firstSimConnectedOn = ""
            var secondSimConnectedOn = ""
            response.forEach {
                if (it.simSlot == 0) {
                    deliveredFirstSim = it.simDelivered
                    limitFirstSim = it.simPerDay
                    firstSimConnectedOn = it.simDate
                } else {
                    deliveredSecondSim = it.simDelivered
                    limitSecondSim = it.simPerDay
                    secondSimConnectedOn = it.simDate
                }
            }

            setState {
                state.value.copy(
                    deliveredFirstSim = deliveredFirstSim,
                    deliveredSecondSim = deliveredSecondSim,
                    firstSimDayLimit = limitFirstSim,
                    secondSimDayLimit = limitSecondSim,
                    firstSimConnectedOn = firstSimConnectedOn,
                    secondSimConnectedOn = secondSimConnectedOn
                )
            }
        }
    }

    fun verifySimCard(event: SimCardInfoEvents.VerifySimCardEvent) {
        if (!SendingSMSWorker.isRunning.value) {
            SendingSMSWorker.start(event.context)
        }
        viewModelScope.launch {
            while (!taskRepository.connectionStatusFlow.value) {
                delay(1000)
            }
            simCardVerifier.verifySimCard(event.context, event.simSlot)
        }
    }

    fun resetSim(event: SimCardInfoEvents.ResetSimCardEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settingsRepository.refreshDataForSim(
                    simSlot = event.simSlotID,
                    context = event.context
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setState {
                if (event.simSlotID == 0) {
                    state.value.copy(deliveredFirstSim = 0)
                } else {
                    state.value.copy(deliveredSecondSim = 0)
                }
            }
            taskRepository.clearFor(simIndex = event.simSlotID)
        }

    fun setNewLimitForSim(event: SimCardInfoEvents.SetNewLimitsEvent) {
        val firstSimDayLimit: Int
        val firstSimMonthLimit: Int
        val secondSimDayLimit: Int
        val secondSimMonthLimit: Int
        if (event.index == 0) {
            firstSimDayLimit = event.dayLimit
            firstSimMonthLimit = event.monthLimit
            secondSimDayLimit = state.value.secondSimDayLimit
            secondSimMonthLimit = state.value.secondSimMonthLimit
            setState {
                state.value.copy(
                    firstSimDayLimit = firstSimDayLimit,
                    firstSimMonthLimit = firstSimMonthLimit
                )
            }
        } else {
            firstSimDayLimit = state.value.firstSimDayLimit
            firstSimMonthLimit = state.value.firstSimMonthLimit
            secondSimDayLimit = event.dayLimit
            secondSimMonthLimit = event.monthLimit
            setState {
                state.value.copy(
                    secondSimDayLimit = secondSimDayLimit,
                    secondSimMonthLimit = secondSimMonthLimit
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.setSmsPerDay(
                context = event.context,
                smsPerDaySimFirst = firstSimDayLimit,
                smsPerMonthSimFirst = firstSimMonthLimit,
                smsPerDaySimSecond = secondSimDayLimit,
                smsPerMonthSimSecond = secondSimMonthLimit
            )
        }
    }

}