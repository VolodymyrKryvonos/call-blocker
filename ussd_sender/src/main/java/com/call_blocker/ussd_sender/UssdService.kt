package com.call_blocker.ussd_sender

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.romellfudi.ussdlibrary.USSDController
import com.romellfudi.ussdlibrary.USSDServiceKT


object UssdService {
    private val map = hashMapOf(
        "KEY_LOGIN" to listOf("espere", "waiting", "loading", "esperando", "running"),
        "KEY_ERROR" to listOf("problema", "problem", "error", "null", "invalid")
    )

    private val handler = Handler(Looper.getMainLooper())
    var isSessionAlive = false
        private set

    fun startSession(
        command: String,
        simSlot: Int = 0,
        context: Context,
        onReceiveResult: (SessionResult) -> Unit
    ) {
        try {
            SmartLog.e("Start session")
            context.startService(Intent(context, USSDServiceKT::class.java))
            USSDController.callUSSDInvoke(context, command, simSlot, map,
                object : USSDController.CallbackInvoke {
                    override fun responseInvoke(message: String) {
                        isSessionAlive = true
                        startSessionCountdown(onReceiveResult)
                        onReceiveResult(SessionResult.Success(message))
                    }

                    override fun over(message: String) {
                        isSessionAlive = false
                        onReceiveResult(SessionResult.Error(message))
                    }
                })
        } catch (e: Exception) {
            SmartLog.e("UssdService startSession error: ${getStackTrace(e)}")
            onReceiveResult(SessionResult.Error("Error occurred, try again"))
        }
    }

    private fun startSessionCountdown(onReceiveResult: (SessionResult) -> Unit) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayedKt(60 * 1000) {
            onReceiveResult(SessionResult.Timeout)
            closeSession()
        }
    }

    fun selectMenu(
        menuItem: String,
        onReceiveResult: (SessionResult) -> Unit
    ) {
        SmartLog.e("selectMenu")
        USSDController.send(menuItem) {
            onReceiveResult(SessionResult.Success(it))
            isSessionAlive = true
            startSessionCountdown(onReceiveResult)
        }
    }

    fun closeSession() {
        SmartLog.e("CloseSession")
        try {
            USSDController.cancel()
        } catch (e: Exception) {
            SmartLog.e("UssdService closeSession error: ${getStackTrace(e)}")
        }
        isSessionAlive = false
        handler.removeCallbacksAndMessages(null)
    }

    fun hasAccessibilityPermission(
        context: Context
    ): Boolean {
        val am: AccessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices: List<AccessibilityServiceInfo> =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName.equals(context.packageName) && enabledServiceInfo.name.equals(
                    USSDServiceKT::class.java.name
                )
            ) {
                return true
            }
        }
        return false
    }

    fun enableAccessibilityPermission(context: Context) {
        if (!hasAccessibilityPermission(context)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        }
    }
}

sealed interface SessionResult {
    object Timeout : SessionResult
    class Success(val message: String) : SessionResult
    class Error(val message: String) : SessionResult
}


fun Handler.postDelayedKt(delayMillis: Long, runnable: Runnable) {
    postDelayed(runnable, delayMillis)
}