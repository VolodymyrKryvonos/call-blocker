package com.call_blocke.rest_work_imp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi

/**
 * Created by Apipas on 6/4/15.
 */
object SimUtil {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    fun getSIMInfo(context: Context?): List<SubscriptionInfo> {
        return SubscriptionManager.from(context).activeSubscriptionInfoList
    }

    fun isFirstSimAllow(context: Context): Boolean {
        return getSIMInfo(context).any { it.simSlotIndex == 0 }
    }

    fun isSecondSimAllow(context: Context): Boolean {
        return getSIMInfo(context).any { it.simSlotIndex == 1 }
    }

    fun firstSimId(context: Context?): String? {
        val simInfo = getSIMInfo(context)
        simInfo.ifEmpty { return null }
        return simInfo[0].iccId
    }

    fun secondSimId(context: Context?): String? {
        val simInfo = getSIMInfo(context)
        if (simInfo.size > 1) {
            return simInfo[1].iccId
        }
        return null
    }
}