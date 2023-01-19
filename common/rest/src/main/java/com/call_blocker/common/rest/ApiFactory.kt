/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

import com.rokobit.adstvv_unit.loger.SmartLog
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory {

    private var builder: OkHttpClient.Builder? = null

    init {
        builder = OkHttpClient.Builder()
        builder?.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("Accept", "application/json; charset=utf-8")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .header("platform", "android")
                .method(original.method, original.body)

            val request = requestBuilder
                .cacheControl(CacheControl.Builder().noCache().build())
                .build()

            chain.proceed(request)
        }

        val logging = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else {
            HttpLoggingInterceptor { SmartLog.e(it) }.apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        }
        builder?.addInterceptor(logging)
    }

    fun addHeader(name: String, value: String) {
        builder?.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader(name, value)

            val request = requestBuilder
                .cacheControl(CacheControl.Builder().noCache().build())
                .build()

            chain.proceed(request)
        }
    }

    fun buildRetrofit(baseUrl: String): Retrofit {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder!!.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    companion object {

        private const val DEFAULT_TIMEOUT = 15
        const val CONNECT_TIMEOUT = DEFAULT_TIMEOUT
        const val WRITE_TIMEOUT = DEFAULT_TIMEOUT
        const val READ_TIMEOUT = DEFAULT_TIMEOUT
    }
}