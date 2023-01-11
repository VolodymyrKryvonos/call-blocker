package com.call_blocke.app.screen.refresh_full

import android.content.Context
import android.telephony.SubscriptionInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.ValidationState
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
import kotlinx.coroutines.flow.collectLatest
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
        checkSim(firstSim, firstSimValidationInfo, SmsBlockerDatabase.firstSimValidationState)
        checkSim(secondSim, secondSimValidationInfo, SmsBlockerDatabase.secondSimValidationState)
        viewModelScope.launch {
            validationState.emit(Resource.None)
        }
    }

    private fun checkSim(
        simInfo: SubscriptionInfo?,
        simValidationInfo: MutableStateFlow<SimValidationInfo>,
        simValidationState: MutableStateFlow<ValidationState>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (simInfo != null) {
                settingsRepository.checkSimCard(simInfo.iccId ?: "", simInfo.simSlotIndex)
                    .collectLatest {
                        simValidationInfo.emit(it)
                        if (it.status == SimValidationStatus.INVALID && simValidationState.value != ValidationState.FAILED) {
                            simValidationState.emit(ValidationState.INVALID)
                            return@collectLatest
                        }
                        if (it.status == SimValidationStatus.AUTO_VALIDATION) {
                            simValidationState.emit(ValidationState.AUTO_VALIDATION)
                        }
                    }
            }
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

    fun validatePhoneNumber(
        phoneNumber: String,
        simID: String,
        monthlyLimit: String,
        simSlot: Int
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.validateSimCard(
                phoneNumber = phoneNumber,
                simID = simID,
                monthlyLimit = monthlyLimit.toIntOrNull() ?: 5000,
                simSlot = simSlot

            ).collectLatest {
                if (it is Resource.Success) {
                    if (simSlot == 0) {
                        SmsBlockerDatabase.firstSimValidationState.emit(ValidationState.PROCESSING)
                    } else {
                        SmsBlockerDatabase.secondSimValidationState.emit(ValidationState.PROCESSING)
                    }
                }
                validationState.emit(it)
            }
        }
}