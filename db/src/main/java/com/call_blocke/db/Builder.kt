package com.call_blocke.db

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.call_blocke.db.entity.*

object SmsBlockerDatabase {
    private var preference: Preference? = null

    private var database: AppDatabase? = null

    val userIsAuthLiveData = MutableLiveData(false)

    val onSimChanged = MutableLiveData(false)

    var isInitialized: Boolean = false

    val isSimChanged: Boolean
        get() {
            return firstSimChanged || secondSimChanged
        }

    var userToken: String?
        get() {
            return (preference ?: throw DbModuleException("please init db module")).userToken
        }
        set(value) {
            preference?.userToken = value
            userIsAuthLiveData.postValue(value != null)
        }

    var userPassword: String
        get() {
            val tok = (preference ?: throw DbModuleException("please init db module")).userPassword
            return tok ?: "unknow"
        }
        set(value) {
            preference?.userPassword = value
        }

    var userName: String
        get() {
            val tok = (preference ?: throw DbModuleException("please init db module")).userName
            return tok ?: "unknow"
        }
        set(value) {
            preference?.userName = value
        }

    val deviceID: String
        get() = (preference ?: throw DbModuleException("please init db module")).deviceID

    var smsTodaySentFirstSim: Int
        get() = (preference
            ?: throw DbModuleException("please init db module")).smsTodaySentFirstSim
        set(value) {
            preference?.smsTodaySentFirstSim = value
        }

    var smsTodaySentSecondSim: Int
        get() = (preference
            ?: throw DbModuleException("please init db module")).smsTodaySentSecondSim
        set(value) {
            preference?.smsTodaySentSecondSim = value
        }

    var smsPerDaySimFirst: Int
        get() = (preference ?: throw DbModuleException("please init db module")).smsPerDaySimFirst
        set(value) {
            preference?.smsPerDaySimFirst = value
        }

    var smsPerDaySimSecond: Int
        get() = (preference ?: throw DbModuleException("please init db module")).smsPerDaySimSecond
        set(value) {
            preference?.smsPerDaySimSecond = value
        }

    var lastRefreshTime: Long
        get() = (preference ?: throw DbModuleException("please init db module")).lastRefreshTime
        set(value) {
            preference?.lastRefreshTime = value
        }

    val taskDao: TaskDao
        get() = (database ?: throw DbModuleException("please init db module")).taskDao()

    val replayDao: ReplayTaskDao
        get() = (database ?: throw DbModuleException("please init db module")).replayTaskDao()

    val phoneNumberDao: PhoneNumberDao
        get() = (database ?: throw DbModuleException("please init db module")).phoneNumberDao()

    val taskStatusDao: TaskStatusDao
        get() = (database ?: throw DbModuleException("please init db module")).taskStatusDao()

    var lastSimSlotUsed: Int
        get() = (preference ?: throw DbModuleException("please init db module")).lastSimSlotUsed
        set(value) {
            preference?.lastSimSlotUsed = value
        }

    var systemDetail: SystemDetailEntity
        get() = (preference ?: throw DbModuleException("please init db module")).systemDetail
        set(value) {
            preference?.systemDetail = value
        }

    var firstSimId: String?
        get() = (preference ?: throw DbModuleException("please init db module")).firstSimId
        set(value) {
            preference?.firstSimId = value
        }

    var secondSimId: String?
        get() = (preference ?: throw DbModuleException("please init db module")).secondSimId
        set(value) {
            preference?.secondSimId = value
        }

    var firstSimChanged: Boolean
        get() = (preference ?: throw DbModuleException("please init db module")).firstSimChanged
        set(value) {
            preference?.firstSimChanged = value
        }

    var secondSimChanged: Boolean
        get() = (preference ?: throw DbModuleException("please init db module")).secondSimChanged
        set(value) {
            preference?.secondSimChanged = value
        }


    @SuppressLint("HardwareIds")
    fun init(context: Context) {
        if (isInitialized)
            return
        preference = Preference(context)

        preference?.deviceID = fun(): String {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }.invoke()

        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "call_blocker_db"
        ).build()

        userIsAuthLiveData.postValue(userToken != null)
        isInitialized = true
    }

    class DbModuleException(override val message: String?) : Exception()
}