package com.call_blocker.app.screen.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.worker_manager.SendingSMSWorker
import com.call_blocker.common.Resource
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.repository.RepositoryImp
import com.call_blocker.rest_work_imp.FullSimInfoModel
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), SimCardVerificationChecker by SimCardVerificationCheckerImpl() {
    private val settingsRepository = RepositoryImp.settingsRepository

    private val _openValidateSimCardDialog: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val openValidateSimCardDialog = _openValidateSimCardDialog.asSharedFlow()

    private val _openOutdatedVersionDialog: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val openOutdatedVersionDialog = _openOutdatedVersionDialog.asSharedFlow()

    val taskExecutorIsRunning: StateFlow<Boolean> = SendingSMSWorker.isRunning

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

    fun simsInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _simInfoState.emit(
                settingsRepository.simInfo(context)
            )
        }
    }

    val isServerOnline = taskRepository.serverConnectStatus()

    fun userName() = userRepository.userName()


    val deviceID = userRepository.deviceID

    fun runExecutor(context: Context) {

        viewModelScope.launch {
            RepositoryImp.settingsRepository.changeSimCard(
                context
            )
        }

        SendingSMSWorker.start(context = context)
    }

    fun stopExecutor(context: Context) {
        SendingSMSWorker.stop(context = context)
    }

    fun reloadSystemInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)

            val systemDetail = userRepository.systemDetail(
                context
            )

            isLoading.postValue(false)

            systemInfoLiveData.postValue(systemDetail)
        }
    }

    fun logOut(context: Context) {
        stopExecutor(context = context)
        userRepository.logOut()
    }

    fun resetSimIfChanged(context: Context) {
        if (SmsBlockerDatabase.firstSimChanged) {
            viewModelScope.launch {
                try {
                    settingsRepository.refreshDataForSim(
                        0, context
                    )
                } catch (e: Exception) {
                    SmartLog.e("Auto reset failed sim1 ${getStackTrace(e)}")
                }
            }
        }
        if (SmsBlockerDatabase.secondSimChanged) {
            viewModelScope.launch {
                try {
                    settingsRepository.refreshDataForSim(
                        1, context
                    )
                } catch (e: Exception) {
                    SmartLog.e("Auto reset failed sim2 ${getStackTrace(e)}")
                }
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            settingsRepository.getProfile().collectLatest {
                when (it) {
                    is Resource.Error -> isLoading.postValue(false)
                    is Resource.Loading -> isLoading.postValue(true)
                    is Resource.Success -> {
                        isLoading.postValue(false)
                        SmsBlockerDatabase.profile = it.data
                        checkIsLatestVersion()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun checkIsLatestVersion() {
        viewModelScope.launch {
            _openOutdatedVersionDialog.emit(!isVersionUpToDate())
        }
    }

    private fun isVersionUpToDate(): Boolean {
        val profile = SmsBlockerDatabase.profile

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

    fun notifyServerUserStopService() {
        viewModelScope.launch {
            settingsRepository.notifyServerUserStopService().collectLatest {
                if (it is Resource.Success)
                    SmartLog.e("Server notified")
            }
        }
    }

    fun closeOutdatedVersionDialog() {
        viewModelScope.launch {
            _openOutdatedVersionDialog.emit(false)
        }
    }

    fun closeValidateSimCardDialog() {
        viewModelScope.launch {
            _openValidateSimCardDialog.emit(false)
        }
    }
}