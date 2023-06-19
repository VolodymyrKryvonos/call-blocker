package com.call_blocker.app.new_ui.screens.home_screen

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.new_ui.BaseViewModel
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.rest_work_imp.UserRepository
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.call_blocker.verification.domain.SimCardVerifier
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val simCardVerifier: SimCardVerifier,
    private val smsBlockerDatabase: SmsBlockerDatabase,
    verificationRepository: VerificationRepository
) : BaseViewModel<HomeScreenState, HomeScreenEvents>(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl(verificationRepository) {
    override fun setInitialState() = HomeScreenState(uniqueId = smsBlockerDatabase.deviceID)

    override fun handleEvent(event: HomeScreenEvents) {
        when (event) {
            HomeScreenEvents.CheckIsLatestVersionEvent -> checkIsLatestVersion()
            HomeScreenEvents.CloseUpdateDialogEvent -> closeUpdateDialog()
            is HomeScreenEvents.LogOutEvent -> logOut(event)
            is HomeScreenEvents.ReloadSystemInfoEvent -> reloadSystemInfo(event)
            is HomeScreenEvents.ResetSimEvent -> resetSim(event)
            is HomeScreenEvents.RunExecutorEvent -> runExecutor(event)
            is HomeScreenEvents.StopExecutorEvent -> stopExecutor(event)
            is HomeScreenEvents.VerifySimCardEvent -> verifySimCard(event)
        }
    }

    init {
        coroutineScope = viewModelScope
        viewModelScope.launch {
            setState {
                state.value.copy(
                    uniqueId = smsBlockerDatabase.deviceID,
                    isRunning = SendingSMSWorker.isRunning.value,
                    isConnected = taskRepository.connectionStatusFlow.value
                )
            }
            launch {
                taskRepository.connectionStatusFlow.collectLatest {
                    setState {
                        state.value.copy(isConnected = it)
                    }
                }
            }
            launch {
                SendingSMSWorker.isRunning.collectLatest {

                    setState { state.value.copy(isRunning = it) }
                }
            }
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
        checkIsLatestVersion()
    }

    fun closeUpdateDialog() {

        setState { state.value.copy(showUpdateAppDialog = false) }
    }

    fun checkIsLatestVersion() {
        setState { state.value.copy(showUpdateAppDialog = !isVersionUpToDate()) }
    }

    private fun isVersionUpToDate(): Boolean {
        val profile = smsBlockerDatabase.profile

        val isMajorHigherThenLatest = (profile?.latestMajorVersion ?: 0) < BuildConfig.major
        if (isMajorHigherThenLatest)
            return true

        val isMinorHigherThenLatest = (profile?.latestMinorVersion ?: 0) < BuildConfig.minor
        val isMajorUpToDate = profile?.latestMajorVersion == BuildConfig.major
        if (isMajorUpToDate && isMinorHigherThenLatest)
            return true

        val isMinorUpToDate = (profile?.latestMinorVersion ?: 0) <= BuildConfig.minor
        val isPatchUpToDate = (profile?.latestPatchVersion ?: 0) <= BuildConfig.patch

        return isMajorUpToDate && isMinorUpToDate && isPatchUpToDate
    }

    fun runExecutor(event: HomeScreenEvents.RunExecutorEvent) {
        if (state.value.isRunning)
            return
        viewModelScope.launch {
            settingsRepository.changeSimCard(
                event.context
            )
        }
        SendingSMSWorker.start(context = event.context)
    }

    fun stopExecutor(event: HomeScreenEvents.StopExecutorEvent) {
        SendingSMSWorker.stop(context = event.context)
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

    fun reloadSystemInfo(event: HomeScreenEvents.ReloadSystemInfoEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                simsInfo(event.context)
            }
            launch {
                getSystemDetails(event.context)
            }
            launch {
                checkSimCards(event.context)
            }
        }
    }

    private suspend fun getSystemDetails(context: Context) {

        setState { state.value.copy(isLoading = true) }
        val systemDetail = userRepository.systemDetail(
            context
        )

        setState {
            state.value.copy(
                firstName = systemDetail.firstName,
                lastName = systemDetail.lastName,
                isLoading = false,
                amount = systemDetail.amount,
                delivered = systemDetail.deliveredCount,
                undelivered = systemDetail.undeliveredCount,
                leftToSend = systemDetail.leftCount
            )
        }
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


        setState {
            state.value.copy(
                deliveredFirstSim = deliveredFirstSim,
                deliveredSecondSim = deliveredSecondSim,
                firstSimDayLimit = limitFirstSim,
                secondSimDayLimit = limitSecondSim,
                isFirstSimAvailable = SimUtil.firstSim(context) != null,
                isSecondSimAvailable = SimUtil.secondSim(context) != null
            )
        }
    }

    fun verifySimCard(event: HomeScreenEvents.VerifySimCardEvent) {
        runExecutor(HomeScreenEvents.RunExecutorEvent(event.context))
        viewModelScope.launch {
            while (!state.value.isConnected) {
                delay(1000)
            }
            simCardVerifier.verifySimCard(event.context, event.simSlot)
        }
    }

    fun logOut(event: HomeScreenEvents.LogOutEvent) {
        stopExecutor(HomeScreenEvents.StopExecutorEvent(event.context))
        userRepository.logOut()
    }

    fun resetSim(event: HomeScreenEvents.ResetSimEvent) = viewModelScope.launch(Dispatchers.IO) {

        setState { state.value.copy(isLoading = true) }
        try {
            settingsRepository.refreshDataForSim(
                simSlot = event.simSlot, context = event.context
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        setState {
            if (event.simSlot == 0) {
                state.value.copy(deliveredFirstSim = 0)
            } else {
                state.value.copy(deliveredSecondSim = 0)
            }
        }
        taskRepository.clearFor(simIndex = event.simSlot)
        setState { state.value.copy(isLoading = false) }
    }
}