package com.call_blocke.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract.BlockedNumbers
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocker.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class SettingsRepository {

    val currentSmsContFirstSimSlot: Int
        get() = SmsBlockerDatabase.smsPerDaySimFirst

    val currentSmsContSecondSimSlot: Int
        get() = SmsBlockerDatabase.smsPerDaySimSecond

    suspend fun setSmsPerDay(forFirstSim: Int, forSecondSim: Int) {
        try {
            updateSmsPerDay(RepositoryBuilder.mContext,forFirstSim, forSecondSim)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract suspend fun updateSmsPerDay(context: Context, sim1Limit: Int, sim2Limit: Int)

    protected abstract suspend fun blackPhoneNumberList(): List<String>

    abstract suspend fun refreshDataForSim(simSlot: Int, iccid: String, number: String = "")
    abstract suspend fun validateSimCard(phoneNumber: String, simID: String, monthlyLimit: Int): Flow<Resource<Unit>>

    abstract suspend fun simInfo(): List<FullSimInfoModel>

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

    abstract suspend fun getProfile(): Flow<Resource<com.call_blocker.model.Profile>>
    abstract suspend fun checkConnection(): Resource<ConnectionStatus>
    abstract suspend fun notifyServerUserStopService(): Flow<Resource<Unit>>
    abstract suspend fun checkSimCard(iccId: String, stateHolder: MutableStateFlow<SimValidationInfo>)
}