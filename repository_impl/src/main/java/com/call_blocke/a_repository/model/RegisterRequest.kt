package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("unique_id")
    override val uniqueId: String,
    override val email: String,
    override val password: String,
    @SerializedName("name_of_package")
    val nameOfPackage: String,
    @SerializedName("version_of_package")
    val versionOfPackage: String,
    @SerializedName("os_name")
    val osName: String = "Android",
    @SerializedName("device_model")
    val deviceModel: String,
    @SerializedName("device_brand")
    val deviceBrand: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("display_height")
    val displayHeight: Int = 0,
    @SerializedName("display_width")
    val displayWidth: Int = 0,
    val campaign: String,
    @SerializedName("connection_type")
    val connectionType: String,
    @SerializedName("sms_per_day")
    val smsPerDay: Int = 0,
    val msisdn: String,
    @SerializedName("firebase_token")
    val firebase_token: String = ""
): AuthRequest
