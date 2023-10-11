package com.call_blocker.app.ui.screens.sim_card_info_screen

import android.telephony.SubscriptionInfo
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.call_blocker.app.R
import com.call_blocker.app.ui.Them
import com.call_blocker.app.ui.backgroundError
import com.call_blocker.app.ui.buttonBackground
import com.call_blocker.app.ui.buttonShape
import com.call_blocker.app.ui.buttonTextColor
import com.call_blocker.app.ui.darkGrey
import com.call_blocker.app.ui.disabledButton
import com.call_blocker.app.ui.gray6
import com.call_blocker.app.ui.itemBackground
import com.call_blocker.app.ui.primary
import com.call_blocker.app.ui.roboto700
import com.call_blocker.app.ui.screens.home_screen.Container
import com.call_blocker.app.ui.simInfoCaptionStyle
import com.call_blocker.app.ui.simInfoDataStyle
import com.call_blocker.app.ui.tabTextColor
import com.call_blocker.app.ui.tintError
import com.call_blocker.app.ui.widgets.IconWithBackground
import com.call_blocker.app.ui.widgets.TextFieldWithErrorMsg

data class SimTab(
    val name: String,
    @DrawableRes
    val iconId: Int? = null,
    val simInfo: SimInfoState
)


@Composable
@Preview
fun PreviewSimCardInfoScreen() {
    Them {
        SimCardInfoScreen()
    }
}

@Composable
fun SimCardInfoScreen(
    state: SimCardInfoScreenState = SimCardInfoScreenState(),
    onEvent: (event: SimCardInfoEvents) -> Unit = {}
) {
    Log.e("SimCardInfoScreen", state.toString())
    val tabs = getTabs(state)
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        onEvent(SimCardInfoEvents.ReloadSimInfoEvent(context))
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Box(
            Modifier
                .background(itemBackground)
                .fillMaxWidth()
                .padding(vertical = 22.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.sim_card_info),
                style = MaterialTheme.typography.h2,
            )
        }
        if (tabs.isEmpty()) {
            NoSimDetected()
        } else {
            if (tabs.size > 1) {
                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[state.currentPage])
                            .height(4.dp)
                            .padding(horizontal = tabPositions[state.currentPage].width / 3)
                            .background(
                                color = primary,
                                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                            )
                    )
                }
                TabRow(
                    selectedTabIndex = state.currentPage,
                    contentColor = tabTextColor,
                    indicator = indicator,
                    backgroundColor = itemBackground
                ) {
                    tabs.forEach { (index, tab) ->
                        if (tab.iconId != null) {
                            LeadingIconTab(
                                text = { Text(tab.name) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = tab.iconId),
                                        contentDescription = ""
                                    )
                                },
                                selected = state.currentPage == index,
                                onClick = { onEvent(SimCardInfoEvents.SetCurrentPageEvent(index)) }
                            )
                        } else {
                            Tab(text = { Text(tab.name, style = MaterialTheme.typography.h5) },
                                selected = state.currentPage == index,
                                onClick = {
                                    Log.e("onClick", "$index")
                                    onEvent(SimCardInfoEvents.SetCurrentPageEvent(index))
                                }
                            )
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val simInfo = tabs[state.currentPage]?.simInfo
            if (simInfo != null)
                SimCardInfoTab(
                    simInfo,
                    onNewLimitsSet = { dayLimit, monthLimit ->
                        onEvent(
                            SimCardInfoEvents.SetNewLimitsEvent(
                                context,
                                simInfo.simSubInfo.simSlotIndex,
                                dayLimit,
                                monthLimit
                            )
                        )
                    },
                    onResetClicked = {
                        onEvent(
                            SimCardInfoEvents.ResetSimCardEvent(
                                simInfo.simSubInfo.simSlotIndex,
                                context
                            )
                        )
                    },
                    onVerifyClicked = {
                        onEvent(
                            SimCardInfoEvents.VerifySimCardEvent(
                                simInfo.simSubInfo.simSlotIndex,
                                context
                            )
                        )
                    })
        }
    }
}

