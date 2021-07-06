package com.call_blocke.app.screen.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val settingsRepository = RepositoryImp.settingsRepository

    val fistSimSlotSmsCount: Int
        get() = settingsRepository.currentSmsContFirstSimSlot

    val secondSimSlotSmsCount: Int
        get() = settingsRepository.currentSmsContSecondSimSlot

    val onLoading = MutableLiveData(false)

    val onSuccessUpdated = MutableLiveData(false)

    fun isFirstSimAllow(context: Context): Boolean = SimUtil.getSIMInfo(context).isNotEmpty()

    fun isSecondSimAllow(context: Context): Boolean = SimUtil.getSIMInfo(context).size > 1

    fun updateSmsPerDay(forSimFirst: Int, forSimSecond: Int) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)

        delay(2000L)

        settingsRepository.setSmsPerDay(
            forFirstSim = forSimFirst,
            forSecondSim = forSimSecond
        )

        onLoading.postValue(false)

        onSuccessUpdated.postValue(true)
    }

}