package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.unit.AppRest
import com.call_blocke.db.SmsBlockerDatabase

object ApiRepositoryHelper {

    fun <T> createRest(service: Class<T>): T {
        return AppRest("https://free-tokens.info/api/v1/", service)
            .apply {
                SmsBlockerDatabase.userToken?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
    }

}