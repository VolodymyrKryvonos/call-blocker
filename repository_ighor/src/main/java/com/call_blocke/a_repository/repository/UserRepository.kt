package com.call_blocke.a_repository.repository

import android.content.Context
import com.call_blocke.a_repository.rest.UserRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.UserRepository
import android.os.Build
import com.call_blocke.a_repository.model.*
import com.call_blocke.db.entity.SystemDetailEntity

class UserRepositoryImp : UserRepository() {

    private val userRest: UserRest
        get() = ApiRepositoryHelper.createRest(
            UserRest::class.java
        )

    override suspend fun doLogin(email: String, password: String, context: Context): String {
        return userRest.signIn(
            LoginRequest(
                uniqueId = SmsBlockerDatabase.deviceID,
                email = email,
                password = password
            )
        ).data.success.token
    }

    override suspend fun doRegister(email: String, password: String, context: Context): String {
        return userRest.signUp(
            RegisterRequest(
                uniqueId = SmsBlockerDatabase.deviceID,
                email = email,
                password = password,
                nameOfPackage = context.packageName,
                versionOfPackage = kotlin.run {
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    return@run pInfo.versionName
                },
                deviceModel = Build.MODEL,
                deviceType = "Smarthone",
                campaign = "App Sms",
                connectionType = "WIFI",
                msisdn = "",
                deviceBrand = Build.BRAND
            )
        ).data.success.token
    }

    override suspend fun loadSystemDetail(): SystemDetailEntity {
        val data = try {
            userRest.userInfo(TasksRequest())
        } catch (e: Exception) {
            SmsBlockerDatabase.userToken = null
            null
        }?.data?.user ?: return SmsBlockerDatabase.systemDetail

        return try {
            SystemDetailEntity(
                leftCount = data.details.leftCount,
                deliveredCount = data.details.deliveredCount,
                undeliveredCount = data.details.undeliveredCount,
                amount = data.calculation
            )
        } catch (e: Exception) {
            SystemDetailEntity()
        }
    }

}