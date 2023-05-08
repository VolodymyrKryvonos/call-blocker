package com.call_blocker.rest_work_imp

interface ReplyRepository {
    suspend fun storeReply(phone: String, msg: String, receivedDate: Long)
}