/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

class AppRest<T>(
    private val service: Class<T>
) {

    private var apiFactory: ApiFactory = ApiFactory().apply {
    }

    fun build(): T {
        return apiFactory
            .buildRetrofit(Const.url)
            .create(service)
    }

}