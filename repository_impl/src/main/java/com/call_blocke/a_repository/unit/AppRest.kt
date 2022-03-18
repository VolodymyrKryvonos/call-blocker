/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocke.a_repository.unit

class AppRest<T>(private val baseUrl: String,
                 private val service: Class<T>)  {

    private var apiFactory: ApiFactory = ApiFactory().apply {
    }

    fun addHeader(name: String, value: String) : AppRest<T> {
        apiFactory.addHeader(name, value)
        return this
    }

    fun build(): T {
        return apiFactory
            .buildRetrofit(baseUrl)
            .create(service)
    }

}