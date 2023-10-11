package com.call_blocker.a_repository.repository

import android.content.Context
import android.os.Build
import com.call_blocker.a_repository.model.LoginRequest
import com.call_blocker.a_repository.model.RegisterRequest
import com.call_blocker.a_repository.model.ResetRequest
import com.call_blocker.a_repository.model.TasksRequest
import com.call_blocker.a_repository.rest.UserRest
import com.call_blocker.common.ConnectionManager
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.Resource
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.db.entity.SystemDetailEntity
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.UserRepository
import kotlinx.coroutines.flow.flow

class UserRepositoryImp(
    private val userRest: UserRest,
    smsBlockerDatabase: SmsBlockerDatabase
) : UserRepository(smsBlockerDatabase) {
    override suspend fun doLogin(
        email: String,
        password: String,
        version: String
    ): String {
        return userRest.signIn(
            LoginRequest(
                email = email,
                password = password,
                version = version
            )
        ).data.success.token
    }

    override suspend fun doRegister(
        email: String,
        password: String,
        whatsApp: String,
        packageName: String,
        versionName: String
    ): String {
        return userRest.signUp(
            RegisterRequest(
                email = email,
                password = password,
                nameOfPackage = packageName,
                versionOfPackage = versionName,
                deviceModel = Build.MODEL,
                deviceType = "Smartphone",
                campaign = "App Sms",
                connectionType = "WIFI",
                msisdn = "",
                deviceBrand = Build.BRAND,
                whatsApp = whatsApp
            )
        ).data.success.token
    }

    override suspend fun loadSystemDetail(
        context: Context
    ): SystemDetailEntity {
        val data = try {
            userRest.userInfo(
                TasksRequest(
                    connectionType = ConnectionManager.getNetworkGeneration(),
                    firstSimId = SimUtil.firstSim(context)?.iccId,
                    secondSimId = SimUtil.secondSim(context)?.iccId,
                    countryCode = CountryCodeExtractor.getCountryCode(
                        context
                    )
                )
            ).toUserInfo()
                .let {
                    smsBlockerDatabase.userName = "${it.user.name} ${it.user.lastName}"
                    it
                }
        } catch (e: Exception) {
            SmartLog.e("Failed load system details ${getStackTrace(e)}")
            // SmsBlockerDatabase.userToken = null
            null
        }?.user ?: return smsBlockerDatabase.systemDetail

        return try {
            SystemDetailEntity(
                leftCount = data.details.leftCount,
                deliveredCount = data.details.deliveredCount,
                undeliveredCount = data.details.undeliveredCount,
                amount = data.calculation,
                firstName = data.name ?: "",
                lastName = data.lastName ?: ""
            )
        } catch (e: Exception) {
            SystemDetailEntity()
        }
    }

    override fun reset(email: String) = flow {
        try {
            emit(Resource.Loading<Unit>())
            userRest.reset(ResetRequest(email))
            emit(Resource.Success<Unit>(Unit))
        } catch (e: Exception) {
            emit(Resource.Error<Unit>(""))
        }
    }

}