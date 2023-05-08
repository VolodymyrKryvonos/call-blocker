package com.call_blocker.rest_work_imp

import kotlinx.coroutines.flow.Flow


interface UssdRepository {

    fun storeUssdResponse(
        ussdCommand: String,
        result: String,
        simId: String,
        countryCode: String
    ): Flow<Result<Unit>>

}