package com.call_blocke.app.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
} else {
    arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}

class SplashViewModel : ViewModel() {

    val isPermissionGranted = MutableLiveData<Boolean>()
    val isAppDefault = MutableLiveData<Boolean>()

    fun initMe(context: Context) {
        isPermissionGranted.postValue(PERMISSIONS_REQUIRED.all { ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED })
        isAppDefault.postValue(Telephony.Sms.getDefaultSmsPackage(context) == context.packageName)
    }

}