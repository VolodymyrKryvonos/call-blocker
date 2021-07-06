package com.call_blocke.rest_work_imp;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private static boolean isSimSupport(Context context, int slot) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return tm.getSimState(slot) == TelephonyManager.SIM_STATE_READY;
    }

}