@Composable
fun getTabs(state: SimCardInfoScreenState): Map<Int, SimTab> {
    return mutableMapOf<Int, SimTab>().apply {
        Log.e("getTabList", "$state")
        if (state.firstSimSubInfo != null)
            put(
                0,
                SimTab(
                    name = stringResource(id = R.string.simWithPlaceHolder, 1),
                    simInfo = SimInfoState(
                        delivered = state.deliveredFirstSim,
                        limit = state.firstSimDayLimit,
                        simSubInfo = state.firstSimSubInfo,
                        simVerificationState = state.firstSimVerificationState,
                        connectedOn = state.firstSimConnectedOn
                    )
                )
            )
        if (state.secondSimSubInfo != null) {
            put(
                1,
                SimTab(
                    name = stringResource(id = R.string.simWithPlaceHolder, 2),
                    simInfo = SimInfoState(
                        delivered = state.deliveredSecondSim,
                        limit = state.secondSimDayLimit,
                        simSubInfo = state.secondSimSubInfo,
                        simVerificationState = state.secondSimVerificationState,
                        connectedOn = state.secondSimConnectedOn
                    )
                )
            )
        }
    }
}

@Composable
private fun NoSimDetected() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.noSimDetected),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SimCardInfoTab(
    simInfo: SimInfoState,
    onResetClicked: () -> Unit,
    onVerifyClicked: () -> Unit,
    onNewLimitsSet: (Int, Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        if (simInfo.simVerificationState.isNeedVerification()) {
            SimCardNotVerified(simInfo.simSubInfo.simSlotIndex, onVerifyClicked)
            Spacer(modifier = Modifier.height(20.dp))
        }
        if (simInfo.delivered >= simInfo.limit) {
            SimCardOutOfSms(
                simInfo.simSubInfo.simSlotIndex,
                simInfo.delivered,
                simInfo.limit,
                onResetClicked
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        SimCardInfo(
            simInfo.simSubInfo,
            phoneNumber = simInfo.simVerificationState.phoneNumber,
            connectedOn = simInfo.connectedOn
        )
        Spacer(modifier = Modifier.height(20.dp))
        SentSmsToday(simInfo.delivered, simInfo.limit)
        Spacer(modifier = Modifier.height(20.dp))
        Container {
            LimitFields(onNewLimitsSet)
        }
    }
}

@Composable
private fun SimCardNotVerified(simId: Int, onVerifyClicked: () -> Unit) {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconWithBackground(
                iconDrawable = R.drawable.ic_sim_card_alert,
                contentDescription = "Sim card",
                tint = tintError,
                background = backgroundError
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(
                Modifier
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    text = stringResource(id = R.string.simWithPlaceHolder, simId + 1),
                    style = MaterialTheme.typography.h5
                )
                Text(
                    text = stringResource(id = R.string.simCardNotVerified),
                    style = MaterialTheme.typography.body2,
                    color = darkGrey
                )
            }
            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = onVerifyClicked,
                colors = ButtonDefaults.buttonColors(backgroundColor = primary),
                shape = buttonShape
            ) {

                Text(
                    text = stringResource(id = R.string.verify),
                    style = MaterialTheme.typography.h5,
                    color = buttonTextColor,
                )
            }
        }
    }
}


