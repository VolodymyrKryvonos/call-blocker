package com.call_blocker.a_repository.model

import com.call_blocker.db.SmsBlockerDatabase

data class ReplyBody(
    val message: String,
    val msisdn: String,
    val received_date: Long,
    val unique_id: String = SmsBlockerDatabase.deviceID
)