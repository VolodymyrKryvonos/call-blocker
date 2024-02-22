package com.call_blocker.app.managers

import android.content.Context
import android.os.Build
import com.call_blocker.common.CountryCodeExtractor
import com.call_blocker.common.SignalStrengthListener
import com.call_blocker.common.SignalStrengthListenerApi31
import com.call_blocker.common.SignalStrengthListenerPreS
import com.call_blocker.common.SimUtil
import com.call_blocker.common.WifiSignalStrengthListener
import com.call_blocker.common.WifiSignalStrengthListenerApi31
import com.call_blocker.rest_work_imp.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class SignalStrengthManager(
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : CoroutineScope {

    private val listeners = Array<SignalStrengthListener?>(3) {
        null
    }

    @OptIn(FlowPreview::class)
    fun listenSignalStrength() {
        val telephonyManagers = listOfNotNull(
            SimUtil.getTelephonyManager(context, 0),
            SimUtil.getTelephonyManager(context, 1)
        )

        telephonyManagers.forEachIndexed { index, telephonyManager ->
            val signalStrengthHolder: SignalStrengthListener =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SignalStrengthListenerApi31(telephonyManager, context)
                } else {
                    SignalStrengthListenerPreS(telephonyManager)
                }
            listeners[index] = signalStrengthHolder
        }

        listeners[2] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WifiSignalStrengthListenerApi31(context)
        } else {
            WifiSignalStrengthListener(context)
        }

        listeners.forEach {
            launch { it?.register() }
        }
        launch {
            combine(listeners.filterNotNull().map { it.signalStrength }) { it }
                .debounce(60.seconds)
                .collectLatest {
                    settingsRepository.sendSignalStrengthInfo(
                        firstSimSignal = listeners[0]?.signalStrength?.value ?: Int.MAX_VALUE,
                        secondSimSignal = listeners[1]?.signalStrength?.value ?: Int.MAX_VALUE,
                        wifiSignal = listeners[2]?.signalStrength?.value ?: Int.MAX_VALUE,
                        firstSim = SimUtil.firstSim(context),
                        secondSim = SimUtil.secondSim(context),
                        countryCode = CountryCodeExtractor.getCountryCode(context)
                    )

                }
        }
    }

    fun stopListeningSignalStrength() {
        coroutineContext.cancelChildren()
        listeners.forEach {
            it?.unregister()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO
}