@Composable
private fun SimCardOutOfSms(simId: Int, delivered: Int, limit: Int, onResetClicked: () -> Unit) {
    Container {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconWithBackground(
                iconDrawable = R.drawable.ic_sim_card_alert,
                contentDescription = "Sim card",
                tint = tintError,
                background = backgroundError
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.simWithPlaceHolder, simId + 1),
                    style = MaterialTheme.typography.h5,
                    maxLines = 1
                )
                Text(
                    text = stringResource(id = R.string.simLimitIsFull),
                    style = MaterialTheme.typography.body2,
                    color = darkGrey,
                    maxLines = 2
                )
            }
            Column(
                Modifier
                    .width(IntrinsicSize.Min)
                    .padding(horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$delivered",
                        style = MaterialTheme.typography.h2,
                        fontFamily = roboto700,
                        color = tintError
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "sms",
                        style = MaterialTheme.typography.body2,
                        color = tintError
                    )
                }
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .background(gray6)
                        .fillMaxWidth()
                )
                Text(text = "of $limit", style = MaterialTheme.typography.h5)
            }
            Button(
                onClick = onResetClicked,
                colors = ButtonDefaults.buttonColors(backgroundColor = primary),
                shape = buttonShape
            ) {
                Text(
                    text = stringResource(id = R.string.reset),
                    style = MaterialTheme.typography.h5,
                    color = buttonTextColor, maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SimCardInfo(simSubInfo: SubscriptionInfo, phoneNumber: String?, connectedOn: String) {
    Container {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row {
                SimInfoDataWithCaption(
                    stringResource(id = R.string.imsi),
                    phoneNumber ?: stringResource(
                        id = R.string.notDetermined
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                SimInfoDataWithCaption(
                    stringResource(id = R.string.operator),
                    simSubInfo.carrierName.toString()
                )
            }
            if (connectedOn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SimInfoDataWithCaption(stringResource(id = R.string.connectedOn), connectedOn)
            }
            Spacer(modifier = Modifier.height(10.dp))
            SimInfoDataWithCaption(stringResource(id = R.string.somCardId), simSubInfo.iccId)
        }
    }
}


@Composable
private fun SentSmsToday(sent: Int, limit: Int) {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconWithBackground(
                iconDrawable = R.drawable.ic_delivered_sms,
                contentDescription = "Sim card",
                tint = primary
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(
                Modifier
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    text = stringResource(id = R.string.sentSMSToday),
                    style = MaterialTheme.typography.h5
                )
                Text(
                    text = stringResource(id = R.string.limit_for_today),
                    style = MaterialTheme.typography.body2,
                    color = darkGrey
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                Modifier.width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$sent",
                        style = MaterialTheme.typography.h2,
                        fontFamily = roboto700,
                        color = primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "sms", style = MaterialTheme.typography.body2, color = primary)
                }
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .background(gray6)
                        .fillMaxWidth()
                )
                Text(text = "of $limit", style = MaterialTheme.typography.h5)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LimitFields(onNewLimitsSet: (Int, Int) -> Unit) {
    var dayLimit: Int by remember {
        mutableStateOf(0)
    }
    var isDayLimitError by remember {
        mutableStateOf(false)
    }
    var monthLimit: Int by remember {
        mutableStateOf(0)
    }
    var isMonthLimitError by remember {
        mutableStateOf(false)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
        ) {
            TextFieldWithErrorMsg(
                modifier = Modifier.weight(1f),
                hint = stringResource(id = R.string.sms_count_per_day),
                keyboardType = KeyboardType.Decimal,
                isError = isDayLimitError,
                value = if (dayLimit == 0) "" else dayLimit.toString(),
                onValueChange = {
                    dayLimit = run {
                        val limit = it.toIntOrNull()
                        isDayLimitError = limit == null || (monthLimit != 0 && limit > monthLimit)
                        if ((limit ?: 0) > 1000) 1000 else limit ?: 0
                    }
                },
                errorMsg = stringResource(id = R.string.invalidValue)
            )
            Spacer(modifier = Modifier.width(20.dp))
            TextFieldWithErrorMsg(
                modifier = Modifier.weight(1f),
                hint = stringResource(id = R.string.sms_count_per_month),
                keyboardType = KeyboardType.Decimal,
                isError = isMonthLimitError,
                value = if (monthLimit == 0) "" else monthLimit.toString(),
                onValueChange = {
                    monthLimit = run {
                        val limit = it.toIntOrNull()
                        isDayLimitError = limit != null && dayLimit > limit
                        isMonthLimitError = limit == null
                        if ((limit ?: 0) > 5000) 5000 else limit ?: 0
                    }
                },
                errorMsg = stringResource(id = R.string.invalidValue)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onNewLimitsSet(dayLimit, monthLimit) },
            shape = RoundedCornerShape(100f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = buttonBackground,
                disabledBackgroundColor = disabledButton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            enabled = !isDayLimitError && !isMonthLimitError
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = stringResource(id = R.string.apply),
                style = MaterialTheme.typography.h5,
                color = buttonTextColor
            )
        }
    }

}

@Composable
private fun SimInfoDataWithCaption(caption: String, data: String) {
    Column {
        Text(text = caption, style = simInfoCaptionStyle)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = data, style = simInfoDataStyle)
    }
}