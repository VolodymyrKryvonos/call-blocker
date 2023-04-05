package com.example.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.*
import androidx.annotation.RequiresPermission
import kotlin.random.Random


object ConnectionManager {

    private var connectionManager: ConnectivityManager? = null
    private var telephonyManager: TelephonyManager? = null
    private var wifiManager: WifiManager? = null

    fun init(context: Context) {
        connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    fun getSignalStrength(): Int {
        var strength: Int = -60 + Random.nextInt(-15, 15)
        val cellInfos = telephonyManager?.allCellInfo
        if (getNetworkGeneration() == "Wi-Fi") {
            strength = getWifiSignalStrength()
        } else
            if (cellInfos != null) {
                for (i in cellInfos.indices) {
                    if (cellInfos[i].isRegistered) {
                        if (cellInfos[i] is CellInfoWcdma) {
                            val cellInfoWcdma = cellInfos[i] as CellInfoWcdma
                            val cellSignalStrengthWcdma = cellInfoWcdma.cellSignalStrength
                            strength = cellSignalStrengthWcdma.dbm
                        } else if (cellInfos[i] is CellInfoGsm) {
                            val cellInfoGsm = cellInfos[i] as CellInfoGsm
                            val cellSignalStrengthGsm = cellInfoGsm.cellSignalStrength
                            strength = cellSignalStrengthGsm.dbm
                        } else if (cellInfos[i] is CellInfoLte) {
                            val cellInfoLte = cellInfos[i] as CellInfoLte
                            val cellSignalStrengthLte = cellInfoLte.cellSignalStrength
                            strength = cellSignalStrengthLte.dbm
                        } else if (cellInfos[i] is CellInfoCdma) {
                            val cellInfoCdma = cellInfos[i] as CellInfoCdma
                            val cellSignalStrengthCdma = cellInfoCdma.cellSignalStrength
                            strength = cellSignalStrengthCdma.dbm
                        }
                    }
                }
            }
        return strength
    }

    private fun getWifiSignalStrength(): Int {
        return wifiManager?.connectionInfo?.rssi ?: 0
    }

    fun getNetworkGeneration(): String {
        if (connectionManager == null)
            return "?"
        val info: NetworkInfo? = connectionManager!!.activeNetworkInfo
        if (info == null || !info.isConnected) return "-" // not connected

        if (info.type == ConnectivityManager.TYPE_WIFI) return "Wi-Fi"
        if (info.type == ConnectivityManager.TYPE_MOBILE) {
            return when (info.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "No data"
            }
        }
        return "No data"
    }
}