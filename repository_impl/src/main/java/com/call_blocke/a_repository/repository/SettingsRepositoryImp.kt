package com.call_blocke.a_repository.repository

import android.content.Context
import androidx.annotation.RequiresPermission
import com.call_blocke.a_repository.BuildConfig
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.request.GetProfileRequest
import com.call_blocke.a_repository.rest.SettingsRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.call_blocker.model.ConnectionStatus
import com.call_blocker.model.Profile
import com.example.common.ConnectionManager
import com.example.common.CountryCodeExtractor
import com.example.common.Resource
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class SettingsRepositoryImp : SettingsRepository() {

    private val settingsRest: SettingsRest
        get() = AppRest(
            Const.url,
            SmsBlockerDatabase.userToken ?: "",
            SettingsRest::class.java
        ).build()

    override suspend fun updateSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int,
    ) {
        try {
            val simInfo =
                SimUtil.getSIMInfo(context)
            val firstSim = simInfo?.firstOrNull { it.simSlotIndex == 0 }
            val secondSim = simInfo?.firstOrNull { it.simSlotIndex == 1 }
            val firstSimName = if (firstSim == null) {
                "none"
            } else {
                firstSim.carrierName.ifEmpty { "unknown" }
            }
            (simInfo?.firstOrNull { it.simSlotIndex == 0 }?.carrierName ?: "none").toString()
            val secondSimName = if (secondSim == null) {
                "none"
            } else {
                secondSim.carrierName.ifEmpty { "unknown" }
            }

            val countryCode = CountryCodeExtractor.getCountryCode(context)
            SmartLog.e("CountryCode = $countryCode")
            settingsRest.setSmsPerDay(
                SmsPerDayRequest(
                    smsPerDaySimFirst = smsPerDaySimFirst,
                    smsPerDaySimSecond = smsPerDaySimSecond,
                    smsPerMonthSimFirst = smsPerMonthSimFirst,
                    smsPerMonthSimSecond = smsPerMonthSimSecond,
                    firstSimName = firstSimName.toString(),
                    secondSimName = secondSimName.toString(),
                    firstSimICCID = firstSim?.iccId ?: "",
                    secondSimICCID = secondSim?.iccId ?: "",
                    countryCode = countryCode,
                    connectionType = ConnectionManager.getNetworkGeneration()
                )
            )

            SmsBlockerDatabase.smsPerDaySimFirst = smsPerDaySimFirst
            SmsBlockerDatabase.smsPerDaySimSecond = smsPerDaySimSecond
            SmsBlockerDatabase.smsPerMonthSimFirst = smsPerMonthSimFirst
            SmsBlockerDatabase.smsPerMonthSimSecond = smsPerMonthSimSecond
        } catch (e: Exception) {
            SmartLog.e("Failed update sms per day ${getStackTrace(e)}")
        }
    }

    override suspend fun refreshDataForSim(simSlot: Int, context: Context) {
        try {
            if (simSlot == 0) {
                SmsBlockerDatabase.firstSimChanged = false
            } else {
                SmsBlockerDatabase.secondSimChanged = false
            }
            val countryCode = CountryCodeExtractor.getCountryCode(context)
            val simInfo = SimUtil.simInfo(context, simSlot)
            settingsRest.resetSim(
                RefreshDataForSimRequest(
                    simName = if (simSlot == 0)
                        "msisdn_1"
                    else
                        "msisdn_2",
                    simICCID = simInfo?.iccId ?: "",
                    simNumber = simInfo?.number ?: "",
                    countryCode = countryCode
                )
            )
        } catch (e: Exception) {
            SmartLog.e("Failed reset sim ${getStackTrace(e)}")
        }
    }


    override suspend fun simInfo(
        context: Context
    ): List<FullSimInfoModel> {
        try {
            val sims = arrayListOf<FullSimInfoModel>()
            val data = settingsRest.simInfo(
                SimInfoRequest(
                    firstSimId = SimUtil.firstSim(context)?.iccId,
                    secondSimId = SimUtil.secondSim(context)?.iccId,
                    countryCode = CountryCodeExtractor.getCountryCode(
                        context
                    )
                )
            ).toSimInfoResponse()

            data.simFirst?.let {
                sims.add(
                    FullSimInfoModel(
                        simDate = it.updatedAt,
                        simDelivered = it.delivered,
                        simPerDay = it.smsPerDay,
                        simSlot = 0
                    )
                )
            }

            data.simSecond?.let {
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
        } catch (e: Exception) {
            SmartLog.e("Failed get sim info ${getStackTrace(e)}")
        }
        return emptyList()
    }

    override suspend fun getProfile(): Flow<Resource<Profile>> = flow {
        emit(Resource.Loading<com.call_blocker.model.Profile>())
        try {
            val profile = settingsRest.getProfile(
                GetProfileRequest(
                    uniqueId = SmsBlockerDatabase.deviceID,
                    protocolVersion = Const.protocolVersion,
                    appVersion = BuildConfig.versionName
                )
            ).data.toProfile()
            SmartLog.e("Profile: $profile")
            emit(
                Resource.Success<com.call_blocker.model.Profile>(
                    profile
                )
            )
        } catch (e: Exception) {
            SmartLog.e("Failed get profile ${getStackTrace(e)}")
            emit(Resource.Error<com.call_blocker.model.Profile>(e.message ?: ""))
        }
    }

    override suspend fun checkConnection(
        context: Context
    ): Resource<ConnectionStatus> {
        return try {
            Resource.Success<ConnectionStatus>(
                settingsRest.checkConnection(
                    SimInfoRequest(
                        firstSimId = SimUtil.firstSim(context)?.iccId,
                        secondSimId = SimUtil.secondSim(context)?.iccId,
                        countryCode = CountryCodeExtractor.getCountryCode(
                            context
                        )
                    )
                ).toConnectionStatus()
            )
        } catch (e: Exception) {
            SmartLog.e("Failed check connection ${getStackTrace(e)}")
            Resource.Error<ConnectionStatus>("")
        }
    }

    override suspend fun notifyServerUserStopService(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading<Unit>())
        try {
            emit(Resource.Success<Unit>(settingsRest.stopService()))
        } catch (e: Exception) {
            SmartLog.e("Failed notify server ${getStackTrace(e)}")
            emit(Resource.Error<Unit>(e.message ?: ""))
        }
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    override suspend fun sendSignalStrengthInfo(
        context: Context
    ) {
        try {
            val firstSim = SimUtil.firstSim(context)
            val secondSim = SimUtil.secondSim(context)
            settingsRest.sendSignalStrengthInfo(
                SignalStrengthRequest(
                    signalStrength = ConnectionManager.getSignalStrength(),
                    signalGeneration = ConnectionManager.getNetworkGeneration(),
                    firstSimId = firstSim?.iccId,
                    secondSimId = secondSim?.iccId,
                    firstSimOperator = firstSim?.carrierName?.toString(),
                    secondSimOperator = secondSim?.carrierName?.toString(),
                    countryCode = CountryCodeExtractor.getCountryCode(
                        context
                    )
                )
            )
        } catch (e: Exception) {
            SmartLog.e("Failed send signal strength ${getStackTrace(e)}")
        }
    }

    override suspend fun changeSimCard(
        context: Context
    ) {
        try {
            val firstSim = SimUtil.firstSim(context)
            val secondSim = SimUtil.secondSim(context)
            val countryCode = CountryCodeExtractor.getCountryCode(context)
            val response = settingsRest.changeSimCard(
                ChangeSimCardRequest(
                    firstSimId = firstSim?.iccId,
                    secondSimId = secondSim?.iccId,
                    firstSimOperator = firstSim?.carrierName.toString(),
                    secondSimOperator = secondSim?.carrierName.toString(),
                    countryCode = countryCode
                )
            )
            if (response.status) {
                SmsBlockerDatabase.smsPerDaySimFirst = response.firstSimLimit
                SmsBlockerDatabase.smsPerDaySimSecond = response.secondSimLimit
            }
        } catch (e: Exception) {
            SmartLog.e("Failed send change sim card request ${getStackTrace(e)}")
        }
    }
}