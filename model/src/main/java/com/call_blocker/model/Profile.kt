package com.call_blocker.model

data class Profile(
    val delayIsConnected: Int = 3,
    val delaySmsSend: Int = 5,
    val delaySignalStrength: Int = 10,
    val isConnected: Boolean = false,
    val isKeepAlive: Boolean = false,
    val keepAliveDelay: Int = 900,
    val protocolMin: String = "1.0",
    val socketIp: String = "",
    val socketPort: String = "",
    val url: String = "",
    val latestMajorVersion: Int = 0,
    val latestMinorVersion: Int = 0,
    val latestPatchVersion: Int = 0,
)