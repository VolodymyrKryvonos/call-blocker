package com.call_blocke.a_repository

import android.content.Context
import com.call_blocke.a_repository.model.SmsPerDayRequest
import com.call_blocke.a_repository.rest.SettingsRest
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil

class SettingsRepositoryImp : SettingsRepository() {

    private val settingsRest: SettingsRest
        get() = ApiRepositoryHelper.createRest(
            SettingsRest::class.java
        )

    override suspend fun updateSmsPerDay(context: Context) {
        val simInfo = SimUtil.getSIMInfo(context)

        if (simInfo.isEmpty())
            return

        settingsRest.setSmsPerDay(SmsPerDayRequest(
            forSimFirst  = currentSmsContFirstSimSlot,
            forSimSecond = currentSmsContSecondSimSlot,
            firstSimName = simInfo[0].displayName.toString(),
            secondSimName = if (simInfo.size > 1) simInfo[1].displayName.toString() else "none",
            countryCode = simInfo[0].countryIso
        ))
    }

    override suspend fun blackPhoneNumberList(): List<String> {
        return settingsRest.blackList().data.map {
            it.number
        }
    }
}