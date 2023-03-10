package com.call_blocke.app.new_ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.app.new_ui.Them
import com.call_blocke.app.new_ui.headerBackground
import com.call_blocke.app.new_ui.roboto700
import com.call_blocke.app.new_ui.spacerColor
import com.call_blocke.app.new_ui.tabIconColor
import com.call_blocke.app.new_ui.textColor
import com.call_blocke.app.new_ui.turnOnButtonBackground
import com.call_blocke.app.new_ui.uniqueIdTextColor
import com.call_blocke.db.SmsBlockerDatabase

@Composable
@Preview
fun HomeScreen() {
    val isConnected = remember {
        mutableStateOf(false)
    }
    val isRunning = remember {
        mutableStateOf(false)
    }
    val amount = remember {
        mutableStateOf(30.2525)
    }
    Them {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            Header()
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ConnectionState(isConnected = isConnected.value, isRunning = isRunning.value) {
                    isConnected.value = !isConnected.value
                }
                Spacer(modifier = Modifier.height(20.dp))
                WalletInfo(amount.value)
                Spacer(modifier = Modifier.height(20.dp))
                SmsInfo()
                Spacer(modifier = Modifier.height(20.dp))
                SimInfo("Sim 1", 10, 100) {}
                Spacer(modifier = Modifier.height(20.dp))
                SimInfo("Sim 2", 10, 100) {}
            }
        }
    }
}

@Composable
private fun SimInfo(simName: String, sent: Int, limit: Int, onClick: () -> Unit) {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sim_card),
                contentDescription = "Sim card"
            )
            Text(
                text = simName,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "$sent/", style = MaterialTheme.typography.h2, fontFamily = roboto700)
                Text(text = "$limit", style = MaterialTheme.typography.h4)
            }
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun SmsInfoComponent(
    componentName: String,
    @DrawableRes
    iconId: Int,
    count: Int = 0
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painter = painterResource(id = iconId), contentDescription = componentName)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = componentName, style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "$count", style = MaterialTheme.typography.h1)
    }
}

@Composable
private fun SmsInfo() {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 16.dp, horizontal = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmsInfoComponent(
                iconId = R.drawable.ic_recieved_sms,
                componentName = stringResource(id = R.string.leftToSend)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(spacerColor, shape = RoundedCornerShape(1.dp))
            )
            SmsInfoComponent(
                iconId = R.drawable.ic_delivered_sms,
                componentName = stringResource(id = R.string.delivered)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(spacerColor, shape = RoundedCornerShape(1.dp))
            )
            SmsInfoComponent(
                iconId = R.drawable.ic_undelivered_sms,
                componentName = stringResource(id = R.string.undelivered)
            )
        }
    }
}

@Composable
private fun WalletInfo(amount: Double) {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_account_balance_wallet
                    ),
                    contentDescription = "Amount"
                )
                Text(
                    text = stringResource(id = R.string.amount),
                    style = MaterialTheme.typography.h5
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                val doubleAsString: String = java.lang.String.valueOf(amount)
                val indexOfDecimal = doubleAsString.indexOf(".")
                Text(
                    text = "${doubleAsString.substring(0, indexOfDecimal)}.",
                    style = MaterialTheme.typography.h1
                )
                Text(
                    text = doubleAsString.substring(indexOfDecimal + 1),
                    style = MaterialTheme.typography.h4
                )
            }
        }
    }
}

@Composable
private fun ConnectionState(
    isConnected: Boolean,
    isRunning: Boolean,
    onStartStopClick: () -> Unit
) {
    Container(Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Icon(
                    painter = painterResource(
                        id = if (isConnected)
                            R.drawable.ic_connected else R.drawable.ic_disconnected
                    ),
                    contentDescription = "Connection state"
                )
                Text(
                    text = stringResource(id = R.string.status),
                    style = MaterialTheme.typography.h5
                )
            }

            Text(
                text = stringResource(
                    id = if (isConnected)
                        R.string.connected else R.string.disconnected
                ),
                style = MaterialTheme.typography.h3
            )

            Button(
                onClick = onStartStopClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = turnOnButtonBackground)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRunning)
                            R.drawable.ic_stop else R.drawable.ic_play_arrow
                    ),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(headerBackground)
            .padding(top = 22.dp, start = 25.dp, bottom = 12.dp, end = 30.dp)
    ) {
        ProfilePicture("VK")
        Spacer(modifier = Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Volodymyr Kryvonos",
                style = MaterialTheme.typography.h4,
                color = textColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = SmsBlockerDatabase.deviceID,
                color = uniqueIdTextColor,
                style = MaterialTheme.typography.overline,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.h5,
                color = textColor
            )
        }
        IconButton(modifier = Modifier.padding(top = 12.dp), onClick = { }) {
            Icon(
                painterResource(id = R.drawable.ic_logout),
                contentDescription = "Logout",
                tint = tabIconColor
            )
        }
    }
}

@Composable
fun ProfilePicture(name: String) {
    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(size = 14.dp))
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.profile_pic),
            contentDescription = "Profile picture",
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = name,
            style = MaterialTheme.typography.h2,
            modifier = Modifier.align(Alignment.Center),
            color = Color.White
        )
    }
}

@Composable
fun Container(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) =
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colors.onBackground)
            .fillMaxWidth()
            .then(modifier)
    ) {
        content()
    }
