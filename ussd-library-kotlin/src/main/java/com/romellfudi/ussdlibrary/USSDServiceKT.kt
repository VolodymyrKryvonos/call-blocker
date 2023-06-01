/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */
package com.romellfudi.ussdlibrary

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.call_blocker.loger.SmartLog
import com.romellfudi.ussdlibrary.USSDController.context
import com.romellfudi.ussdlibrary.USSDController.map
import timber.log.Timber
import java.util.Locale

/**
 * AccessibilityService object for ussd dialogs on Android mobile Telcoms
 *
 * @author Romell Dominguez
 * @version 1.1.c 27/09/2018
 * @since 1.0.a
 */
open class USSDServiceKT : AccessibilityService() {
    /**
     * Catch widget by Accessibility, when is showing at mobile display
     *
     * @param event AccessibilityEvent
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Companion.event = event
        val ussd = USSDController
        SmartLog.e("${event.text}")
        SmartLog.e("ussd dialog class:${event.className}")
        SmartLog.e("${ussd.isRunning}")
        val response = event.text.toString()
        if (LoginView(event) && notInputText(event)) {
            SmartLog.e("LoginView(event) && notInputText(event)")
            // first view or logView, do nothing, pass / FIRST MESSAGE
            clickOnButton(event, 0)
            ussd.stopRunning()
            USSDController.callbackInvoke.over(response)
        } else if (problemView(event) || LoginView(event)) {
            SmartLog.e("problemView(event) || LoginView(event)")
            // deal down
            clickOnButton(event, 1)
            USSDController.callbackInvoke.over(response)
        } else if (isUSSDWidget(event)) {
            SmartLog.e("isUSSDWidget(event)")
            Timber.d("catch a USSD widget/Window")
            if (notInputText(event)) {
                SmartLog.e("notInputText(event)")
                // not more input panels / LAST MESSAGE
                // sent 'OK' button
                Timber.d("No inputText found & closing USSD process")
                clickOnButton(event, 0)
                ussd.stopRunning()
                USSDController.callbackInvoke.over(response)
            } else {
                // sent option 1
                if (ussd.sendType==true) {
                    SmartLog.e("ussd.sendType")
                    ussd.callbackMessage?.invoke(response)
                } else {
                    SmartLog.e("else")
                    USSDController.callbackInvoke.responseInvoke(
                        response
                    )
                }
            }
        }
    }

    /**
     * The AccessibilityEvent is instance of USSD Widget class
     *
     * @param event AccessibilityEvent
     * @return boolean AccessibilityEvent is USSD
     */
    private fun isUSSDWidget(event: AccessibilityEvent): Boolean {
        return listOf(
            "amigo.app.AmigoAlertDialog",
            "android.app.AlertDialog",
            "android.app.ProgressDialog",
            "com.android.phone.oppo.settings.LocalAlertDialog",
            "com.zte.mifavor.widget.AlertDialog",
            "color.support.v7.app.AlertDialog",
            "com.transsion.widgetslib.dialog.PromptDialog",
            "miuix.appcompat.app.AlertDialog",
            "com.android.phone.MMIDialogActivity",
            "androidx.appcompat.app.e",
            "com.samsung.telephony.phone.mmi.SamsungMmiDialogActivity"
        ).contains(event.className)
    }

    /**
     * The View has a login message into USSD Widget
     *
     * @param event AccessibilityEvent
     * @return boolean USSD Widget has login message
     */
    private fun LoginView(event: AccessibilityEvent): Boolean {
        return (isUSSDWidget(event)
                && map[USSDController.KEY_LOGIN]?.contains(event.text[0].toString()) == true)
    }

    /**
     * The View has a problem message into USSD Widget
     *
     * @param event AccessibilityEvent
     * @return boolean USSD Widget has problem message
     */
    private fun problemView(event: AccessibilityEvent): Boolean {
        return (isUSSDWidget(event)
                && map[USSDController.KEY_ERROR]
            ?.contains(event.text[0].toString()) == true)
    }

    /**
     * Active when SO interrupt the application
     */
    override fun onInterrupt() {
        Timber.d("onInterrupt")
    }

    /**
     * Configure accessibility server from Android Operative System
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("onServiceConnected")
    }

    companion object {
        private var event: AccessibilityEvent? = null

        /**
         * Send whatever you want via USSD
         *
         * @param text any string
         */
        fun send(text: String?) {
            Timber.d("trying to send... %s", text)
            setTextIntoField(event, text)
            clickOnButton(event, 1)
        }

        /**
         * Dismiss dialog by using first button from USSD Dialog
         */
        fun cancel(): Boolean {
            Timber.d("Trying to close/cancel USSD process by clicked in first button ")
            return clickOnButton(event, 0)
        }

        /**
         * set text into input text at USSD widget
         *
         * @param event AccessibilityEvent
         * @param data  Any String
         */
        private fun setTextIntoField(event: AccessibilityEvent?, data: String?) {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                data
            )
            for (leaf in getLeaves(event)) {
                if (leaf?.className == "android.widget.EditText" && !leaf.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        arguments
                    )
                ) {
                    val clipboardManager =
                        context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", data))
                    leaf.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                }
            }
        }

        /**
         * Method evaluate if USSD widget has input text
         *
         * @param event AccessibilityEvent
         * @return boolean has or not input text
         */
        protected fun notInputText(event: AccessibilityEvent?): Boolean {
            for (leaf in getLeaves(event)) if (leaf?.className == "android.widget.EditText") return false
            return true
        }

        /**
         * click a button using the index
         *
         * @param event AccessibilityEvent
         * @param index button's index
         */
        protected fun clickOnButton(event: AccessibilityEvent?, index: Int): Boolean {
            var count = -1
            for (leaf in getLeaves(event)) {
                if (leaf?.className.toString().lowercase(Locale.getDefault()).contains("button")) {
                    count++
                    if (count == index) {
                        return leaf?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                    }
                }
            }
            return false
        }

        private fun getLeaves(event: AccessibilityEvent?): List<AccessibilityNodeInfo?> {
            val leaves: MutableList<AccessibilityNodeInfo?> = ArrayList()
            if (event?.source != null) {
                getLeaves(leaves, event.source)
            }
            leaves.forEach {
                SmartLog.e("Leaf className ${it?.className} ")
                SmartLog.e("Leaf $it")
            }
            return leaves
        }

        private fun getLeaves(
            leaves: MutableList<AccessibilityNodeInfo?>,
            node: AccessibilityNodeInfo?
        ) {
            if (node?.childCount == 0) {
                leaves.add(node)
                return
            }
            for (i in 0 until (node?.childCount?:0)) {
                getLeaves(leaves, node?.getChild(i))
            }
        }
    }
}