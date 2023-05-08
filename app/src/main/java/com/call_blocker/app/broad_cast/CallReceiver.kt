package com.call_blocker.app.broad_cast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.internal.telephony.ITelephony
import com.call_blocker.app.worker_manager.SendingSMSWorker
import java.lang.reflect.Method


class CallReceiver : BroadcastReceiver() {

    @SuppressLint("DiscouragedPrivateApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (!SendingSMSWorker.isRunning.value) {
            return
        }
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (telecomManagerEndCall(telecomManager)) {
                    Log.e("CallEnded", "Success")
                }
            } else {
                val telephonyService: ITelephony
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val m: Method =
                    tm.javaClass.getDeclaredMethod("getITelephony")
                m.isAccessible = true
                telephonyService = m.invoke(tm) as ITelephony
                if (telephonyService.endCall()) {
                    Log.e("CallEnded", "Success")
                }
            }
        } catch (e: Exception) {

        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun telecomManagerEndCall(telecomManager: TelecomManager): Boolean {
        return telecomManager.endCall()
    }
}
