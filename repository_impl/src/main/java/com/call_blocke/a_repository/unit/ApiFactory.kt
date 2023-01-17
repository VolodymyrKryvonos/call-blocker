/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocke.a_repository.unit

import com.call_blocke.a_repository.BuildConfig
import com.call_blocke.a_repository.Const
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstvv_unit.loger.SmartLog
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit

class ApiFactory {

    private var builder: OkHttpClient.Builder? = null

    init {
        builder = OkHttpClient.Builder()
        builder?.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.addInterceptor(HostSelectionInterceptor())
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
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder!!.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    class HostSelectionInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val host: String = SmsBlockerDatabase.profile?.url?:Const.url
            var request: Request = chain.request()
            val newHost = URI(host)
            val newUrl = ("${newHost.scheme}://${newHost.host}${request.url.encodedPath}").toHttpUrlOrNull()
            if (newUrl != null) {
                request = request.newBuilder()
                    .url(newUrl)
                    .build()
            }
            return chain.proceed(request)
        }
    }

    companion object {

        private const val DEFAULT_TIMEOUT = 15
        const val CONNECT_TIMEOUT = DEFAULT_TIMEOUT
        const val WRITE_TIMEOUT = DEFAULT_TIMEOUT
        const val READ_TIMEOUT = DEFAULT_TIMEOUT
    }
}