package com.call_blocke.db

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.call_blocke.db.entity.SystemDetailEntity
import com.call_blocker.model.Profile
import com.google.gson.Gson

enum class AutoValidationResult {
    NONE, FAILED, SUCCESS
}

class Preference(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "sms_send_app",
        Context.MODE_PRIVATE
    )

    var userPassword: String?
        get() = sharedPreferences.getString("userPassword", null)
        set(value) {
            with(sharedPreferences.edit()) {
                putString("userPassword", value)
                commit()
            }
        }


    var profile: Profile?
        get() = try {
            Gson().fromJson(sharedPreferences.getString("Profile", ""), Profile::class.java)
        } catch (e: Exception) {
            null
        }
        set(value) {
            with(sharedPreferences.edit()) {
                putString("Profile", Gson().toJson(value))
                commit()
            }
        }

    var userName: String?
        get() = sharedPreferences.getString("userName", null)
        set(value) {
            with(sharedPreferences.edit()) {
                putString("userName", value)
                commit()
            }
        }

    var userToken: String?
        get() = sharedPreferences.getString("user_token", null)
        set(value) {
            with(sharedPreferences.edit()) {
                putString("user_token", value)
                commit()
            }
        }

    var isSimChanged: Boolean
        get() = sharedPreferences.getBoolean("isSimChanged", false)
        set(value) {
            with(sharedPreferences.edit()) {
                putBoolean("isSimChanged", value)
                commit()
            }
        }

    var ipType: String
        get() {
            val type = sharedPreferences.getString("ipType", "") ?: "Production"
            Log.e("Type", "Type$type")
            return if (type.isNotEmpty()) type else "Production"
        }
        set(value) {
            with(sharedPreferences.edit()) {
                putString("ipType", value)
                commit()
            }
        }

    var customIp: String
        get() = sharedPreferences.getString("customIp", "") ?: ""
        set(value) {
            with(sharedPreferences.edit()) {
                putString("customIp", value)
                commit()
            }
        }

    var deviceID: String
        get() = sharedPreferences.getString("device_id", "no") ?: "no"
        set(value) {
            with(sharedPreferences.edit()) {
                putString("device_id", value)
                commit()
            }
        }


    var lastRefreshTime: Long
        get() = sharedPreferences.getLong("lastRefreshTime", 0)
        set(value) {
            with(sharedPreferences.edit()) {
                putLong("lastRefreshTime", value)
                commit()
            }
        }

    var smsTodaySentFirstSim: Int
        get() = sharedPreferences.getInt("smsTodaySentFirstSim", 0)
        set(value) {
            with(sharedPreferences.edit()) {
                putInt("smsTodaySentFirstSim", value)
                commit()
            }
        }

    var smsTodaySentSecondSim: Int
        get() = sharedPreferences.getInt("smsTodaySentSecondSim", 0)
        set(value) {
            with(sharedPreferences.edit()) {
                putInt("smsTodaySentSecondSim", value)
                commit()
            }
        }

    var smsPerDaySimFirst: Int
        get() = sharedPreferences.getInt("smsPerDaySimFirst", 0)
        set(value) {
            with(sharedPreferences.edit()) {
                putInt("smsPerDaySimFirst", value)
                commit()
            }
        }

    var smsPerDaySimSecond: Int
        get() = sharedPreferences.getInt("smsPerDaySimSecond", 0)
        set(value) {
            with(sharedPreferences.edit()) {
                putInt("smsPerDaySimSecond", value)
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
            with(sharedPreferences.edit()) {
                putString("system_detail", Gson().toJson(value))
                commit()
            }
        }

    var firstSimId: String?
        get() = sharedPreferences.getString("firstSimId", "none") ?: "none"
        set(value) {
            with(sharedPreferences.edit()) {
                putString("firstSimId", value)
                commit()
            }
        }

    var secondSimId: String?
        get() = sharedPreferences.getString("secondSimId", "none") ?: "none"
        set(value) {
            with(sharedPreferences.edit()) {
                putString("secondSimId", value)
                commit()
            }
        }

    var firstSimChanged: Boolean
        get() = sharedPreferences.getBoolean("firstSimChanged", false)
        set(value) {
            with(sharedPreferences.edit()) {
                putBoolean("firstSimChanged", value)
                commit()
            }
        }

    var secondSimChanged: Boolean
        get() = sharedPreferences.getBoolean("secondSimChanged", false)
        set(value) {
            with(sharedPreferences.edit()) {
                putBoolean("secondSimChanged", value)
                commit()
            }
        }


    var firstSimSlotValidationNumber: String
        get() = sharedPreferences.getString("firstSimSlotValidationNumber", "") ?: ""
        set(value) {
            with(sharedPreferences.edit()) {
                putString("firstSimSlotValidationNumber", value)
                commit()
            }
        }

    var secondSimSlotValidationNumber: String
        get() = sharedPreferences.getString("secondSimSlotValidationNumber", "") ?: ""
        set(value) {
            with(sharedPreferences.edit()) {
                putString("secondSimSlotValidationNumber", value)
                commit()
            }
        }

    var simFirstAutoValidationResult: AutoValidationResult
        get() = try {
            AutoValidationResult.valueOf(
                sharedPreferences.getString(
                    "simFirstAutoValidationResult",
                    ""
                ) ?: ""
            )
        } catch (e: IllegalArgumentException) {
            AutoValidationResult.NONE
        }
        set(value) {
            with(sharedPreferences.edit()) {
                putString("simFirstAutoValidationResult", value.name)
                commit()
            }
        }

    var simSecondAutoValidationResult: AutoValidationResult
        get() = try {
            AutoValidationResult.valueOf(
                sharedPreferences.getString(
                    "simSecondAutoValidationResult",
                    ""
                ) ?: ""
            )
        } catch (e: IllegalArgumentException) {
            AutoValidationResult.NONE
        }
        set(value) {
            with(sharedPreferences.edit()) {
                putString("simSecondAutoValidationResult", value.name)
                commit()
            }
        }
}