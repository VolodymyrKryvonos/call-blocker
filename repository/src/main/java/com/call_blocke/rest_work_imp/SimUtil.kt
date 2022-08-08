package com.call_blocke.rest_work_imp

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager

/**
 * Created by Apipas on 6/4/15.
 */
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

    fun firstSimId(context: Context?): String? {
        val simInfo = getSIMInfo(context)
        return simInfo?.firstOrNull { it.simSlotIndex == 0 }?.iccId
    }

    fun secondSimId(context: Context?): String? {
        val simInfo = getSIMInfo(context)
        return simInfo?.firstOrNull { it.simSlotIndex == 1 }?.iccId
    }

    fun simInfo(context: Context?, simSlot: Int): SubscriptionInfo? {
        val simInfo = getSIMInfo(context)
        return if (simSlot == 0) {
            firstSim(context)
        } else {
            secondSim(context)
        }
    }
}