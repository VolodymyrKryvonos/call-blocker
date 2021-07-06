package com.call_blocke.a_repository

import com.call_blocke.a_repository.unit.AppRest
import com.call_blocke.db.SmsBlockerDatabase

object ApiRepositoryHelper {

    fun <T> createRest(service: Class<T>): T {
        return AppRest("http://app-sms.phd.com.ua/api/v1/", service)
            .apply {
                SmsBlockerDatabase.userToken?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
    }

}