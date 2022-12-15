package com.call_blocke.a_repository.dto


import com.call_blocker.model.Profile
import com.google.gson.annotations.SerializedName
import java.util.regex.Matcher
import java.util.regex.Pattern

data class ProfileDto(
    @SerializedName("delay_is_connected")
    val delayIsConnected: Int,
    @SerializedName("delay_sms_send")
    val delaySmsSend: Int,
    @SerializedName("is_connected")
    val isConnected: Boolean,
    @SerializedName("is_keep_alive")
    val isKeepAlive: Boolean,
    @SerializedName("delay_is_keep_alive")
    val keepAliveDelay: Int,
    @SerializedName("protocol_min")
    val protocolMin: String,
    @SerializedName("socket_ip")
    val socketIp: String,
    @SerializedName("socket_port")
    val socketPort: String,
    val url: String,
    @SerializedName("latest_app_ver")
    val latestAppVersion: String
) {
    fun toProfile(): Profile{
        val regex = "\\d+"
        val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
        val matcher: Matcher = pattern.matcher(latestAppVersion)
        matcher.find()
        val major = matcher.group().toInt()
        matcher.find()
        val minor = matcher.group().toInt()
        matcher.find()
        val patch = matcher.group().toInt()
        return Profile(
            delayIsConnected,
            delaySmsSend,
            isConnected,
            isKeepAlive,
            keepAliveDelay,
            protocolMin,
            socketIp,
            socketPort,
            url,
            major,
            minor,
            patch
        )
    }

}