package com.call_blocke.app.new_ui.screens.home_screen

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
import com.example.common.Resource
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel(), SimCardVerificationChecker by SimCardVerificationCheckerImpl() {
    private val settingsRepository = RepositoryImp.settingsRepository
    private val taskRepository = RepositoryImp.taskRepository
    private val userRepository = RepositoryImp.userRepository
    private val simCardVerifier = SimCardVerifier()
    var state by mutableStateOf(HomeScreenState())
        private set

    init {
        coroutineScope = viewModelScope
        viewModelScope.launch {
            state = state.copy(
                isRunning = SendingSMSWorker.isRunning.value,
                isConnected = taskRepository.connectionStatusFlow.value
            )
            launch {
                taskRepository.connectionStatusFlow.collectLatest {
                    state = state.copy(isConnected = it)
                }
            }
            launch {
                SendingSMSWorker.isRunning.collectLatest {
                    state = state.copy(isRunning = it)
                }
            }
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


    fun runExecutor(context: Context) {
        if (state.isRunning)
            return
        viewModelScope.launch {
            settingsRepository.changeSimCard(
                context
            )
        }
        SendingSMSWorker.start(context = context)
    }

    fun stopExecutor(context: Context) {
        SendingSMSWorker.stop(context = context)
        notifyServerUserStopService()
    }

    private fun notifyServerUserStopService() {
        viewModelScope.launch {
            settingsRepository.notifyServerUserStopService().collectLatest {
                if (it is Resource.Success)
                    SmartLog.e("Server notified")
            }
        }
    }

    fun reloadSystemInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                simsInfo(context)
            }
            launch {
                getSystemDetails(context)
            }
            launch {
                checkSimCards(context)
            }
        }
    }

    private suspend fun getSystemDetails(context: Context) {
        state = state.copy(isLoading = true)
        val systemDetail = userRepository.systemDetail(
            context
        )
        state = state.copy(
            firstName = systemDetail.firstName,
            lastName = systemDetail.lastName,
            isLoading = false,
            amount = systemDetail.amount,
            delivered = systemDetail.deliveredCount,
            undelivered = systemDetail.undeliveredCount,
            leftToSend = systemDetail.leftCount
        )
    }

    private suspend fun simsInfo(context: Context) {
        val response = settingsRepository.simInfo(
            context
        )
        var deliveredFirstSim = 0
        var deliveredSecondSim = 0
        var limitFirstSim = 0
        var limitSecondSim = 0
        response.forEach {
            if (it.simSlot == 0) {
                deliveredFirstSim = it.simDelivered
                limitFirstSim = it.simPerDay
            } else {
                deliveredSecondSim = it.simDelivered
                limitSecondSim = it.simPerDay
            }
        }

        state = state.copy(
            deliveredFirstSim = deliveredFirstSim,
            deliveredSecondSim = deliveredSecondSim,
            firstSimDayLimit = limitFirstSim,
            secondSimDayLimit = limitSecondSim,
            isFirstSimAvailable = SimUtil.firstSim(context) != null,
            isSecondSimAvailable = SimUtil.secondSim(context) != null
        )
    }

    fun verifySimCard(simId: String, simSlot: Int, context: Context, phoneNumber: String = "") {
        runExecutor(context)
        viewModelScope.launch {
            while (!state.isConnected) {
                delay(1000)
            }
            simCardVerifier.verifySimCard(context, simSlot)
        }
    }

    fun logOut(context: Context) {
        stopExecutor(context = context)
        userRepository.logOut()
    }

    fun resetSim(simSlotID: Int, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        state = state.copy(isLoading = true)
        try {
            val simInfo = SimUtil.simInfo(context, simSlotID)
            settingsRepository.refreshDataForSim(
                simSlot = simSlotID, context = context
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
        state = state.copy(isLoading = false)
    }
}