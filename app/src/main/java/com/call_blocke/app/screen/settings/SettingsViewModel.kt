package com.call_blocke.app.screen.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.app.util.SimCardVerificationChecker
import com.call_blocke.app.util.SimCardVerificationCheckerImpl
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel(),
    SimCardVerificationChecker by SimCardVerificationCheckerImpl() {

    init {
        coroutineScope = viewModelScope
    }

    val firstSimSlotSmsCountPerDay: Int
        get() = SmsBlockerDatabase.smsPerDaySimFirst

    val secondSimSlotSmsCountPerDay: Int
        get() = SmsBlockerDatabase.smsPerDaySimSecond

    val firstSimSlotSmsCountPerMonth: Int
        get() = SmsBlockerDatabase.smsPerMonthSimFirst

    val secondSimSlotSmsCountPerMonth: Int
        get() = SmsBlockerDatabase.smsPerMonthSimSecond

    val onLoading = MutableLiveData(false)

    val onSuccessUpdated = MutableLiveData(false)

    fun isFirstSimAllow(context: Context): Boolean = SimUtil.isFirstSimAllow(context)

    fun isSecondSimAllow(context: Context): Boolean = SimUtil.isSecondSimAllow(context)


    fun updateSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        onLoading.postValue(true)

        SmartLog.e("Update sms limit sim1: $smsPerDaySimFirst, sim2: $smsPerDaySimSecond")

        settingsRepository.setSmsPerDay(
            context = context,
            smsPerDaySimFirst = smsPerDaySimFirst,
            smsPerDaySimSecond = smsPerDaySimSecond,
            smsPerMonthSimFirst = smsPerMonthSimFirst,
            smsPerMonthSimSecond = smsPerMonthSimSecond
        )
        onLoading.postValue(false)
        onSuccessUpdated.postValue(true)
    }

}