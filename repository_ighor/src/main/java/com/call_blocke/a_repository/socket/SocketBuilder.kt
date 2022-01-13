package com.call_blocke.a_repository.socket

import android.os.Handler
import android.os.Looper
import com.call_blocke.a_repository.Const.socketIp
import com.call_blocke.a_repository.Const.socketUrl
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import kotlin.coroutines.CoroutineContext

class SocketBuilder private constructor(
    private val userToken: String,
    private val uuid: String,
    var ip: String
) : WebSocketListener(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    val messageCollector = MutableSharedFlow<String?>(0)

    val statusConnect = MutableStateFlow(false)

    private var connector: WebSocket? = null

    private var isOn = false

    private var pingJob: Job? = null

    fun connect() {
        SmartLog.d("onConnect $ip")
        isOn = true
        val url = Request.Builder()
            .url("${String.format(socketUrl, ip)}?token=$userToken&unique_id=$uuid")
            .build()
        if (!statusConnect.value) {
            connector = OkHttpClient().newWebSocket(url, this@SocketBuilder)
        }
    }

    fun disconnect() {
        SmartLog.d("onDisconnect Socket")
        isOn = false
        if (connector?.close(1000, "disconnect") == true) {
            SmartLog.d("Closed successful")
        } else {
            SmartLog.d("Closed previously or connector is null")
        }
        connector = null
    }

    fun reconnect() {
        SmartLog.e("Reconnect $ip")
        disconnect()
        Handler(Looper.getMainLooper()).postDelayed({ connect() }, 11000)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        statusConnect.value = true
        SmartLog.d("onOpen")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        SmartLog.d("onClosing $reason code = $code")
        if (reason != "disconnect") {
            reconnect()
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        SmartLog.d("onMessage $text")

        launch(Dispatchers.IO) {
            messageCollector.emit(text)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        SmartLog.d("onClosed")
        statusConnect.value = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        SmartLog.d("onFailure connection ${getStackTrace(t)}")
        statusConnect.value = false
        if (isOn) {
            Handler(Looper.getMainLooper()).postDelayed({
                reconnect()
            }, 60 * 1000)

        }
    }

    fun sendMessage(data: String): Boolean {
        return try {
            SmartLog.e("sendMessage $data")
            connector?.send(data) == true
        } catch (e: Exception) {
            SmartLog.e("onSend error $e")
            false
        }
    }

    class Builder private constructor() {

        private var userToken: String? = null
        private var uuid: String? = null
        private var ip: String? = null

        companion object {
            private val data = Builder()

            fun setUserToken(d: String): Builder {
                data.userToken = d
                return data
            }
        }

        fun setIP(ip: String): Builder {
            data.ip = ip
            return data
        }

        fun setUUid(d: String): Builder {
            data.uuid = d
            return data
        }

        fun build() = SocketBuilder(
            userToken = userToken!!,
            uuid = uuid!!,
            ip = ip ?: socketIp
        )

    }

}