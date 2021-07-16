package com.call_blocke.db

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.call_blocke.db.entity.SystemDetailEntity
import com.google.gson.Gson

class Preference(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "sms_send_app",
        Context.MODE_PRIVATE
    )

    var userToken: String?
        get() = sharedPreferences.getString("user_token", null)
        set(value) {
            with (sharedPreferences.edit()) {
                putString("user_token", value)
                commit()
            }
        }

    var deviceID: String
        get() = sharedPreferences.getString("device_id", "no") ?: "no"
        set(value) {
            with (sharedPreferences.edit()) {
                putString("device_id", value)
                commit()
            }
        }

    var smsPerDaySimFirst: Int
        get() = sharedPreferences.getInt("smsPerDaySimFirst", 0)
        set(value) {
            with (sharedPreferences.edit()) {
                putInt("smsPerDaySimFirst", value)
                commit()
            }
        }

    var smsPerDaySimSecond: Int
        get() = sharedPreferences.getInt("smsPerDaySimSecond", 0)
        set(value) {
            with (sharedPreferences.edit()) {
                putInt("smsPerDaySimSecond", value)
                commit()
            }
        }

    var lastSimSlotUsed: Int
        get() = sharedPreferences.getInt("last_sim_slot_used", 0)
        set(value) {
            with (sharedPreferences.edit()) {
                putInt("last_sim_slot_used", value)
                commit()
            }
        }

    var systemDetail: SystemDetailEntity
        get() {
            if (sharedPreferences.contains("system_detail"))
                return try {
                    Gson().fromJson(
                        sharedPreferences.getString("system_detail", ""),
                        SystemDetailEntity::class.java
                    )
                } catch (e: Exception) {
                    SystemDetailEntity()
                }
            return SystemDetailEntity()
        }
        set(value) {
            with (sharedPreferences.edit()) {
                putString("system_detail", Gson().toJson(value))
                commit()
            }
        }

}