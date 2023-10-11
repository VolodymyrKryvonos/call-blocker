package com.call_blocker.a_repository.repository

import com.call_blocker.a_repository.model.UssdResult
import com.call_blocker.a_repository.rest.UssdRest
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.UssdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UssdRepositoryImpl(
    private val userRest: UssdRest
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