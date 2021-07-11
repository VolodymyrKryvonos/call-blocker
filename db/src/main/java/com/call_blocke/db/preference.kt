package com.call_blocke.db

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences

class Preference(context: Context) {

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "sms_blocker_preference",
        "possible",
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
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

}