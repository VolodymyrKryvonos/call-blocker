package com.call_blocke.app.screen.refresh_full

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class RefreshViewModel : ViewModel() {

    private val settingsRepository = RepositoryImp.settingsRepository

    private val taskRepository = RepositoryImp.taskRepository

    val onLoading = MutableLiveData(false)
    val validationState = MutableSharedFlow<Resource<Unit>>()

    private val _simInfoState: MutableStateFlow<List<FullSimInfoModel>> =
        MutableStateFlow(emptyList())
    val simInfoState = _simInfoState.asStateFlow()

    val firstSimValidationInfo =
        MutableStateFlow(SimValidationInfo(SimValidationStatus.UNKNOWN, ""))
    val secondSimValidationInfo =
        MutableStateFlow(SimValidationInfo(SimValidationStatus.UNKNOWN, ""))

    fun simsInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _simInfoState.emit(
                settingsRepository.simInfo()
            )
        }
    }

    fun firstSim(context: Context) = SimUtil.firstSim(context)

    fun secondSim(context: Context) = SimUtil.secondSim(context)

    fun checkSimCards(context: Context) {
        val firstSim = firstSim(context)
        val secondSim = secondSim(context)
        viewModelScope.launch(Dispatchers.IO) {
            if (firstSim != null) {
                settingsRepository.checkSimCard(firstSim.iccId ?: "", firstSimValidationInfo)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (secondSim != null) {
                settingsRepository.checkSimCard(secondSim.iccId ?: "", secondSimValidationInfo)
            }
        }
        viewModelScope.launch {
            validationState.emit(Resource.None)
        }
    }

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

    fun validatePhoneNumber(phoneNumber: String, simID: String, monthlyLimit: String) =
        viewModelScope.launch(Dispatchers.IO) {
            validationState.emitAll(
                settingsRepository.validateSimCard(
                    phoneNumber = phoneNumber,
                    simID = simID,
                    monthlyLimit = monthlyLimit.toInt()
                )
            )
        }
}