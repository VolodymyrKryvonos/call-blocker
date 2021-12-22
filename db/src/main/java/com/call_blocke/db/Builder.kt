package com.call_blocke.db

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.call_blocke.db.entity.ReplayTaskDao
import com.call_blocke.db.entity.SystemDetailEntity
import com.call_blocke.db.entity.TaskDao

object SmsBlockerDatabase {
    private var preference: Preference? = null

    private var database: AppDatabase? = null

    val userIsAuthLiveData = MutableLiveData(false)

    val onSimChanged = MutableLiveData(false)

    var isSimChanged: Boolean
        get() {
            return (preference ?: throw Exception("please init db module")).isSimChanged
        }
        set(value) {
            preference?.isSimChanged = value
            onSimChanged.postValue(value)
        }

    var userToken: String?
        get() {
            return (preference ?: throw Exception("please init db module")).userToken
        }
        set(value) {
            preference?.userToken = value
            userIsAuthLiveData.postValue(value != null)
        }

    var userPassword: String
        get() {
            val tok = (preference ?: throw Exception("please init db module")).userPassword
            return tok ?: "unknow"
        }
        set(value) {
            preference?.userPassword = value
        }

    var userName: String
        get() {
            val tok = (preference ?: throw Exception("please init db module")).userName
            return tok ?: "unknow"
        }
        set(value) {
            preference?.userName = value
        }

    val deviceID: String
        get() = (preference ?: throw Exception("please init db module")).deviceID

    var smsPerDaySimFirst: Int
        get() = (preference ?: throw Exception("please init db module")).smsPerDaySimFirst
        set(value) {
            preference?.smsPerDaySimFirst = value
        }

    var smsPerDaySimSecond: Int
        get() = (preference ?: throw Exception("please init db module")).smsPerDaySimSecond
        set(value) {
            preference?.smsPerDaySimSecond = value
        }

    val taskDao: TaskDao
        get() = (database ?: throw Exception("please init db module")).taskDao()

    val replayDao: ReplayTaskDao
        get() = (database ?: throw Exception("please init db module")).replayTaskDao()

    var lastSimSlotUsed: Int
        get() = (preference ?: throw Exception("please init db module")).lastSimSlotUsed
        set(value) {
            preference?.lastSimSlotUsed = value
        }

    var systemDetail: SystemDetailEntity
        get() = (preference ?: throw Exception("please init db module")).systemDetail
        set(value) {
           preference?.systemDetail = value
        }

    @SuppressLint("HardwareIds")
    fun init(context: Context) {
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
    }
}