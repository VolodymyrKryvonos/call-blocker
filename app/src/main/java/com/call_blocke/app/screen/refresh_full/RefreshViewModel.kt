package com.call_blocke.app.screen.refresh_full

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
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

    fun firstSim(context: Context) = if (
        SimUtil.getSIMInfo(context).isNotEmpty()
    ) SimUtil.getSIMInfo(context)[0]
    else
        null

    fun secondSim(context: Context) = if (
        SimUtil.getSIMInfo(context).size > 1
    ) SimUtil.getSIMInfo(context)[1]
    else
        null

    fun reset(simSlotID: Int) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)

        try {
            settingsRepository.refreshDataForSim(simSlot = simSlotID)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        taskRepository.clearFor(simIndex = simSlotID)

        onLoading.postValue(false)
    }
}