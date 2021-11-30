package com.call_blocke.rest_work_imp

import android.content.Context
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.SystemDetailEntity

abstract class UserRepository {

    /**
     * Implement rest
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return auth token
     */
    protected abstract suspend fun doLogin(email: String, password: String, context: Context): String

    /**
     * Implement rest
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return auth token
     */
    protected abstract suspend fun doRegister(email: String, password: String, context: Context): String

    protected abstract suspend fun  loadSystemDetail(): SystemDetailEntity

    /**
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return success result
     */
    suspend fun login(email: String, password: String): Boolean {
        val userToken: String? = try {
            doLogin(email, password, RepositoryBuilder.mContext)
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

    /**
     * @param email is accounts`s login field
     * @param password is accounts`s password field
     * @return success result
     */
    suspend fun register(email: String, password: String): Boolean {
        val userToken: String? = try {
            doRegister(email, password, RepositoryBuilder.mContext)
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

    suspend fun systemDetail(): SystemDetailEntity {
        SmsBlockerDatabase.systemDetail = loadSystemDetail()
        return SmsBlockerDatabase.systemDetail
    }

    fun logOut() {
        SmsBlockerDatabase.userToken = null
    }

    val deviceID = SmsBlockerDatabase.deviceID

    fun userName() = SmsBlockerDatabase.userName

    fun userPassword() = SmsBlockerDatabase.userPassword


}