package com.call_blocke.a_repository.dto


import com.google.gson.annotations.SerializedName

data class ConnectionStatusDto(
    @SerializedName("status")
    val status: Boolean
){
    fun toConnectionStatus()= com.call_blocker.model.ConnectionStatus(status = status)
}