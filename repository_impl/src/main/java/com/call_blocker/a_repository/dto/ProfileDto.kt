package com.call_blocker.a_repository.dto


import com.call_blocker.model.Profile
import com.squareup.moshi.Json
import java.util.regex.Matcher
import java.util.regex.Pattern

data class ProfileDto(
    @Json(name = "delay_is_connected")
    val delayIsConnected: Int,
    @Json(name = "delay_sms_send")
    val delaySmsSend: Int,
    @Json(name = "delay_signal_strength")
    val delaySignalStrength: Int,
    @Json(name = "is_connected")
    val isConnected: Boolean,
    @Json(name = "is_keep_alive")
    val isKeepAlive: Boolean,
    @Json(name = "delay_is_keep_alive")
    val keepAliveDelay: Int,
    @Json(name = "protocol_min")
    val protocolMin: String,
    @Json(name = "socket_ip")
    val socketIp: String,
    @Json(name = "socket_port")
    val socketPort: String,
    val url: String,
    @Json(name = "latest_app_ver")
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
            delaySignalStrength,
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