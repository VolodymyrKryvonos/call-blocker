package com.call_blocker.app.screen.refresh_full

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.repository.RepositoryImp
import com.call_blocker.rest_work_imp.FullSimInfoModel
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.call_blocker.verification.domain.SimCardVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class RefreshViewModel : ViewModel(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl() {

    private val taskRepository = RepositoryImp.taskRepository
    private val settingsRepository = RepositoryImp.settingsRepository
    private val simCardVerifier = SimCardVerifier()

    val onLoading = MutableLiveData(false)

    private val _simInfoState: MutableStateFlow<List<FullSimInfoModel>> =
        MutableStateFlow(emptyList())
    val simInfoState = _simInfoState.asStateFlow()

    init {
        coroutineScope = viewModelScope
    }

    fun simsInfo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _simInfoState.emit(
                settingsRepository.simInfo(context)
            )
        }
    }

    fun reset(simSlotID: Int, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)
        try {
            settingsRepository.refreshDataForSim(
                simSlot = simSlotID, context
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
        simsInfo(
            context
        )
    }

    fun verifySimCard(context: Context, simSlot: Int) {
        viewModelScope.launch {
            simCardVerifier.verifySimCard(context, simSlot)
        }
    }
}