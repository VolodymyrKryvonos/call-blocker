package com.call_blocker.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract.BlockedNumbers
import com.call_blocker.common.Resource
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.model.ConnectionStatus
import com.call_blocker.model.Profile
import kotlinx.coroutines.flow.Flow

abstract class SettingsRepository {

    suspend fun setSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int = SmsBlockerDatabase.smsPerMonthSimFirst,
        smsPerMonthSimSecond: Int = SmsBlockerDatabase.smsPerMonthSimSecond
    ) {
        try {
            updateSmsPerDay(
                context,
                smsPerDaySimFirst,
                smsPerDaySimSecond,
                smsPerMonthSimFirst,
                smsPerMonthSimSecond
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract suspend fun updateSmsPerDay(
        context: Context,
        smsPerDaySimFirst: Int,
        smsPerDaySimSecond: Int,
        smsPerMonthSimFirst: Int,
        smsPerMonthSimSecond: Int
    )

    abstract suspend fun refreshDataForSim(simSlot: Int, context: Context)
    abstract suspend fun simInfo(context: Context): List<FullSimInfoModel>

    fun blackList(context: Context): List<String> {
        val c: Cursor =
            context.contentResolver.query(
                BlockedNumbers.CONTENT_URI, arrayOf(
                    BlockedNumbers.COLUMN_ORIGINAL_NUMBER
                ), null, null, null
            ) ?: return emptyList()

        val data = arrayListOf<String>()

        while (c.moveToNext()) {
            data.add(c.getString(0))
        }
        c.close()
        return data
    }

    fun removeFromBlackList(context: Context, phoneNumber: String) {
        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, phoneNumber)
        val uri: Uri = context.contentResolver.insert(BlockedNumbers.CONTENT_URI, values)!!
        context.contentResolver.delete(uri, null, null)
    }

    abstract suspend fun getProfile(): Flow<Resource<Profile>>
    abstract suspend fun checkConnection(
        context: Context
    ): Resource<ConnectionStatus>

    abstract suspend fun notifyServerUserStopService(): Flow<Resource<Unit>>
    abstract suspend fun sendSignalStrengthInfo(
        context: Context
    )

    abstract suspend fun changeSimCard(context: Context)

}