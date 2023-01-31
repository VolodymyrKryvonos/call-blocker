package com.call_blocker.verification.domain

import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.api.VerificationApi
import com.call_blocker.verification.data.model.AutoVerificationRequest
import com.call_blocker.verification.data.model.CheckSimCardRequest
import com.call_blocker.verification.data.model.ConfirmSimCardVerificationRequest
import com.call_blocker.verification.data.model.VerifySimCardRequest
import com.example.common.CountryCodeExtractor
import com.example.common.Resource
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VerificationRepositoryImpl : VerificationRepository {
    private val verificationApi =
        AppRest(Const.url, SmsBlockerDatabase.userToken ?: "", VerificationApi::class.java).build()

    override fun checkSimCard(
        iccId: String,
        simSlot: Int,
        phoneNumber: String?
    ) = flow {
        try {
            emit(Resource.Loading())
            val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(iccId)
            val response = verificationApi.checkSimCard(
                CheckSimCardRequest(
                    iccId,
                    countryCode,
                    "msisdn_${simSlot + 1}",
                    phoneNumber = phoneNumber
                )
            )
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(""))
            SmartLog.e("Failed check Sim Card ${getStackTrace(e)}")
        }
    }

    override fun confirmVerification(
        iccid: String,
        simSlot: Int,
        verificationCode: String,
        phoneNumber: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            verificationApi.confirmSimCardVerification(
                ConfirmSimCardVerificationRequest(
                    iccId = iccid,
                    simSlot = "msisdn_${simSlot + 1}",
                    verificationCode = verificationCode,
                    phoneNumber = phoneNumber
                )
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(""))
            SmartLog.e("Failed confirm validation ${getStackTrace(e)}")
        }

    }

    override fun verifySimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ): Flow<Resource<Unit>> =
        flow {
            try {
                emit(Resource.Loading())
                val countryCode = CountryCodeExtractor.getCountryCodeFromIccId(simID)
                if (phoneNumber.isEmpty()) {
                    verificationApi.startAutoVerification(
                        AutoVerificationRequest(
                            simID = simID,
                            simSlot = "msisdn_${simSlot + 1}",
                            countryCode = countryCode
                        )
                    )
                } else {
                    verificationApi.verifySimCard(
                        VerifySimCardRequest(
                            simICCID = simID,
                            simNumber = phoneNumber,
                            simSlot = "msisdn_${simSlot + 1}",
                            countryCode = countryCode
                        )
                    )
                }
                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(""))
                SmartLog.e("Failed validate phone number ${getStackTrace(e)}")
            }
        }

}