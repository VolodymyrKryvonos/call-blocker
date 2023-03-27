package com.call_blocke.a_repository.repository

import android.content.Context
import android.os.Build
import com.call_blocke.a_repository.model.LoginRequest
import com.call_blocke.a_repository.model.RegisterRequest
import com.call_blocke.a_repository.model.ResetRequest
import com.call_blocke.a_repository.model.TasksRequest
import com.call_blocke.a_repository.rest.UserRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.SystemDetailEntity
import com.call_blocke.rest_work_imp.UserRepository
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.example.common.ConnectionManager
import com.example.common.CountryCodeExtractor
import com.example.common.Resource
import com.example.common.SimUtil
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.flow

class UserRepositoryImp : UserRepository() {

    private val userRest: UserRest
        get() = AppRest(Const.url, SmsBlockerDatabase.userToken ?: "", UserRest::class.java).build()


    override suspend fun doLogin(
        email: String,
        password: String,
        version: String
    ): String {
        return userRest.signIn(
            LoginRequest(
                uniqueId = SmsBlockerDatabase.deviceID,
                email = email,
                password = password,
                version = version
            )
        ).data.success.token
    }

    override suspend fun doRegister(
        email: String, password: String,
        packageName: String,
        versionName: String
    ): String {
        return userRest.signUp(
            RegisterRequest(
                uniqueId = SmsBlockerDatabase.deviceID,
                email = email,
                password = password,
                nameOfPackage = packageName,
                versionOfPackage = versionName,
                deviceModel = Build.MODEL,
                deviceType = "Smarthone",
                campaign = "App Sms",
                connectionType = "WIFI",
                msisdn = "",
                deviceBrand = Build.BRAND
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
                    SmsBlockerDatabase.userName = "${it.user.name} ${it.user.lastName}"
                    it
                }
        } catch (e: Exception) {
            SmartLog.e("Failed load system details ${getStackTrace(e)}")
            // SmsBlockerDatabase.userToken = null
            null
        }?.user ?: return SmsBlockerDatabase.systemDetail

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