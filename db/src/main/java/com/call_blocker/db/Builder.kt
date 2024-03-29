package com.call_blocker.db

import android.content.Context
import android.provider.Settings
import androidx.room.Room
import com.call_blocker.db.entity.PhoneNumberDao
import com.call_blocker.db.entity.ReplayTaskDao
import com.call_blocker.db.entity.SystemDetailEntity
import com.call_blocker.db.entity.TaskDao
import com.call_blocker.db.entity.TaskStatusDao
import com.call_blocker.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SmsBlockerDatabase(context: Context) {

    private var preference: Preference? = null

    private var database: AppDatabase? = null

    val userIsAuthLiveData = MutableStateFlow(false)

    val onSimChanged = MutableStateFlow(false)

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
            userIsAuthLiveData.tryEmit(value != null)
        }

    var profile: Profile?
        get() {
            return (preference ?: throw DbModuleException("please init db module")).profile
        }
        set(value) {
            preference?.profile = value
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

    var smsPerMonthSimFirst: Int
        get() = (preference ?: throw DbModuleException("please init db module")).smsPerMonthSimFirst
        set(value) {
            preference?.smsPerMonthSimFirst = value
        }

    var smsPerMonthSimSecond: Int
        get() = (preference
            ?: throw DbModuleException("please init db module")).smsPerMonthSimSecond
        set(value) {
            preference?.smsPerMonthSimSecond = value
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

    private val _ussdCommandState = MutableStateFlow(isUssdCommandOn)
    val ussdCommandState = _ussdCommandState.asStateFlow()
    var isUssdCommandOn: Boolean
        get() = preference?.isUssdCommandOn ?: false
        set(value) {
            preference?.isUssdCommandOn = value
            _ussdCommandState.update { value }
        }


    val isSimChange: Boolean
        get() = secondSimChanged || firstSimChanged


    var email: String?
        get() = (preference ?: throw DbModuleException("please init db module")).email
        set(value) {
            preference?.email = value
        }

    var password: String?
        get() = (preference ?: throw DbModuleException("please init db module")).password
        set(value) {
            preference?.password = value
        }

    init {
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

        userIsAuthLiveData.tryEmit(userToken != null)
    }

    class DbModuleException(override val message: String?) : Exception()
}