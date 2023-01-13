package com.call_blocke.a_repository.repository

import android.content.Context
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import com.call_blocke.a_repository.BuildConfig
import com.call_blocke.a_repository.Const
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.request.GetProfileRequest
import com.call_blocke.a_repository.rest.SettingsRest
import com.call_blocke.a_repository.unit.NetworkInfo
import com.call_blocke.db.AutoValidationResult
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.call_blocker.model.ConnectionStatus
import com.example.common.ConnectionManager
import com.example.common.CountryCodeExtractor
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class SettingsRepositoryImp : SettingsRepository() {

    private val settingsRest: SettingsRest
        get() = ApiRepositoryHelper.createRest(
            SettingsRest::class.java
        )

    override suspend fun updateSmsPerDay(context: Context, sim1Limit: Int, sim2Limit: Int) {
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

            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCode = CountryCodeExtractor.getCountryCode(simInfo, tm)
            SmartLog.e("CountryCode = $countryCode")
            settingsRest.setSmsPerDay(
                SmsPerDayRequest(
                    forSimFirst = sim1Limit,
                    forSimSecond = sim2Limit,
                    firstSimName = firstSimName.toString(),
                    secondSimName = secondSimName.toString(),
                    firstSimICCID = firstSim?.iccId ?: "",
                    secondSimICCID = secondSim?.iccId ?: "",
                    countryCode = countryCode,
                    connectionType = NetworkInfo.connectionType()
                )
            )

            SmsBlockerDatabase.smsPerDaySimFirst = sim1Limit
            SmsBlockerDatabase.smsPerDaySimSecond = sim2Limit
        } catch (e: Exception) {
            SmartLog.e("Failed update sms per day ${getStackTrace(e)}")
        }
    }

    override suspend fun blackPhoneNumberList(): List<String> {
        return settingsRest.blackList(
            TasksRequest()
        ).data.map {
            it.number
        }
    }

    override suspend fun refreshDataForSim(simSlot: Int, iccid: String, number: String) {
        try {
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
        } catch (e: Exception) {
            SmartLog.e("Failed reset sim ${getStackTrace(e)}")
        }
    }

    override suspend fun validateSimCard(
        phoneNumber: String,
        simID: String,
        monthlyLimit: Int,
        simSlot: Int
    ): Flow<Resource<Unit>> = flow {
        try {
            val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(simID)
            emit(Resource.Loading())
            settingsRest.validateSimCard(
                ValidateSimCardRequest(
                    simICCID = simID,
                    simNumber = phoneNumber,
                    monthlyLimit = monthlyLimit,
                    simSlot = "msisdn_${simSlot + 1}",
                    countryCode = countryCode
                )
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(""))
            SmartLog.e("Failed validate phone number ${getStackTrace(e)}")
        }
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
        } catch (e: Exception) {
            SmartLog.e("Failed get sim info ${getStackTrace(e)}")
        }
        return emptyList()
    }

    override suspend fun getProfile(): Flow<Resource<com.call_blocker.model.Profile>> = flow {
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

    override suspend fun checkConnection(): Resource<ConnectionStatus> {
        return try {
            Resource.Success<ConnectionStatus>(settingsRest.checkConnection().toConnectionStatus())
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

    override suspend fun checkSimCard(
        iccId: String,
        simSlot: Int,
        createAutoVerificationSms: Boolean,
    ) = flow {
        try {
            val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(iccId)
            val response = settingsRest.checkSimCard(
                CheckSimCardRequest(
                    iccId,
                    countryCode,
                    "msisdn_${simSlot + 1}",
                    createAutoVerificationSms = createAutoVerificationSms
                )
            )
            if (response.status) {
                if (simSlot == 0) {
                    SmsBlockerDatabase.simFirstAutoValidationResult = AutoValidationResult.SUCCESS
                } else {
                    SmsBlockerDatabase.simSecondAutoValidationResult = AutoValidationResult.SUCCESS
                }
            }
            emit(response.toSimValidationInfo())
        } catch (e: Exception) {
            emit(SimValidationInfo(SimValidationStatus.UNKNOWN, ""))
            SmartLog.e("Failed check Sim Card ${getStackTrace(e)}")
        }
    }

    override suspend fun confirmValidation(
        iccid: String,
        simSlot: String,
        verificationCode: String,
        phoneNumber: String,
        uniqueId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading<Unit>())
        try {
            settingsRest.confirmSimCardValidation(
                ConfirmSimCardValidationRequest(
                    iccId = iccid,
                    simSlot = simSlot,
                    verificationCode = verificationCode,
                    phoneNumber = phoneNumber,
                    uniqueId = uniqueId
                )
            )
            emit(Resource.Success<Unit>(Unit))
        } catch (e: Exception) {
            emit(Resource.Error<Unit>(""))
            SmartLog.e("Failed confirm validation ${getStackTrace(e)}")
        }

    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    override suspend fun sendSignalStrengthInfo() {
        try {
            settingsRest.sendSignalStrengthInfo(
                SignalStrengthRequest(
                    signalStrength = ConnectionManager.getSignalStrength()
                        ?: throw Exception("Signal strength is null"),
                    signalGeneration = ConnectionManager.getNetworkGeneration()
                )
            )
        } catch (e: Exception) {
            SmartLog.e("Failed send signal strength ${getStackTrace(e)}")
        }
    }
}