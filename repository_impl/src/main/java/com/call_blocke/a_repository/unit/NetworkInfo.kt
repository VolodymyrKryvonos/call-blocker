package com.call_blocke.a_repository.unit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import com.call_blocke.rest_work_imp.RepositoryBuilder.mContext

object NetworkInfo {

    fun connectionType(): String {
        val info = networkInfo()
        if (!isConnected()) return "-" //not connected

        if (isConnectedWifi()) return "Wi-Fi"

        if (isConnectedMobile()) {
            return when (info.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> "No data"
            }
        }

        return "No data"
    }

    private fun isConnected(): Boolean {
        val info: NetworkInfo = networkInfo()
        return info != null && info.isConnected
    }

    private fun networkInfo(): NetworkInfo {
        val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo!!
    }

    private fun isConnectedWifi(): Boolean {
        val info: NetworkInfo = networkInfo()
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    private fun isConnectedMobile(): Boolean {
        val info: NetworkInfo = networkInfo()
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

}