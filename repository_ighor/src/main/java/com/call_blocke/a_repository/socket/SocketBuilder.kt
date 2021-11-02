package com.call_blocke.a_repository.socket

import android.util.Log
import com.call_blocke.a_repository.Const.socketUrl
import com.call_blocke.a_repository.model.ApiResponse
import com.call_blocke.a_repository.model.TaskResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import java.lang.reflect.Type

@DelicateCoroutinesApi
class SocketBuilder private constructor(private val userToken: String,
                                        private val uuid: String) : WebSocketListener() {

    private val url = Request.Builder()
        .url("$socketUrl?token=$userToken&unique_id=$uuid")
        .build()

    val onMessage = MutableSharedFlow<String>()

    private var connector: WebSocket? = null

    fun connect() {
        connector = OkHttpClient().newWebSocket(url, this@SocketBuilder)
    }
    fun disconnect() {
        connector?.close(1000, null)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d("SocketBuilder", "onMessage $text")

        GlobalScope.launch(Dispatchers.IO) {
            onMessage.emit(text)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d("SocketBuilder", "onClosed")
        connect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.d("SocketBuilder", "onFailure")
        connect()
    }

    class Builder private constructor() {

        private var userToken: String? = null
        private var uuid: String? = null

        companion object {
            private val data = Builder()

            fun setUserToken(d: String): Builder {
                data.userToken = d
                return data
            }
        }

        fun setUUid(d: String): Builder {
            data.uuid = d
            return data
        }

        fun build() = SocketBuilder(
            userToken = userToken!!,
            uuid      = uuid!!
        )
    }

}