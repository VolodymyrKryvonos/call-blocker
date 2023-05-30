package com.call_blocker.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

object SimUtil {
    @SuppressLint("MissingPermission")
    fun getSIMInfo(context: Context?): List<SubscriptionInfo>? {
        return SubscriptionManager.from(context).activeSubscriptionInfoList
    }

    fun isSimAllow(context: Context, simSlot: Int): Boolean {
        return if (simSlot == 0) {
            isFirstSimAllow(context)
        } else {
            isSecondSimAllow(context)
        }
    }

    fun isFirstSimAllow(context: Context): Boolean {
        return getSIMInfo(context)?.any { it.simSlotIndex == 0 } == true
    }

    fun isSecondSimAllow(context: Context): Boolean {
        return getSIMInfo(context)?.any { it.simSlotIndex == 1 } == true
    }

    fun firstSim(context: Context?): SubscriptionInfo? {
        val simInfo = getSIMInfo(context)
        return simInfo?.firstOrNull { it.simSlotIndex == 0 }
    }

    fun secondSim(context: Context?): SubscriptionInfo? {
        val simInfo = getSIMInfo(context)
        return simInfo?.firstOrNull { it.simSlotIndex == 1 }
    }

    fun simInfo(context: Context?, simSlot: Int): SubscriptionInfo? {
        return if (simSlot == 0) {
            firstSim(context)
        } else {
            secondSim(context)
        }
    }

    fun simSlotById(context: Context?, simId: String): Int {
        val simInfo = getSIMInfo(context)
        return simInfo?.firstOrNull { it.iccId.contains(simId) }?.simSlotIndex ?: -1
    }

    @SuppressLint("MissingPermission")
    fun getImei(context: Context): String {
        val telephonyManager =
            (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).createForSubscriptionId(
                0
            )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }
    }
}