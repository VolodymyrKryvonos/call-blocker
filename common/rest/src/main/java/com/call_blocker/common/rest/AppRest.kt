/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

class AppRest<T>(
    private val service: Class<T>,
    private val baseUrl: String = Const.url,
) {

    private var apiFactory: ApiFactory = ApiFactory().apply {
    }

    fun build(): T {
        return apiFactory
            .buildRetrofit(baseUrl)
            .create(service)
    }

}