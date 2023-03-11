package com.call_blocke.app.new_ui.screens.sim_card_info_screen

import android.telephony.SubscriptionInfo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.TabRow
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
import com.call_blocke.app.new_ui.buttonBackground
import com.call_blocke.app.new_ui.buttonTextColor
import com.call_blocke.app.new_ui.disabledButton
import com.call_blocke.app.new_ui.roboto700
import com.call_blocke.app.new_ui.screens.home_screen.Container
import com.call_blocke.app.new_ui.simInfoCaptionStyle
import com.call_blocke.app.new_ui.simInfoDataStyle
import com.call_blocke.app.new_ui.tabTextColor
import com.call_blocke.app.new_ui.widgets.TextField
import com.call_blocke.app.screen.main.OnLifecycleEvent

data class SimTab(
    val id: Int,
    val name: String,
    @DrawableRes
    val iconId: Int? = null,
    val simInfo: SimInfoState
)

@Composable
fun SimCardInfoScreen(viewModel: SimCardViewModel, simSlot: Int) {
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
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = stringResource(id = R.string.sim_card_info),
            style = MaterialTheme.typography.h2
        )
        Spacer(modifier = Modifier.height(22.dp))
        when (tabs.size) {
            0 -> {
                NoSimDetected()
            }

            1 -> {
                SimCardInfoTab(
                    tabs.first().simInfo,
                    onNewLimitsSet = { dayLimit, monthLimit -> },
                    onResetClicked = {},
                    onVerifyClicked = {}
                )
            }

            else -> {
                TabRow(selectedTabIndex = currentTab, contentColor = tabTextColor) {
                    tabs.forEachIndexed { index, tab ->
                        if (tab.iconId != null) {
                            LeadingIconTab(text = { Text(tab.name) },
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
                            Tab(text = { Text(tab.name) },
                                selected = currentTab == index,
                                onClick = { currentTab = index }
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                SimCardInfoTab(tabs[currentTab].simInfo,
                    onNewLimitsSet = { dayLimit, monthLimit -> },
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
}

@Composable
fun getTabList(state: SimCardInfoScreenState): List<SimTab> {
    return mutableListOf<SimTab>().apply {
        if (state.firstSimSubInfo != null)
            add(
                SimTab(
                    0, stringResource(id = R.string.simWithPlaceHolder, 1),
                    if (state.firstSimDayLimit > state.deliveredFirstSim && !state.firstSimVerificationState.isNeedVerification()) {
                        R.drawable.ic_sim_card
                    } else {
                        R.drawable.ic_sim_card_alert
                    },
                    SimInfoState(
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
                    0, stringResource(id = R.string.simWithPlaceHolder, 2),
                    if (state.secondSimDayLimit > state.deliveredSecondSim && !state.secondSimVerificationState.isNeedVerification()) {
                        R.drawable.ic_sim_card
                    } else {
                        R.drawable.ic_sim_card_alert
                    },
                    SimInfoState(
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
            SimCardOutOfSms(onResetClicked)
            Spacer(modifier = Modifier.height(20.dp))
        }
        SimCardInfo(
            simInfo.simSubInfo,
            phoneNumber = simInfo.simVerificationState.phoneNumber,
            connectedOn = simInfo.connectedOn
        )
        Spacer(modifier = Modifier.height(20.dp))
        SentSmsToday(simInfo.delivered, simInfo.limit)
        if (!simInfo.simVerificationState.isNeedVerification()) {
            Spacer(modifier = Modifier.height(20.dp))
            LimitFields(onNewLimitsSet)
        }
    }
}

@Composable
private fun SimCardNotVerified(simId: Int, onVerifyClicked: () -> Unit) {
    Container {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sim_card_alert),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(
                        id = if (simId == 0)
                            R.string.first_sim_card_not_verified else R.string.second_sim_card_not_verified
                    ),
                    style = MaterialTheme.typography.h5
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onVerifyClicked,
                shape = RoundedCornerShape(100f),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
            ) {
                Text(
                    text = stringResource(id = R.string.verify),
                    style = MaterialTheme.typography.h5,
                    color = buttonTextColor
                )
            }
        }
    }
}


@Composable
private fun SimCardOutOfSms(onResetClicked: () -> Unit) {
    Container {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sim_card_alert),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(
                        id = R.string.simLimitIsFull
                    ),
                    style = MaterialTheme.typography.h5
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onResetClicked,
                shape = RoundedCornerShape(100f),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
            ) {
                Text(
                    text = stringResource(id = R.string.reset),
                    style = MaterialTheme.typography.h5,
                    color = buttonTextColor
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
                    stringResource(id = R.string.operator),
                    simSubInfo.carrierName.toString()
                )
                Spacer(modifier = Modifier.weight(1f))
                SimInfoDataWithCaption(
                    stringResource(id = R.string.imsi),
                    phoneNumber ?: stringResource(
                        id = R.string.notDetermined
                    )
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
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delivered_sms_today),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.sentSMSToday),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "$sent/", style = MaterialTheme.typography.h2, fontFamily = roboto700)
                Text(text = "$limit", style = MaterialTheme.typography.h4)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LimitFields(onNewLimitsSet: (Int, Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                hint = stringResource(id = R.string.sms_count_per_day),
                keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.width(20.dp))
            TextField(
                modifier = Modifier.weight(1f),
                hint = stringResource(id = R.string.sms_count_per_month),
                keyboardType = KeyboardType.Decimal
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onNewLimitsSet(0, 0) },
            shape = RoundedCornerShape(100f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = buttonBackground,
                disabledBackgroundColor = disabledButton
            )
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