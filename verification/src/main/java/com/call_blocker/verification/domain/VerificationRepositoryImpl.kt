package com.call_blocker.verification.domain

import android.content.Context
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.api.VerificationApi
import com.call_blocker.verification.data.model.AutoVerificationRequest
import com.call_blocker.verification.data.model.CheckSimCardRequest
import com.call_blocker.verification.data.model.ConfirmSimCardVerificationRequest
import com.call_blocker.verification.data.model.VerifySimCardRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VerificationRepositoryImpl : VerificationRepository {
    private val verificationApi =
        AppRest(Const.url, SmsBlockerDatabase.userToken ?: "", VerificationApi::class.java).build()

    override fun checkSimCard(
        context: Context,
        simSlot: Int,
    ) = flow {
        try {
            emit(Resource.Loading())
            val countryCode = CountryCodeExtractor.getCountryCode(context)
            val response = verificationApi.checkSimCard(
                CheckSimCardRequest(
                    SimUtil.simInfo(context, simSlot)?.iccId ?: "",
                    countryCode,
                    "msisdn_${simSlot + 1}",
                    phoneNumber = SimUtil.simInfo(context, simSlot)?.number
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
        verificationCode: String,
        phoneNumber: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            verificationApi.confirmSimCardVerification(
                ConfirmSimCardVerificationRequest(
                    iccId = iccid,
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
        context: Context,
        simSlot: Int
    ): Flow<Resource<Unit>> =
        flow {
            try {
                val simID = SimUtil.simInfo(context, simSlot)?.iccId ?: ""
                val phoneNumber = SimUtil.simInfo(context, simSlot)?.number ?: ""
                emit(Resource.Loading())
                val countryCode = CountryCodeExtractor.getCountryCode(context)
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