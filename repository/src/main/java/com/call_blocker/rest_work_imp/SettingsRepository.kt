package com.call_blocker.rest_work_imp

import android.content.Context
import com.call_blocker.common.Resource
import com.call_blocker.model.ConnectionStatus
import com.call_blocker.model.Profile
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int
    )

    suspend fun updateSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int
    )

    suspend fun refreshDataForSim(simSlot: Int, context: Context)
    suspend fun simInfo(context: Context): List<FullSimInfoModel>

    suspend fun getProfile(): Flow<Resource<Profile>>
    suspend fun checkConnection(
        context: Context
    ): Resource<ConnectionStatus>

    suspend fun notifyServerUserStopService(): Flow<Resource<Unit>>
    suspend fun sendSignalStrengthInfo(
        context: Context
    )

    suspend fun changeSimCard(context: Context)

}