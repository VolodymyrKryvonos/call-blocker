package com.call_blocke.db

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.room.Room
import com.call_blocke.db.entity.TaskDao

object SmsBlockerDatabase {
    private var preference: Preference? = null

    private var database: AppDatabase? = null

    var userToken: String?
        get() = (preference ?: throw Exception("please init db module")).userToken
        set(value) {
            preference?.userToken = value
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

    var lastSimSlotUsed: Int
        get() = (preference ?: throw Exception("please init db module")).lastSimSlotUsed
        set(value) {
            preference?.lastSimSlotUsed = value
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
    }
}