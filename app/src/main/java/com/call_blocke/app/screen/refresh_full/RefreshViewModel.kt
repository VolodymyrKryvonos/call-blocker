package com.call_blocke.app.screen.refresh_full

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefreshViewModel : ViewModel() {

    private val settingsRepository = RepositoryImp.settingsRepository

    private val taskRepository = RepositoryImp.taskRepository

    val onLoading = MutableLiveData(false)

    fun simsInfo() = liveData(Dispatchers.IO) {
        emit(
            settingsRepository.simInfo()
        )
    }

    fun firstSim(context: Context) = SimUtil.firstSim(context)

    fun secondSim(context: Context) = SimUtil.secondSim(context)

    fun reset(simSlotID: Int) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)
        try {
            settingsRepository.refreshDataForSim(simSlot = simSlotID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (simSlotID == 0) {
            SmsBlockerDatabase.smsTodaySentFirstSim = 0
        } else {
            SmsBlockerDatabase.smsTodaySentSecondSim = 0
        }
        SmartLog.e("Reset sim slot = $simSlotID")
        SmsBlockerDatabase.isSimChanged = false
        taskRepository.clearFor(simIndex = simSlotID)
        onLoading.postValue(false)
    }
}