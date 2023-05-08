package com.call_blocker.ussd_sender

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.call_blocker.loger.SmartLog


class UssdResultReceiver : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        SmartLog.e("onAccessibilityEvent $event")
        sendBroadcast(Intent(ussdReceivedAction).apply { putExtra(ussd, event.text.toString()) })
//        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {
        SmartLog.e("onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.android.phone")
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    companion object {
        const val ussdReceivedAction = "com.call_blocker.USSD_RECEIVED"
        const val ussd = "USSD"
    }
}