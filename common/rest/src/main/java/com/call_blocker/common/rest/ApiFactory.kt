/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

import com.call_blocker.db.SmsBlockerDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Interceptor.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.koin.java.KoinJavaComponent.get
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
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
        builder?.addInterceptor(AuthorizationInterceptor())
        builder?.addInterceptor(UnauthorizedInterceptor())
        builder?.addInterceptor(UniqueIdInterceptor())
//        val logging = if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//        } else {
//            HttpLoggingInterceptor {
//                SmartLog.e(it)
//            }.apply {
//                level = HttpLoggingInterceptor.Level.BASIC
//            }
//        }
//        builder?.addInterceptor(logging)
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

internal class UniqueIdInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {

        val smsBlockerDatabase: SmsBlockerDatabase = get(SmsBlockerDatabase::class.java)
        val originalRequest = chain.request()
        if (originalRequest.method == "POST" &&
            originalRequest.body?.contentType()?.subtype?.contains("json") == true
        ) {
            val newBody = JSONObject(originalRequest.body.bodyToString())
            newBody.put("unique_id", smsBlockerDatabase.deviceID)

            return chain.proceed(
                originalRequest.newBuilder()
                    .post(
                        newBody
                            .toString()
                            .toRequestBody(originalRequest.body?.contentType())
                    )
                    .build()
            )
        }
        return chain.proceed(originalRequest)
    }
}

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}

internal class AuthorizationInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {

        val smsBlockerDatabase: SmsBlockerDatabase = get(SmsBlockerDatabase::class.java)
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${smsBlockerDatabase.userToken}")
            .build();
        return chain.proceed(request)
    }
}

internal class UnauthorizedInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val smsBlockerDatabase: SmsBlockerDatabase = get(SmsBlockerDatabase::class.java)
        val response: Response = chain.proceed(chain.request())
        if (response.code == 401) {
            smsBlockerDatabase.userToken = null
        }
        return response
    }
}