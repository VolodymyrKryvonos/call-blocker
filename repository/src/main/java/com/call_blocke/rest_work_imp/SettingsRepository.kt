package com.call_blocke.rest_work_imp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BlockedNumberContract.BlockedNumbers
import android.telephony.SubscriptionInfo
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.ValidationState
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.call_blocker.model.ConnectionStatus
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
        monthlyLimit: Int,
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

    abstract suspend fun getProfile(): Flow<Resource<com.call_blocker.model.Profile>>
    abstract suspend fun checkConnection(): Resource<ConnectionStatus>
    abstract suspend fun notifyServerUserStopService(): Flow<Resource<Unit>>
    abstract suspend fun checkSimCard(
        iccId: String,
        simSlot: Int,
        createAutoVerificationSms: Boolean = false
    ): Flow<SimValidationInfo>

    abstract suspend fun confirmValidation(
        iccid: String,
        simSlot: String,
        verificationCode: String,
        phoneNumber: String,
        uniqueId: String
    ): Flow<Resource<Unit>>

    abstract suspend fun sendSignalStrengthInfo()

    suspend fun checkSim(
        simInfo: SubscriptionInfo?,
        simValidationInfo: MutableStateFlow<SimValidationInfo>,
        simValidationState: MutableStateFlow<ValidationState>,
        createAutoVerificationSms: Boolean = false
    ) {
        if (simInfo != null) {
            checkSimCard(
                simInfo.iccId ?: "",
                simInfo.simSlotIndex,
                createAutoVerificationSms
            )
                .collectLatest {
                    simValidationInfo.emit(it)
                    if (it.status == SimValidationStatus.INVALID && simValidationState.value != ValidationState.FAILED) {
                        simValidationState.emit(ValidationState.INVALID)
                        return@collectLatest
                    }
                    if (it.status == SimValidationStatus.AUTO_VALIDATION) {
                        simValidationState.emit(ValidationState.AUTO_VALIDATION)
                        return@collectLatest
                    }
                    simValidationState.emit(ValidationState.SUCCESS)
                }
        }
    }
}