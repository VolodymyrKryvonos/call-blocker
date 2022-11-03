package com.call_blocke.a_repository.repository

import android.content.Context
import com.call_blocke.a_repository.BuildConfig
import com.call_blocke.a_repository.Const
import com.call_blocke.a_repository.model.RefreshDataForSimRequest
import com.call_blocke.a_repository.model.SmsPerDayRequest
import com.call_blocke.a_repository.model.TasksRequest
import com.call_blocke.a_repository.request.GetProfileRequest
import com.call_blocke.a_repository.rest.SettingsRest
import com.call_blocke.a_repository.unit.NetworkInfo
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocker.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SettingsRepositoryImp : SettingsRepository() {

    private val settingsRest: SettingsRest
        get() = ApiRepositoryHelper.createRest(
            SettingsRest::class.java
        )

    override suspend fun updateSmsPerDay(context: Context) {
        val simInfo =
            SimUtil.getSIMInfo(context)
        val firstSimName =
            (simInfo?.firstOrNull { it.simSlotIndex == 0 }?.carrierName ?: "none").toString()
        val secondSimName =
            (simInfo?.firstOrNull { it.simSlotIndex == 1 }?.carrierName ?: "none").toString()
        val countryCode = simInfo?.firstOrNull()?.countryIso ?: "default"
        settingsRest.setSmsPerDay(
            SmsPerDayRequest(
                forSimFirst = currentSmsContFirstSimSlot,
                forSimSecond = currentSmsContSecondSimSlot,
                firstSimName = firstSimName,
                secondSimName = secondSimName,
                countryCode = countryCode,
                connectionType = NetworkInfo.connectionType()
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

    override suspend fun refreshDataForSim(simSlot: Int, iccid: String, number: String) {
        if (simSlot == 0) {
            SmsBlockerDatabase.firstSimChanged = false
        } else {
            SmsBlockerDatabase.secondSimChanged = false
        }
        settingsRest.resetSim(
            RefreshDataForSimRequest(
                simName = if (simSlot == 0)
                    "msisdn_1"
                else
                    "msisdn_2",
                simICCID = iccid,
                simNumber = number
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
                        simSlot = 0
                    )
                )
            }

            data.data.simSecond?.let {
                sims.add(
                    FullSimInfoModel(
                        simDate = it.updatedAt,
                        simDelivered = it.delivered,
                        simPerDay = it.smsPerDay,
                        simSlot = 1
                    )
                )
            }

            return sims
        } catch (_: Exception) {
        }
        return emptyList()
    }

    override suspend fun getProfile(): Flow<Resource<com.call_blocker.model.Profile>> = flow {
        emit(Resource.Loading<com.call_blocker.model.Profile>())
        try {
            emit(
                Resource.Success<com.call_blocker.model.Profile>(
                    settingsRest.getProfile(
                        GetProfileRequest(
                            uniqueId = SmsBlockerDatabase.deviceID,
                            protocolVersion = Const.protocolVersion,
                            appVersion = BuildConfig.versionName
                        )
                    ).data.toProfile()
                )
            )
        } catch (e: Exception) {
            emit(Resource.Error<com.call_blocker.model.Profile>(e.message ?: ""))
        }
    }

    override suspend fun checkConnection(): Resource<ConnectionStatus> {
        return try {
            Resource.Success<ConnectionStatus>(settingsRest.checkConnection().toConnectionStatus())
        } catch (e: Exception) {
            Resource.Error<ConnectionStatus>("")
        }
    }

    override suspend fun notifyServerUserStopService(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading<Unit>())
        try {
            emit(Resource.Success<Unit>(settingsRest.stopService()))
        } catch (e: Exception) {
            emit(Resource.Error<Unit>(e.message ?: ""))
        }
    }
}