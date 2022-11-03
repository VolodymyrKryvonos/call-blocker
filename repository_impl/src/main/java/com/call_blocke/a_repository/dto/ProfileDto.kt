package com.call_blocke.a_repository.dto


import com.call_blocker.model.Profile
import com.google.gson.annotations.SerializedName

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
    @SerializedName("url")
    val url: String
) {
    fun toProfile() = Profile(
            delayIsConnected, delaySmsSend, isConnected, isKeepAlive, keepAliveDelay, protocolMin, socketIp, socketPort, url
        )
}