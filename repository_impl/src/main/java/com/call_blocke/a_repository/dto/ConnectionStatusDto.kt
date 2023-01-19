package com.call_blocke.a_repository.dto

import com.squareup.moshi.Json


data class ConnectionStatusDto(
    @Json(name = "status")
    val status: Boolean
){
    fun toConnectionStatus()= com.call_blocker.model.ConnectionStatus(status = status)
}