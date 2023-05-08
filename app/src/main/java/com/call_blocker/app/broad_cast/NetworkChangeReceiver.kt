package com.call_blocker.app.broad_cast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.call_blocker.loger.SmartLog


class NetworkChangeReceiver : BroadcastReceiver() {

    private fun isOnline(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (isOnline(context)) {
            SmartLog.e("Connected to the internet")
        } else {
            SmartLog.e("Lost internet connection")
        }
    }
}