package com.call_blocke.a_repository.repository

import android.content.Context
import com.call_blocke.a_repository.model.RefreshDataForSimRequest
import com.call_blocke.a_repository.model.SmsPerDayRequest
import com.call_blocke.a_repository.model.TasksRequest
import com.call_blocke.a_repository.rest.SettingsRest
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil

class SettingsRepositoryImp : SettingsRepository() {

    private val settingsRest: SettingsRest
        get() = ApiRepositoryHelper.createRest(
            SettingsRest::class.java
        )

    override suspend fun updateSmsPerDay(context: Context) {
        val simInfo =
            SimUtil.getSIMInfo(context)
        val firstSimName =
            (simInfo.firstOrNull { it.simSlotIndex == 0 }?.carrierName ?: "none").toString()
        val secondSimName =
            (simInfo.firstOrNull { it.simSlotIndex == 1 }?.carrierName ?: "none").toString()
        val countryCode = simInfo.firstOrNull()?.countryIso ?: "default"
        settingsRest.setSmsPerDay(
            SmsPerDayRequest(
                forSimFirst = currentSmsContFirstSimSlot,
                forSimSecond = currentSmsContSecondSimSlot,
                firstSimName = firstSimName,
                secondSimName = secondSimName,
                countryCode = countryCode
            )
        )
    }

    override suspend fun blackPhoneNumberList(): List<String> {
        return settingsRest.blackList(
            TasksRequest()
        ).data.map {
            it.number
        }
    }

    override suspend fun refreshDataForSim(simSlot: Int) {
        settingsRest.resetSim(
            RefreshDataForSimRequest(
                simName = if (simSlot == 0)
                    "msisdn_1"
                else
                    "msisdn_2"
            )
        )
    }

    override suspend fun simInfo(): List<FullSimInfoModel> {
        try {
            val sims = arrayListOf<FullSimInfoModel>()
            val data = settingsRest.simInfo()

            data.data.simFirst?.let {
                sims.add(
                    FullSimInfoModel(
                        simDate = it.updatedAt,
                        simDelivered = it.delivered,
                        simPerDay = it.smsPerDay,
                    )
                )
            }

            data.data.simSecond?.let {
                sims.add(
                    FullSimInfoModel(
                        simDate = it.updatedAt,
                        simDelivered = it.delivered,
                        simPerDay = it.smsPerDay,
                    )
                )
            }

            return sims
        } catch (e: Exception) {
        }
        return emptyList()
    }
}