package com.call_blocke.a_repository.socket

import com.call_blocke.a_repository.Const.socketIp
import com.call_blocke.a_repository.Const.socketUrl
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import java.io.PrintWriter
import java.io.StringWriter

@DelicateCoroutinesApi
class SocketBuilder private constructor(
    private val userToken: String,
    private val uuid: String,
    var ip: String
) : WebSocketListener() {

    val messageCollector = MutableSharedFlow<String?>()

    val statusConnect = MutableStateFlow(false)

    private var connector: WebSocket? = null

    private var isOn = false

    fun connect() {
        SmartLog.d("onConnect $ip")
        isOn = true
        val url = Request.Builder()
            .url("${String.format(socketUrl, ip)}?token=$userToken&unique_id=$uuid")
            .build()
        if (!statusConnect.value)
            connector = OkHttpClient().newWebSocket(url, this@SocketBuilder)

    }

    fun disconnect() {
        SmartLog.d("onDisconnect")
        isOn = false
        connector?.close(1000, null)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        statusConnect.value = true
        SmartLog.d("onOpen")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        SmartLog.d("onMessage $text")
        messageCollector.tryEmit(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        SmartLog.d("onClosed")
        statusConnect.value = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        SmartLog.d("onFailure connection $sw")
        statusConnect.value = false
        if (isOn) {
            try {
                disconnect()
            } catch (e: Exception) {
                SmartLog.e(e)
            }
            try {
                connect()
            } catch (e: Exception) {
                SmartLog.e(e)
            }
        }
    }

    fun sendMessage(data: String) {
        try {
            connector?.send(data)
        } catch (e: Exception) {
            SmartLog.e("onSend error $e")
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