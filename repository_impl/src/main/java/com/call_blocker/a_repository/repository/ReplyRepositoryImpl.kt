package com.call_blocker.a_repository.repository

import com.call_blocker.a_repository.model.ReplyBody
import com.call_blocker.a_repository.rest.ReplyRest
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.ReplyRepository

class ReplyRepositoryImpl : ReplyRepository {

    private val replyRest: ReplyRest
        get() = AppRest(
            Const.url,
            SmsBlockerDatabase.userToken ?: "",
            ReplyRest::class.java
        ).build()

    override suspend fun storeReply(phone: String, msg: String, receivedDate: Long) {
        try {
            replyRest.sendReply(ReplyBody(msg, phone, receivedDate))
        } catch (e: Exception) {
            SmartLog.e("Store reply: ${getStackTrace(e)}")
        }
    }
}