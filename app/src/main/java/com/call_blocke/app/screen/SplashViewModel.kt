package com.call_blocke.app.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.call_blocke.app.MainActivity

class SplashViewModel : ViewModel() {

    val isPermissionGranted = MutableLiveData(false)

    fun openSMSAppChooser(context: Context) {

        if (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName)
            return

        val packageManager: PackageManager = context.packageManager
        val componentName = ComponentName(context, MainActivity::class.java)

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_APP_MESSAGING)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(selector)

        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP
        )
    }

}