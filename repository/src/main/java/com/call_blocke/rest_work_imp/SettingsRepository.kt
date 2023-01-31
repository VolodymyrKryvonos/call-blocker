package com.call_blocke.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract.BlockedNumbers
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.VerificationState
import com.call_blocke.rest_work_imp.model.SimVerificationInfo
import com.call_blocke.rest_work_imp.model.SimVerificationStatus
import com.call_blocker.model.ConnectionStatus
import com.call_blocker.model.Profile
import com.example.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

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

    protected abstract suspend fun blackPhoneNumberList(): List<String>

    abstract suspend fun refreshDataForSim(simSlot: Int, iccid: String, number: String = "")
    abstract suspend fun validateSimCard(
        phoneNumber: String,
        simID: String,
        simSlot: Int
    ): Flow<Resource<Unit>>

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

    abstract suspend fun getProfile(): Flow<Resource<Profile>>
    abstract suspend fun checkConnection(): Resource<ConnectionStatus>
    abstract suspend fun notifyServerUserStopService(): Flow<Resource<Unit>>
    abstract suspend fun checkSimCard(
        iccId: String,
        simSlot: Int,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean = false
    ): Flow<SimVerificationInfo>

    abstract suspend fun confirmVerification(
        iccid: String,
        simSlot: String,
        verificationCode: String,
        phoneNumber: String,
        uniqueId: String
    ): Flow<Resource<Unit>>

    abstract suspend fun sendSignalStrengthInfo()

    suspend fun checkSim(
        simId: String,
        simSlot: Int,
        simVerificationInfo: MutableStateFlow<SimVerificationInfo>,
        simVerificationState: MutableStateFlow<VerificationState>,
        phoneNumber: String?,
        createAutoVerificationSms: Boolean = false
    ) {
        checkSimCard(
            simId,
            simSlot,
            phoneNumber,
            createAutoVerificationSms,
        )
            .collectLatest {
                simVerificationInfo.emit(it)
                if (it.status == SimVerificationStatus.INVALID && simVerificationState.value != VerificationState.FAILED) {
                    simVerificationState.emit(VerificationState.INVALID)
                    return@collectLatest
                }
                if (it.isAutoVerificationAvailable) {
                    simVerificationState.emit(VerificationState.AUTO_VERIFICATION)
                    return@collectLatest
                }
                simVerificationState.emit(VerificationState.SUCCESS)
            }
    }
}