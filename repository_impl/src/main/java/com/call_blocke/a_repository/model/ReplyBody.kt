package com.call_blocke.a_repository.model

import com.call_blocke.db.SmsBlockerDatabase

data class ReplyBody(
    val message: String,
    val msisdn: String,
    val received_date: Long,
    val unique_id: String = SmsBlockerDatabase.deviceID
)