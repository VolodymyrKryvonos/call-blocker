package com.call_blocke.rest_work_imp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apipas on 6/4/15.
 */
public class SimUtil {

    @SuppressLint("MissingPermission")
    public static List<SubscriptionInfo> getSIMInfo(Context context) {
        List<SubscriptionInfo> simInfoList = new ArrayList<>();

        SubscriptionManager sManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        SubscriptionInfo infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0);
        SubscriptionInfo infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1);

        if (infoSim1 != null)
            simInfoList.add(infoSim1);

        if (infoSim2 != null)
            simInfoList.add(infoSim2);

        return simInfoList;
    }

}