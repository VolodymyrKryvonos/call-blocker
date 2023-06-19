package com.call_blocker.a_repository.model

data class ReplyBody(
    val message: String,
    val msisdn: String,
    val received_date: Long,
    val simId: String?,
    val simSlot: Int?
)