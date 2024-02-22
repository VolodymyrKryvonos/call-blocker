package com.call_blocker.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.TelephonyNetworkSpecifier
import android.os.Build
import android.telephony.*


@SuppressLint("MissingPermission")
fun getNetworkGeneration(context: Context): String {
    val connectivityManager: ConnectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return "-"
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return mapNetworkType(
                telephonyManager.createForSubscriptionId((capabilities.networkSpecifier as TelephonyNetworkSpecifier).subscriptionId)
                    .dataNetworkType
            )
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return "Wi-Fi"
        }
    }

    val info: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (info == null || !info.isConnected) return "-" // not connected

    if (info.type == ConnectivityManager.TYPE_WIFI) return "Wi-Fi"
    if (info.type == ConnectivityManager.TYPE_MOBILE) {
        mapNetworkType(info.subtype)
    }
    return "-"
}

private fun mapNetworkType(type: Int): String {
    return when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        else -> "-"
    }
}
