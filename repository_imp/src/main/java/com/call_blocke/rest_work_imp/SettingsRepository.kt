package com.call_blocke.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import com.call_blocke.db.SmsBlockerDatabase

abstract class SettingsRepository {

    val currentSmsContFirstSimSlot: Int
        get() = SmsBlockerDatabase.smsPerDaySimFirst

    val currentSmsContSecondSimSlot: Int
        get() = SmsBlockerDatabase.smsPerDaySimSecond

    suspend fun setSmsPerDay(forFirstSim: Int, forSecondSim: Int) {
        SmsBlockerDatabase.smsPerDaySimFirst  = forFirstSim
        SmsBlockerDatabase.smsPerDaySimSecond = forSecondSim

        try {
            updateSmsPerDay(RepositoryBuilder.mContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun reloadBlackList(context: Context) {
        val list = try {
            blackPhoneNumberList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        list.forEach {
            try {
                val values = ContentValues()
                values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, it)
                val uri = context.contentResolver.insert(
                    BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                    values
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    protected abstract suspend fun updateSmsPerDay(context: Context)

    protected abstract suspend fun blackPhoneNumberList(): List<String>

}