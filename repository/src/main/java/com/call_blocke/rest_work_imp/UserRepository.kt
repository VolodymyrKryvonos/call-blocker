package com.call_blocke.rest_work_imp

import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.SystemDetailEntity
import com.example.common.Resource
import kotlinx.coroutines.flow.Flow

abstract class UserRepository {

    /**
     * Implement rest
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return auth token
     */
    protected abstract suspend fun doLogin(
        email: String,
        password: String,
        version: String
    ): String

    /**
     * Implement rest
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return auth token
     */
    protected abstract suspend fun doRegister(
        email: String,
        password: String,
        packageName: String,
        versionName: String
    ): String

    protected abstract suspend fun loadSystemDetail(
        firstSimId: String?,
        secondSimId: String?
    ): SystemDetailEntity

    /**
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return success result
     */
    suspend fun login(email: String, password: String, version: String): Boolean {
        val userToken: String? = try {
            doLogin(email, password, version)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (userToken != null) {
            SmsBlockerDatabase.userToken = userToken
            SmsBlockerDatabase.userPassword = password
        }

        return userToken != null
    }


    abstract fun reset(email: String): Flow<Resource<Unit>>

    /**
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return success result
     */
    suspend fun register(
        email: String,
        password: String,
        packageName: String,
        versionName: String
    ): Boolean {
        val userToken: String? = try {
            doRegister(email, password, packageName, versionName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (userToken != null) {
            SmsBlockerDatabase.userToken = userToken
            SmsBlockerDatabase.userPassword = password
        }

        return userToken != null
    }

    suspend fun systemDetail(
        firstSimId: String?,
        secondSimId: String?
    ): SystemDetailEntity {
        SmsBlockerDatabase.systemDetail = loadSystemDetail(
            firstSimId,
            secondSimId
        )
        return SmsBlockerDatabase.systemDetail
    }

    fun logOut() {
        SmsBlockerDatabase.userToken = null
    }

    val deviceID = SmsBlockerDatabase.deviceID

    fun userName() = SmsBlockerDatabase.userName

    fun userPassword() = SmsBlockerDatabase.userPassword


}