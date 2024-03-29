package com.call_blocker.a_repository.repository

import android.content.Context
import android.telephony.SubscriptionInfo
import com.call_blocker.a_repository.BuildConfig
import com.call_blocker.a_repository.model.ChangeSimCardRequest
import com.call_blocker.a_repository.model.RefreshDataForSimRequest
import com.call_blocker.a_repository.model.SignalStrengthRequest
import com.call_blocker.a_repository.model.SimInfoRequest
import com.call_blocker.a_repository.model.SmsPerDayRequest
import com.call_blocker.a_repository.request.GetProfileRequest
import com.call_blocker.a_repository.rest.SettingsRest
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.common.getNetworkGeneration
import com.call_blocker.common.rest.Const
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.model.ConnectionStatus
import com.call_blocker.model.Profile
import com.call_blocker.rest_work_imp.FullSimInfoModel
import com.call_blocker.rest_work_imp.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class SettingsRepositoryImp(
    private val settingsRest: SettingsRest,
    private val smsBlockerDatabase: SmsBlockerDatabase
) : SettingsRepository {
    override suspend fun setSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int
    ) {
        try {
            updateSmsPerDay(
                context,
                smsPerDaySimFirst,
                smsPerDaySimSecond,
                smsPerMonthSimFirst,
                smsPerMonthSimSecond
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                    connectionType = getNetworkGeneration(context)
                )
            )

            smsBlockerDatabase.smsPerDaySimFirst = smsPerDaySimFirst
            smsBlockerDatabase.smsPerDaySimSecond = smsPerDaySimSecond
            smsBlockerDatabase.smsPerMonthSimFirst = smsPerMonthSimFirst
            smsBlockerDatabase.smsPerMonthSimSecond = smsPerMonthSimSecond
        } catch (e: Exception) {
            SmartLog.e("Failed update sms per day ${getStackTrace(e)}")
        }
    }

    override suspend fun refreshDataForSim(simSlot: Int, context: Context) {
        try {
            if (simSlot == 0) {
                smsBlockerDatabase.firstSimChanged = false
            } else {
                smsBlockerDatabase.secondSimChanged = false
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

    override suspend fun sendSignalStrengthInfo(
        firstSimSignal: Int,
        secondSimSignal: Int,
        wifiSignal: Int,
        firstSim: SubscriptionInfo?,
        secondSim: SubscriptionInfo?,
        countryCode: String
    ) {
        try {
            settingsRest.sendSignalStrengthInfo(
                SignalStrengthRequest(
                    firstSimSignalStrength = firstSimSignal,
                    secondSimSignalStrength = secondSimSignal,
                    wifiSignalStrength = wifiSignal,
                    firstSimId = firstSim?.iccId,
                    secondSimId = secondSim?.iccId,
                    firstSimOperator = firstSim?.carrierName?.toString(),
                    secondSimOperator = secondSim?.carrierName?.toString(),
                    countryCode = countryCode,
                    ipAddress = getIpAddress()
                )
            )
        } catch (e: Exception) {
            SmartLog.e("Failed send signal strength ${getStackTrace(e)}")
        }
    }

    private fun getIpAddress(): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api64.ipify.org?format=json")
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse.optString("ip", "")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
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
                smsBlockerDatabase.smsPerDaySimFirst = response.firstSimLimit
                smsBlockerDatabase.smsPerDaySimSecond = response.secondSimLimit
            }
        } catch (e: Exception) {
            SmartLog.e("Failed send change sim card request ${getStackTrace(e)}")
        }
    }
}