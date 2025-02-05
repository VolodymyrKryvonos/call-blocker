/*
 * Copyright (c) 2020.
 * Nkita Knyazevkiy
 * UA
 */

package com.call_blocker.common.rest

import com.call_blocker.db.BuildConfig
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.Interceptor.Chain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.koin.java.KoinJavaComponent.get
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager


class ApiFactory {

    private var builder: OkHttpClient.Builder? = null

    init {
        builder = OkHttpClient.Builder()
        builder?.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        builder?.addInterceptor(HeadersInterceptor())
        builder?.addInterceptor(AuthorizationInterceptor())
        builder?.addInterceptor(UnauthorizedInterceptor())
        builder?.addInterceptor(UniqueIdInterceptor())

        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {

            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        builder?.sslSocketFactory(sslSocketFactory, trustAllCerts[0])
        builder?.hostnameVerifier { hostname, session -> true }


        val logging = HttpLoggingInterceptor {
            SmartLog.e(it)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        builder?.addInterceptor(logging)
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


internal class HeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .addHeader("Accept", "application/json; charset=utf-8")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .header("platform", "android")
            .method(original.method, original.body)
        return chain.proceed(requestBuilder.build())
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
        val token = smsBlockerDatabase.userToken ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}


internal class UnauthorizedInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        if (response.code == 401) {
            tryAutoLogin()
        }
        return response
    }

    private fun tryAutoLogin() {
        CoroutineScope(Job()).launch {
            val smsBlockerDatabase: SmsBlockerDatabase = get(SmsBlockerDatabase::class.java)
            val email = smsBlockerDatabase.email ?: return@launch
            val password = smsBlockerDatabase.password ?: return@launch

            val body = JSONObject()
            body.put("email", email)
            body.put("password", password)
            body.put("version_of_package", BuildConfig.versionName)
            OkHttpClient
                .Builder()
                .addInterceptor(HeadersInterceptor())
                .addInterceptor(UniqueIdInterceptor())
                .build()
                .newCall(
                    Request
                        .Builder()
                        .post(
                            body
                                .toString()
                                .toRequestBody("application/json".toMediaTypeOrNull())
                        )
                        .url("${Const.url}login")
                        .build()
                ).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        smsBlockerDatabase.userToken = null
                        SmartLog.e("tryAutoLogin ${getStackTrace(e)}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            SmartLog.e("onResponse ${response.body.toString()}")
                            val jsonResponse = JSONObject(response.body.toString())

                            smsBlockerDatabase.userToken =
                                jsonResponse.getJSONObject("data").getJSONObject("success")
                                    .getString("token")
                        } catch (e: Exception) {

                            SmartLog.e("onResponse ${getStackTrace(e)}")
                        }
                    }
                })
        }
    }
}