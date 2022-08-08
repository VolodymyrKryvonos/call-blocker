package com.call_blocke.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace


object ConnectionManager {

    private var connectionManager: ConnectivityManager? = null
    private var telephonyManager: TelephonyManager? = null
    var mSignalStrength = Int.MAX_VALUE

    fun innit(context: Context) {
        connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val mPhoneStateListener = MyPhoneStateListener()
        telephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    fun getSignalStrength(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (getNetworkGeneration() == "WIFI") {
                    connectionManager?.getNetworkCapabilities(connectionManager?.activeNetwork)?.signalStrength
                } else {
                    telephonyManager?.allCellInfo?.firstOrNull()?.cellSignalStrength?.dbm
                } ?: 0
            } else {
                mSignalStrength
            }
        } catch (e: Exception) {
            SmartLog.e(getStackTrace(e))
            mSignalStrength
        }

    }

    fun getNetworkGeneration(): String {
        val info: NetworkInfo? = connectionManager?.activeNetworkInfo
        if (info == null || !info.isConnected) return "-" // not connected

        if (info.type == ConnectivityManager.TYPE_WIFI) return "WIFI"
        if (info.type == ConnectivityManager.TYPE_MOBILE) {
            return when (info.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "?"
            }
        }
        return "?"
    }

    class MyPhoneStateListener : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            mSignalStrength = signalStrength.gsmSignalStrength
            mSignalStrength = mSignalStrength * 2 - 113
        }
    }
}