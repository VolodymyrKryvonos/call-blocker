package com.call_blocke.rest_work_imp

import android.content.Context
import com.call_blocke.db.SmsBlockerDatabase

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
        }

        return userToken != null
    }



}