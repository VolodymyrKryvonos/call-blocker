package com.call_blocke.app.new_ui.screens.sim_card_info_screen

import android.telephony.SubscriptionInfo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.call_blocke.app.R
import com.call_blocke.app.new_ui.backgroundError
import com.call_blocke.app.new_ui.buttonBackground
import com.call_blocke.app.new_ui.buttonShape
import com.call_blocke.app.new_ui.buttonTextColor
import com.call_blocke.app.new_ui.darkGrey
import com.call_blocke.app.new_ui.disabledButton
import com.call_blocke.app.new_ui.gray6
import com.call_blocke.app.new_ui.itemBackground
import com.call_blocke.app.new_ui.primary
import com.call_blocke.app.new_ui.roboto700
import com.call_blocke.app.new_ui.screens.home_screen.Container
import com.call_blocke.app.new_ui.simInfoCaptionStyle
import com.call_blocke.app.new_ui.simInfoDataStyle
import com.call_blocke.app.new_ui.tabTextColor
import com.call_blocke.app.new_ui.tintError
import com.call_blocke.app.new_ui.widgets.IconWithBackground
import com.call_blocke.app.new_ui.widgets.TextFieldWithErrorMsg
import com.call_blocke.app.screen.main.OnLifecycleEvent

data class SimTab(
    val name: String,
    @DrawableRes
    val iconId: Int? = null,
    val simInfo: SimInfoState
)

@Composable
fun SimCardInfoScreen(viewModel: SimCardViewModel, simSlot: Int = 0) {
    var currentTab: Int by remember {
        mutableStateOf(simSlot)
    }
    val state = viewModel.state
    val tabs = getTabList(state)
    val context = LocalContext.current
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.simsInfo(context)
            }

            else -> {}
        }
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
                            .tabIndicatorOffset(tabPositions[currentTab])
                            .height(4.dp)
                            .padding(horizontal = tabPositions[currentTab].width / 3)
                            .background(
                                color = primary,
                                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                            )
                    )
                }
                TabRow(
                    selectedTabIndex = currentTab,
                    contentColor = tabTextColor,
                    indicator = indicator,
                    backgroundColor = itemBackground
                ) {
                    tabs.forEachIndexed { index, tab ->
                        if (tab.iconId != null) {
                            LeadingIconTab(
                                text = { Text(tab.name) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = tab.iconId),
                                        contentDescription = ""
                                    )
                                },
                                selected = currentTab == index,
                                onClick = { currentTab = index }
                            )
                        } else {
                            Tab(text = { Text(tab.name, style = MaterialTheme.typography.h5) },
                                selected = currentTab == index,
                                onClick = { currentTab = index }
                            )
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            SimCardInfoTab(
                tabs[currentTab].simInfo,
                onNewLimitsSet = { dayLimit, monthLimit ->
                    viewModel.setNewLimitForSim(
                        context,
                        tabs[currentTab].simInfo.simSubInfo.simSlotIndex,
                        dayLimit,
                        monthLimit
                    )
                },
                onResetClicked = {
                    viewModel.resetSim(
                        tabs[currentTab].simInfo.simSubInfo.simSlotIndex,
                        context
                    )
                },
                onVerifyClicked = {
                    viewModel.verifySimCard(
                        tabs[currentTab].simInfo.simSubInfo.iccId,
                        tabs[currentTab].simInfo.simSubInfo.simSlotIndex,
                        context
                    )
                })
        }
    }
}

@Composable
fun getTabList(state: SimCardInfoScreenState): List<SimTab> {
    return mutableListOf<SimTab>().apply {
        if (state.firstSimSubInfo != null)
            add(
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
            add(
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