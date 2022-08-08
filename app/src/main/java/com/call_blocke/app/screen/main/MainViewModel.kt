package com.call_blocke.app.screen.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.app.worker_manager.ServiceWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.repository.RepositoryImp.settingsRepository
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SimUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val taskExecutorIsRunning: StateFlow<Boolean> = ServiceWorker.isRunning

    val systemInfoLiveData = MutableLiveData(SmsBlockerDatabase.systemDetail)

    val isLoading = MutableLiveData(false)

    private val userRepository by lazy {
        RepositoryImp.userRepository
    }

    private val taskRepository by lazy {
        RepositoryImp.taskRepository
    }

    private val _simInfoState: MutableStateFlow<List<FullSimInfoModel>> =
        MutableStateFlow(emptyList())
    val simInfoState = _simInfoState.asStateFlow()

    fun simsInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _simInfoState.emit(
                settingsRepository.simInfo()
            )
        }
    }


    val isPingOn = taskRepository.ping

    val isServerOnline = taskRepository.serverConnectStatus()

    fun userName() = userRepository.userName()

    fun userPassword() = userRepository.userPassword()

    val deviceID = userRepository.deviceID

    fun runExecutor(context: Context) {
        ServiceWorker.start(context = context)

        viewModelScope.launch {
            val forSimFirst =
                if (SimUtil.isFirstSimAllow(context)) SmsBlockerDatabase.smsPerDaySimFirst else 0
            val forSimSecond =
                if (SimUtil.isSecondSimAllow(context)) SmsBlockerDatabase.smsPerDaySimSecond else 0
            settingsRepository.setSmsPerDay(
                forFirstSim = forSimFirst,
                forSecondSim = forSimSecond
            )
            if (forSimFirst == 0) {
                val simInfo = SimUtil.simInfo(context, 0)
                settingsRepository.refreshDataForSim(
                    simSlot = 0,
                    simInfo?.iccId ?: "",
                    simInfo?.number ?: ""
                )
            }
            if (forSimSecond == 0) {
                val simInfo = SimUtil.simInfo(context, 1)
                settingsRepository.refreshDataForSim(
                    simSlot = 1,
                    simInfo?.iccId ?: "",
                    simInfo?.number ?: ""
                )
            }
        }
    }

    fun stopExecutor(context: Context) {
        ServiceWorker.stop(context = context)
    }

    fun reloadSystemInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)

            val systemDetail = userRepository.systemDetail()

            isLoading.postValue(false)

            systemInfoLiveData.postValue(systemDetail)
        }
    }

    fun logOut(context: Context) {
        stopExecutor(context = context)
        userRepository.logOut()
    }

}