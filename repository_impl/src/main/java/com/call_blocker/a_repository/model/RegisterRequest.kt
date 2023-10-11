package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class RegisterRequest(
    override val email: String,
    override val password: String,
    @Json(name = "name_of_package")
    val nameOfPackage: String,
    @Json(name = "version_of_package")
    val versionOfPackage: String,
    @Json(name = "os_name")
    val osName: String = "Android",
    @Json(name = "device_model")
    val deviceModel: String,
    @Json(name = "device_brand")
    val deviceBrand: String,
    @Json(name = "device_type")
    val deviceType: String,
    @Json(name = "display_height")
    val displayHeight: Int = 0,
    @Json(name = "display_width")
    val displayWidth: Int = 0,
    val campaign: String,
    @Json(name = "connection_type")
    val connectionType: String,
    @Json(name = "sms_per_day")
    val smsPerDay: Int = 0,
    val msisdn: String,
    @Json(name = "firebase_token")
    val firebase_token: String = "",
    @Json(name = "whats_app")
    val whatsApp: String = "",
): AuthRequest
