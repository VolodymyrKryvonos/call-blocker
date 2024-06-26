package com.call_blocker.a_repository.socket

import android.os.Handler
import android.os.Looper
import com.call_blocker.common.rest.Const
import com.call_blocker.common.rest.Const.domain
import com.call_blocker.common.rest.Const.socketUrl
import com.call_blocker.common.rest.Pinger
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class SocketBuilder private constructor(
    private val userToken: String,
    private val uuid: String,
    private var ip: String,
    private var port: Int,
    private val smsBlockerDatabase: SmsBlockerDatabase
) : WebSocketListener(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val reconnectHandler = Handler(Looper.getMainLooper())

    val messageCollector = Channel<String?>()

    val connectionStatusFlow = MutableStateFlow(false)

    val statusConnect = MutableStateFlow(false)

    private var connector: WebSocket? = null

    private var failureInRow = 0

    private var isOn = false

    fun connect() {
        SmartLog.d("onConnect $ip")
        isOn = true
        val url = Request.Builder()
            .url(
                "${
                    String.format(
                        socketUrl,
                        ip,
                        port
                    )
                }?token=$userToken&unique_id=$uuid"
            )
            .build()
        if (!statusConnect.value) {
            val connectorBuilder = OkHttpClient.Builder()
            connectorBuilder.pingInterval(
                smsBlockerDatabase.profile?.keepAliveDelay?.toLong() ?: 90L, TimeUnit.SECONDS
            )
            connector = connectorBuilder.build().newWebSocket(url, this@SocketBuilder)
        }
    }

    fun disconnect(reason: String = "disconnect") {
        SmartLog.d("onDisconnect Socket reason = $reason")
        if (reason == "disconnect") {
            reconnectHandler.removeCallbacksAndMessages(null)
            failureInRow = 0
            isOn = false
        }
        if (connector?.close(1000, reason) == true) {
            SmartLog.d("Closed successful")
        } else {
            SmartLog.d("Closed previously or connector is null")
        }
        connector = null
    }

    fun reconnect() {
        SmartLog.e("Reconnect $ip")
        disconnect("reconnect")
        reconnectHandler.postDelayed({
            if (isOn) {
                connect()
            }
        }, 5000)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        statusConnect.value = true
        failureInRow = 0
        SmartLog.d("onOpen")
        launch(Dispatchers.IO) {
            SmartLog.d("emit onOpen")
            connectionStatusFlow.emit(true)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        SmartLog.d("onClosing $reason code = $code")
        if (reason != "disconnect" && reason != "reconnect") {
            SmartLog.e("Reconnect from server")
            reconnect()
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        SmartLog.d("onMessage $text")
        launch(Dispatchers.IO) {
            messageCollector.send(text)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        SmartLog.d("onClosed $reason")
        statusConnect.value = false
        launch(Dispatchers.IO) { connectionStatusFlow.emit(false) }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        failureInRow++
        SmartLog.d("onFailure connection ${getStackTrace(t)}")
        statusConnect.value = false
        launch(Dispatchers.IO) { connectionStatusFlow.emit(false) }
        if (failureInRow >= 5) {
            runPinger()
            failureInRow = 0
        } else {
            Handler(Looper.getMainLooper()).postDelayed(
                { reconnect() }, 10000
            )
        }
    }

    private fun runPinger() {
        launch(Dispatchers.IO) {
            while (true) {
                delay(60000)
                if (Pinger.isHostReachable(ip, 40000)) {
                    reconnect()
                    return@launch
                }
            }
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

    class Builder {

        private var userToken: String? = null
        private var uuid: String? = null
        private var ip: String? = null
        private var port: Int = Const.port


        fun setUserToken(d: String): Builder {
            userToken = d
            return this
        }

        fun setIP(ip: String): Builder {
            this.ip = ip
            return this
        }

        fun setUUid(d: String): Builder {
            uuid = d
            return this
        }

        fun setPort(port: Int): Builder {
            this.port = port
            return this
        }

        fun build(smsBlockerDatabase: SmsBlockerDatabase): SocketBuilder {
            SmartLog.e("SocketBuilder build")
            return SocketBuilder(
                userToken = userToken!!,
                uuid = uuid!!,
                ip = domain,
                port = port,
                smsBlockerDatabase = smsBlockerDatabase
            )
        }

    }

}