package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.model.UssdResult
import com.call_blocke.a_repository.rest.UssdRest
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.UssdRepository
import com.call_blocker.common.rest.AppRest
import com.call_blocker.common.rest.Const
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UssdRepositoryImpl(
    private val userRest: UssdRest = AppRest(
        Const.url, SmsBlockerDatabase.userToken ?: "", UssdRest::class.java
    ).build()
) : UssdRepository {
    override fun storeUssdResponse(
        ussdCommand: String, result: String, simId: String, countryCode: String
    ): Flow<Result<Unit>> = flow {
        try {
            SmartLog.e("storeUssdResponse $result")
            userRest.storeUssdResult(
                UssdResult(
                    countryCode = countryCode,
                    simId = simId,
                    result = result,
                    ussdCommand = ussdCommand
                )
            )
            emit(Result.success(Unit))
        } catch (e: Exception) {
            SmartLog.e("Failed store ussd ${getStackTrace(e)}")
            emit(Result.failure(e))
        }
    }
}