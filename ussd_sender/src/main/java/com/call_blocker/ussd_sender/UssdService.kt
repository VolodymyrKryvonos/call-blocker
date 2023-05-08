package com.call_blocker.ussd_sender

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.UssdResponseCallback
import android.view.accessibility.AccessibilityManager
import com.call_blocker.common.SimUtil
import com.call_blocker.loger.SmartLog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UssdService {
    private var receiver: BroadcastReceiver? = null
    fun sendUssdCommand(
        command: String,
        simSlot: Int = 0,
        context: Context,
        onReceiveResult: (String) -> Unit
    ) {
        apiBelow26SendUssdCommand(command, simSlot, context, onReceiveResult)
    }

    @SuppressLint("MissingPermission")
    @TargetApi(26)
    private suspend fun api26SendUssdCommand(command: String, simSlot: Int = 0, context: Context) =
        suspendCoroutine { cont ->
            val manager =
                (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).createForSubscriptionId(
                    SimUtil.simInfo(context, simSlot)?.subscriptionId ?: 0
                )
            val handler = Handler(Looper.getMainLooper())
            if (manager == null) {
                cont.resume("ERROR")
                return@suspendCoroutine
            }
            manager.sendUssdRequest(command, object : UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager,
                    request: String,
                    response: CharSequence
                ) {
                    super.onReceiveUssdResponse(telephonyManager, request, response)
                    SmartLog.e("onReceiveUssdResponse $request $response ")
                    cont.resume(response.toString())
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode)
                    SmartLog.e("onReceiveUssdResponseFailed $request $failureCode")
                    cont.resume("ERROR $failureCode")
                }
            }, handler)
        }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "MissingPermission")
    private fun apiBelow26SendUssdCommand(
        command: String,
        simSlot: Int = 0,
        context: Context,
        onReceiveResult: (String) -> Unit
    ) {
        context.startService(Intent(context, UssdResultReceiver::class.java))
        val intent =
            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", command, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val extras = Bundle()
        arrayOf(
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx",
            "simSlot"
        ).forEach { extras.putInt(it, simSlot) }
        extras.putBoolean(
            TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE,
            false
        )
        extras.putBoolean("com.android.phone.force.slot", true)
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val phoneAccountHandleList: List<PhoneAccountHandle> =
            telecomManager.callCapablePhoneAccounts
        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandleList[simSlot])
        intent.putExtras(extras)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        if (receiver != null) {
            context.unregisterReceiver(receiver)
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                onReceiveResult(intent.getStringExtra(UssdResultReceiver.ussd) ?: "")
            }
        }
        context.registerReceiver(receiver, IntentFilter(UssdResultReceiver.ussdReceivedAction))
    }

    companion object {
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
                        UssdResultReceiver::class.java.name
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

                UssdService().sendUssdCommand("*101#", context = context) {}
            }
        }
    }
}