package com.call_blocker.app.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.call_blocker.ussd_sender.postDelayedKt

fun wakeScreen(context: Context) {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    if (!pm!!.isInteractive) {
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            ":UssdLock"
        )

        // Acquire the wake lock to turn on the screen
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)

        // Release the wake lock after a short delay to allow time for the screen to turn on
        Handler(Looper.getMainLooper()).postDelayedKt(
            1000
        ) { wakeLock?.release() }
    }
}