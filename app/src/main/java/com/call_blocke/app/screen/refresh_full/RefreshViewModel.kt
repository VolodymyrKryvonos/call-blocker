package com.call_blocke.app.screen.refresh_full

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.example.common.Resource
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SnackbarVisibility {
    Visible, Gone
}

class RefreshViewModel : ViewModel(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl() {

    private val taskRepository = RepositoryImp.taskRepository
    private val settingsRepository = RepositoryImp.settingsRepository

    private val _snackbarVisibility = MutableStateFlow(SnackbarVisibility.Gone)
    val snackbarVisibility = _snackbarVisibility.asStateFlow()

    val onLoading = MutableLiveData(false)
    val verificationState = MutableSharedFlow<Resource<Unit>>()

    private val _simInfoState: MutableStateFlow<List<FullSimInfoModel>> =
        MutableStateFlow(emptyList())
    val simInfoState = _simInfoState.asStateFlow()

    init {
        coroutineScope = viewModelScope
    }

    fun simsInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _simInfoState.emit(
                settingsRepository.simInfo()
            )
        }
    }

    fun firstSim(context: Context) = SimUtil.firstSim(context)
    fun secondSim(context: Context) = SimUtil.secondSim(context)

    fun reset(simSlotID: Int, context: Context?) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)
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
        if (simSlotID == 0) {
            SmsBlockerDatabase.smsTodaySentFirstSim = 0
        } else {
            SmsBlockerDatabase.smsTodaySentSecondSim = 0
        }
        SmartLog.e("Reset sim slot = $simSlotID")
        taskRepository.clearFor(simIndex = simSlotID)
        onLoading.postValue(false)
        simsInfo()
    }

    fun hideSnackbar() {
        viewModelScope.launch {
            _snackbarVisibility.emit(SnackbarVisibility.Gone)
        }
    }

    fun showSnackbar() {
        viewModelScope.launch {
            _snackbarVisibility.emit(SnackbarVisibility.Visible)
        }
    }
}