package com.call_blocke.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract.BlockedNumbers
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

    protected abstract suspend fun updateSmsPerDay(context: Context)

    protected abstract suspend fun blackPhoneNumberList(): List<String>

    abstract suspend fun refreshDataForSim(simSlot: Int)

    abstract suspend fun simInfo(): List<FullSimInfoModel>

    fun blackList(context: Context): List<String> {
        val c: Cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.contentResolver.query(
                BlockedNumbers.CONTENT_URI, arrayOf(
                    BlockedNumbers.COLUMN_ORIGINAL_NUMBER
                ), null, null, null
            ) ?: return emptyList()
        } else {
            return emptyList()
        }

        val data = arrayListOf<String>()

        while (c.moveToNext()) {
            data.add(c.getString(0))
        }

        return data
    }

    fun removeFromBlackList(context: Context, phoneNumber: String) {
        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, phoneNumber)
        val uri: Uri = context.contentResolver.insert(BlockedNumbers.CONTENT_URI, values)!!
        context.contentResolver.delete(uri, null, null)
    }

}