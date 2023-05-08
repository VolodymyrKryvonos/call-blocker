package com.call_blocker.common.rest

import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import kotlin.system.measureTimeMillis

object Pinger {
    suspend fun isHostReachable(
        serverAddress: String?,
        timeoutMS: Int
    ): Boolean {
        var connected = false
        try {
            withContext(Dispatchers.IO) {
                repeat(4) {
                    val time = measureTimeMillis {
                        connected = InetAddress.getByName(serverAddress).isReachable(timeoutMS) || connected
                    }
                    if (time >= timeoutMS) {
                        SmartLog.e("Request timed out.")
                    } else {
                        SmartLog.e("Reply from $serverAddress time=${time}ms")
                    }
                }
            }
        } catch (e: IOException) {
            SmartLog.e(getStackTrace(e))
        }
        return connected
    }
}