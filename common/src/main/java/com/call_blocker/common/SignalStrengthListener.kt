package com.call_blocker.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.call_blocker.loger.SmartLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds


interface SignalStrengthHolder {
    val signalStrength: StateFlow<Int>

    fun updateSignalStrength(newSignalStrength: CellSignalStrength?)
    fun updateSignalStrength(dbm: Int)

}

interface SignalStrengthListener : SignalStrengthHolder {
    suspend fun register()

    fun unregister()
}

class BaseSignalStrengthHolder : SignalStrengthHolder {

    private val _signalStrength = MutableStateFlow(-1000)
    override val signalStrength = _signalStrength.asStateFlow()
    override fun updateSignalStrength(newSignalStrength: CellSignalStrength?) {
        newSignalStrength ?: return

        try {
            val dbm = newSignalStrength.dbm
            if (kotlin.math.abs(signalStrength.value - dbm) > 10) {
                SmartLog.e("updateSignalStrength $dbm")
                _signalStrength.value = dbm
            }
        } catch (e: Exception) {
            SmartLog.e("UpdateSignalStrength error", e)
        }
    }

    override fun updateSignalStrength(dbm: Int) {
        if (kotlin.math.abs(signalStrength.value - dbm) > 10) {
            SmartLog.e("updateSignalStrength $dbm")
            _signalStrength.value = dbm
        }
    }
}


class SignalStrengthListenerPreS(private val telephonyManager: TelephonyManager) :
    SignalStrengthHolder by BaseSignalStrengthHolder(), SignalStrengthListener {
    private var isRegistered = false
    override suspend fun register() {
        isRegistered = true

        while (isRegistered) {
            updateSignalStrength(getSignalStrength())
            delay(60.seconds)
        }
    }

    override fun unregister() {
        isRegistered = false
    }

    @SuppressLint("MissingPermission")
    fun getSignalStrength(): CellSignalStrength? {
        var signal: CellSignalStrength? = null
        val cellInfos = telephonyManager.allCellInfo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return telephonyManager.signalStrength?.cellSignalStrengths?.firstOrNull()
        }
        if (cellInfos != null) {
            for (i in cellInfos.indices) {
                if (cellInfos[i].isRegistered) {
                    if (cellInfos[i] is CellInfoWcdma) {
                        val cellInfoWcdma = cellInfos[i] as CellInfoWcdma
                        val cellSignalStrengthWcdma = cellInfoWcdma.cellSignalStrength
                        signal = cellSignalStrengthWcdma
                    } else if (cellInfos[i] is CellInfoGsm) {
                        val cellInfoGsm = cellInfos[i] as CellInfoGsm
                        val cellSignalStrengthGsm = cellInfoGsm.cellSignalStrength
                        signal = cellSignalStrengthGsm
                    } else if (cellInfos[i] is CellInfoLte) {
                        val cellInfoLte = cellInfos[i] as CellInfoLte
                        val cellSignalStrengthLte = cellInfoLte.cellSignalStrength
                        signal = cellSignalStrengthLte
                    } else if (cellInfos[i] is CellInfoCdma) {
                        val cellInfoCdma = cellInfos[i] as CellInfoCdma
                        val cellSignalStrengthCdma = cellInfoCdma.cellSignalStrength
                        signal = cellSignalStrengthCdma
                    }
                }
                if (signal != null) {
                    return signal
                }
            }
        }
        return null
    }
}


@RequiresApi(Build.VERSION_CODES.S)
class SignalStrengthListenerApi31(
    private val telephonyManager: TelephonyManager,
    private val context: Context
) : TelephonyCallback(),
    TelephonyCallback.SignalStrengthsListener,
    SignalStrengthHolder by BaseSignalStrengthHolder(),
    SignalStrengthListener {
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        updateSignalStrength(signalStrength.cellSignalStrengths.firstOrNull())
    }

    override suspend fun register() {
        telephonyManager.registerTelephonyCallback(
            context.mainExecutor,
            this
        )
    }

    override fun unregister() {
        telephonyManager.unregisterTelephonyCallback(this)
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
class WifiSignalStrengthListenerApi31(context: Context) :
    SignalStrengthHolder by BaseSignalStrengthHolder(), NetworkCallback(), SignalStrengthListener {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val wifiInfo = networkCapabilities.transportInfo as WifiInfo
        updateSignalStrength(wifiInfo.rssi)
    }

    @SuppressLint("MissingPermission")
    override suspend fun register() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, this)
    }

    override fun unregister() {
        connectivityManager.unregisterNetworkCallback(this)
    }
}

class WifiSignalStrengthListener(context: Context) :
    SignalStrengthHolder by BaseSignalStrengthHolder(), SignalStrengthListener {
    private var isRegistered = false
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override suspend fun register() {
        while (isRegistered) {
            val wifiInfo = wifiManager.connectionInfo;
            val rssi = wifiInfo.rssi
            updateSignalStrength(rssi)
            delay(60.seconds)
        }
    }

    override fun unregister() {
        isRegistered = false
    }
}