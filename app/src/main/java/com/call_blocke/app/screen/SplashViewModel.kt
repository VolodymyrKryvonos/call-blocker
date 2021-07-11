package com.call_blocke.app.screen

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.call_blocke.app.MainActivity

val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_MMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.RECEIVE_WAP_PUSH,
    Manifest.permission.SEND_SMS,
    Manifest.permission.READ_PHONE_STATE
)

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