package com.call_blocker.a_repository.repository

import com.call_blocker.a_repository.model.ReplyBody
import com.call_blocker.a_repository.rest.ReplyRest
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.ReplyRepository

class ReplyRepositoryImpl(private val replyRest: ReplyRest) : ReplyRepository {

    override suspend fun storeReply(
        phone: String,
        msg: String,
        receivedDate: Long,
        simIccid: String?,
        simSlot: Int?
    ) {
        try {
            replyRest.sendReply(ReplyBody(msg, phone, receivedDate, simIccid, simSlot))
        } catch (e: Exception) {
            SmartLog.e("Store reply: ${getStackTrace(e)}")
        }
    }
}