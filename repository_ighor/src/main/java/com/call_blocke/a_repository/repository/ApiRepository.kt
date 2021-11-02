package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.Const
import com.call_blocke.a_repository.unit.AppRest
import com.call_blocke.db.SmsBlockerDatabase

object ApiRepositoryHelper {

    fun <T> createRest(service: Class<T>): T {
        return AppRest(Const.url, service)
            .apply {
                SmsBlockerDatabase.userToken?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
    }

}