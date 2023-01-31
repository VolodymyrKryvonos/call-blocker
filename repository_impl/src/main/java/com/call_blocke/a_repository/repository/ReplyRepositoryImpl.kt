package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.model.ReplyBody
import com.call_blocke.a_repository.rest.ReplyRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.ReplyRepository
import com.call_blocker.common.rest.AppRest
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace

class ReplyRepositoryImpl : ReplyRepository {

    private val replyRest: ReplyRest
        get() = AppRest(
            bearerToken = SmsBlockerDatabase.userToken ?: "",
            service = ReplyRest::class.java
        ).build()

    override suspend fun storeReply(phone: String, msg: String, receivedDate: Long) {
        try {
            replyRest.sendReply(ReplyBody(msg, phone, receivedDate))
        } catch (e: Exception) {
            SmartLog.e("Store reply: ${getStackTrace(e)}")
        }
    }
}