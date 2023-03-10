package com.call_blocke.app.new_ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.call_blocke.app.R
import com.call_blocke.app.new_ui.Them
import com.call_blocke.app.new_ui.buttonBackground
import com.call_blocke.app.new_ui.buttonTextColor
import com.call_blocke.app.new_ui.roboto700
import com.call_blocke.app.new_ui.simInfoCaptionStyle
import com.call_blocke.app.new_ui.simInfoDataStyle
import com.call_blocke.app.new_ui.widgets.TextField

data class SimTab(
    val id: Int,
    val name: String,
    @DrawableRes
    val iconId: Int? = null,
    val simInfo: Unit = Unit
)

@Preview
@Composable
fun preview() {
    Them {
        SimCardInfoScreen()
    }
}

@Composable
fun SimCardInfoScreen() {
    val tabIndex = remember { mutableStateOf(0) }
    //TODO create list from subscription info
    val tabs = listOf(
        SimTab(0, "Sim 1", iconId = R.drawable.ic_sim_card),
        SimTab(1, "Sim 2", iconId = R.drawable.ic_sim_card)
    )
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
                SimCardInfoTab(tabs.first())
            }

            else -> {
                TabRow(selectedTabIndex = tabIndex.value) {
                    tabs.forEachIndexed { index, tab ->
                        if (tab.iconId != null) {
                            LeadingIconTab(text = { Text(tab.name) },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = tab.iconId),
                                        contentDescription = ""
                                    )
                                },
                                selected = tabIndex.value == index,
                                onClick = { tabIndex.value = index }
                            )
                        } else {
                            Tab(text = { Text(tab.name) },
                                selected = tabIndex.value == index,
                                onClick = { tabIndex.value = index }
                            )
                        }

                    }
                }
                SimCardInfoTab(tabs[tabIndex.value])
            }
        }
    }
}

@Composable
private fun NoSimDetected() {
    TODO("Not yet implemented")
}

@Composable
private fun SimCardInfoTab(simInfo: SimTab) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(22.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        SimCardNotVerified(simInfo.id)
        Spacer(modifier = Modifier.height(20.dp))
        SimCardOutOfSms()
        Spacer(modifier = Modifier.height(20.dp))
        SimCardInfo()
        Spacer(modifier = Modifier.height(20.dp))
        SentSmsToday(10, 100)
        Spacer(modifier = Modifier.height(20.dp))
        LimitFields()
    }
}

@Composable
private fun SimCardNotVerified(simId: Int) {
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
                onClick = { /*TODO*/ },
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
private fun SimCardOutOfSms() {
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
                onClick = { /*TODO*/ },
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
private fun SimCardInfo() {
    Container {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row {
                SimInfoDataWithCaption(stringResource(id = R.string.operator), "life:)")
                Spacer(modifier = Modifier.weight(1f))
                SimInfoDataWithCaption(stringResource(id = R.string.imsi), "not determined")
            }
            Spacer(modifier = Modifier.height(10.dp))
            SimInfoDataWithCaption(stringResource(id = R.string.connectedOn), "2023-01-24 16:36:34")
            Spacer(modifier = Modifier.height(10.dp))
            SimInfoDataWithCaption(stringResource(id = R.string.somCardId), "89380062300205763930")
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
private fun LimitFields() {
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
                hint = stringResource(id = R.string.sms_count_per_day)
            )
            Spacer(modifier = Modifier.width(20.dp))
            TextField(
                modifier = Modifier.weight(1f),
                hint = stringResource(id = R.string.sms_count_per_month)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { },
            shape = RoundedCornerShape(100f),
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
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