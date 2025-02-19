package com.call_blocker.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

val PERMISSIONS_REQUIRED = arrayListOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_MMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.RECEIVE_WAP_PUSH,
    Manifest.permission.SEND_SMS,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.CALL_PHONE
).apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        add(Manifest.permission.READ_PHONE_NUMBERS)
        add(Manifest.permission.ANSWER_PHONE_CALLS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

class SplashViewModel : ViewModel() {

    val isPermissionGranted = MutableStateFlow<Boolean>(false)
    val isAppDefault = MutableStateFlow<Boolean>(false)

    fun initMe(context: Context) {

        isPermissionGranted.tryEmit(PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        })
        isAppDefault.tryEmit(
            Telephony.Sms.getDefaultSmsPackage(context).also {
                Log.e(
                    "getDefaultSmsPackage",
                    it ?: "null"
                )
            } == context.packageName || Telephony.Sms.getDefaultSmsPackage(
                context
            ) == null
        )
    }

}