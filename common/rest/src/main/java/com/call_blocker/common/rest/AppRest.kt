/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

class AppRest<T>(
    private val baseUrl: String,
    private val bearerToken: String,
    private val service: Class<T>
) {

    private var apiFactory: ApiFactory = ApiFactory().apply {
    }

    fun build(): T {
        apiFactory
            .addHeader("Authorization", "Bearer $bearerToken")
        return apiFactory
            .buildRetrofit(baseUrl)
            .create(service)
    }

}