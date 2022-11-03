package com.call_blocker.model

data class Profile(
    val delayIsConnected: Int = 3,
    val delaySmsSend: Int = 900,
    val isConnected: Boolean = false,
    val isKeepAlive: Boolean = false,
    val keepAliveDelay: Int = 900,
    val protocolMin: String = "1.0",
    val socketIp: String = "",
    val socketPort: String = "",
    val url: String = ""
)