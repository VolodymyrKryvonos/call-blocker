package com.call_blocker.verification.domain

import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.api.VerificationApi
import com.call_blocker.verification.data.model.*
import com.example.common.CountryCodeExtractor
import com.example.common.Resource
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class VerificationRepositoryImpl : VerificationRepository {
    private val verificationApi =
        AppRest(Const.url, SmsBlockerDatabase.userToken ?: "", VerificationApi::class.java).build()

    override suspend fun checkSim(
        simId: String,
        simSlot: Int,
        simVerificationInfo: MutableStateFlow<SimVerificationInfo>,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean
    ) {
        checkSimCard(
            simId,
            simSlot,
            phoneNumber,
            createAutoVerificationSms,
        )
            .collectLatest {
                simVerificationInfo.emit(it)
            }
    }


    override fun validateSimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ): Flow<Resource<Unit>> = flow {
        try {
            val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(simID)
            emit(Resource.Loading())
            verificationApi.validateSimCard(
                ValidateSimCardRequest(
                    simICCID = simID,
                    simNumber = phoneNumber,
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

    override fun checkSimCard(
        iccId: String,
        simSlot: Int,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean,
    ) = flow {
        try {
            val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(iccId)
            val response = verificationApi.checkSimCard(
                CheckSimCardRequest(
                    iccId,
                    countryCode,
                    "msisdn_${simSlot + 1}",
                    createAutoVerificationSms = createAutoVerificationSms,
                    phoneNumber = phoneNumber
                )
            )

            emit(response.toSimVerificationInfo())
        } catch (e: Exception) {
            emit(SimVerificationInfo(SimVerificationStatus.UNKNOWN))
            SmartLog.e("Failed check Sim Card ${getStackTrace(e)}")
        }
    }

    override fun confirmVerification(
        iccid: String,
        simSlot: String,
        verificationCode: String,
        phoneNumber: String,
        uniqueId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            verificationApi.confirmSimCardVerification(
                ConfirmSimCardVerificationRequest(
                    iccId = iccid,
                    simSlot = simSlot,
                    verificationCode = verificationCode,
                    phoneNumber = phoneNumber,
                    uniqueId = uniqueId
                )
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(""))
            SmartLog.e("Failed confirm validation ${getStackTrace(e)}")
        }

    }

}