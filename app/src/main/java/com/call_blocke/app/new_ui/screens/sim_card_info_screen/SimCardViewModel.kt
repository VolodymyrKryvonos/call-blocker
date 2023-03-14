package com.call_blocke.app.new_ui.screens.sim_card_info_screen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.app.worker_manager.SendingSMSWorker
import com.call_blocke.repository.RepositoryImp
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.call_blocker.verification.domain.SimCardVerifier
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SimCardViewModel : ViewModel(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl() {
    var state: SimCardInfoScreenState by mutableStateOf(SimCardInfoScreenState())
    private val simCardVerifier = SimCardVerifier()
    private val taskRepository = RepositoryImp.taskRepository
    private val settingsRepository = RepositoryImp.settingsRepository

    init {
        coroutineScope = viewModelScope
        viewModelScope.launch {
            launch {
                VerificationInfoStateHolder.getStateHolderBySimSlotIndex(0).collectLatest {
                    state = state.copy(firstSimVerificationState = it)
                }
            }
            launch {
                VerificationInfoStateHolder.getStateHolderBySimSlotIndex(1).collectLatest {
                    state = state.copy(secondSimVerificationState = it)
                }
            }
        }
    }

    fun simsInfo(context: Context) {
        viewModelScope.launch {
            SmartLog.e(
                "simsInfo firstSimSubInfo = ${SimUtil.firstSim(context)} secondSimSubInfo = ${
                    SimUtil.secondSim(
                        context
                    )
                }"
            )
            state = state.copy(
                firstSimSubInfo = SimUtil.firstSim(context),
                secondSimSubInfo = SimUtil.secondSim(context)
            )
            val response = settingsRepository.simInfo(
                state.firstSimSubInfo?.iccId,
                state.secondSimSubInfo?.iccId,
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

            state = state.copy(
                deliveredFirstSim = deliveredFirstSim,
                deliveredSecondSim = deliveredSecondSim,
                firstSimDayLimit = limitFirstSim,
                secondSimDayLimit = limitSecondSim,
                firstSimConnectedOn = firstSimConnectedOn,
                secondSimConnectedOn = secondSimConnectedOn
            )
        }
    }

    fun verifySimCard(simId: String, simSlot: Int, context: Context, phoneNumber: String = "") {
        if (!SendingSMSWorker.isRunning.value) {
            SendingSMSWorker.start(context)
        }
        viewModelScope.launch {
            while (!taskRepository.connectionStatusFlow.value) {
                delay(1000)
            }
            simCardVerifier.verifySimCard(phoneNumber, simId, simSlot)
        }
    }

    fun resetSim(simSlotID: Int, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val simInfo = SimUtil.simInfo(context, simSlotID)
            settingsRepository.refreshDataForSim(
                simSlot = simSlotID,
                simInfo?.iccId ?: "",
                simInfo?.number ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        state = if (simSlotID == 0) {
            state.copy(deliveredFirstSim = 0)
        } else {
            state.copy(deliveredSecondSim = 0)
        }
        taskRepository.clearFor(simIndex = simSlotID)
    }

    fun setNewLimitForSim(context: Context, index: Int, dayLimit: Int, monthLimit: Int) {
        val firstSimDayLimit: Int
        val firstSimMonthLimit: Int
        val secondSimDayLimit: Int
        val secondSimMonthLimit: Int
        if (index == 0) {
            firstSimDayLimit = dayLimit
            firstSimMonthLimit = monthLimit
            secondSimDayLimit = state.secondSimDayLimit
            secondSimMonthLimit = state.secondSimMonthLimit
            state = state.copy(
                firstSimDayLimit = firstSimDayLimit,
                firstSimMonthLimit = firstSimMonthLimit
            )
        } else {
            firstSimDayLimit = state.firstSimDayLimit
            firstSimMonthLimit = state.firstSimMonthLimit
            secondSimDayLimit = dayLimit
            secondSimMonthLimit = monthLimit
            state = state.copy(
                secondSimDayLimit = secondSimDayLimit,
                secondSimMonthLimit = secondSimMonthLimit
            )
        }
        viewModelScope.launch {
            settingsRepository.setSmsPerDay(
                context = context,
                smsPerDaySimFirst = firstSimDayLimit,
                smsPerMonthSimFirst = firstSimMonthLimit,
                smsPerDaySimSecond = secondSimDayLimit,
                smsPerMonthSimSecond = secondSimMonthLimit
            )
        }
    }

}