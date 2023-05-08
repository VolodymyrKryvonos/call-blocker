package com.call_blocker.app.new_ui.screens.home_screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.R
import com.call_blocker.app.new_ui.backgroundConnected
import com.call_blocker.app.new_ui.backgroundError
import com.call_blocker.app.new_ui.buttonShape
import com.call_blocker.app.new_ui.buttonTextColor
import com.call_blocker.app.new_ui.darkGrey
import com.call_blocker.app.new_ui.gray3
import com.call_blocker.app.new_ui.gray6
import com.call_blocker.app.new_ui.headerBackground
import com.call_blocker.app.new_ui.itemStroke
import com.call_blocker.app.new_ui.lightBlue
import com.call_blocker.app.new_ui.navigation.Routes
import com.call_blocker.app.new_ui.primary
import com.call_blocker.app.new_ui.roboto700
import com.call_blocker.app.new_ui.spacerColor
import com.call_blocker.app.new_ui.tabIconColor
import com.call_blocker.app.new_ui.textColor
import com.call_blocker.app.new_ui.tintConnected
import com.call_blocker.app.new_ui.tintError
import com.call_blocker.app.new_ui.uniqueIdTextColor
import com.call_blocker.app.new_ui.widgets.IconWithBackground
import com.call_blocker.app.screen.main.OnLifecycleEvent
import com.call_blocker.common.SimUtil
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.verification.domain.VerificationInfo

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavHostController) {
    val state = viewModel.state
    val context = LocalContext.current
    val spaceHeight = 14.dp
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.reloadSystemInfo(context)
                viewModel.checkSimCards(context)
            }

            else -> {}
        }
    }
    if (state.isLoading) {
        CircularProgressIndicator()
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Header(state.getUserName(), state.getInitials()) {
            viewModel.logOut(context)
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 15.dp, vertical = spaceHeight)
        ) {
            ConnectionState(isConnected = state.isConnected, isRunning = state.isRunning) {
                if (viewModel.state.isRunning) {
                    viewModel.stopExecutor(context)
                } else {
                    viewModel.runExecutor(context)
                }
            }
            if (BuildConfig.showAmount) {
                Spacer(modifier = Modifier.height(spaceHeight))
                WalletInfo(state.amount)
            }
            Spacer(modifier = Modifier.height(spaceHeight))
            SmsInfo(
                delivered = state.delivered,
                undelivered = state.undelivered,
                leftToSend = state.leftToSend
            )
            if (state.isFirstSimAvailable) {
                Spacer(modifier = Modifier.height(spaceHeight))
                SimInfo(0,
                    state.deliveredFirstSim,
                    state.firstSimDayLimit,
                    state.firstSimVerificationState,
                    onResetClick = {
                        viewModel.resetSim(0, context)
                    },
                    onVerifyClick = {
                        viewModel.verifySimCard(
                            SimUtil.firstSim(context)?.iccId ?: "", 0, context
                        )
                    },
                    onClick = {
                        navController.navigate(
                            Routes.BottomNavigation.SimInfoScreen.getDestinationWithSimId(
                                0
                            )
                        )
                    })
            }
            if (state.isSecondSimAvailable) {
                Spacer(modifier = Modifier.height(spaceHeight))
                SimInfo(1,
                    state.deliveredSecondSim,
                    state.secondSimDayLimit,
                    state.secondSimVerificationState,
                    onResetClick = {
                        viewModel.resetSim(1, context)
                    },
                    onVerifyClick = {
                        viewModel.verifySimCard(
                            SimUtil.secondSim(context)?.iccId ?: "", 1, context
                        )
                    },
                    onClick = {
                        navController.navigate(
                            Routes.BottomNavigation.SimInfoScreen.getDestinationWithSimId(
                                1
                            )
                        )
                    })
            }
        }
    }
}

@Composable
private fun SimInfo(
    simSlot: Int,
    sent: Int,
    limit: Int,
    verificationState: VerificationInfo,
    onResetClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onClick: () -> Unit
) {
    Container {
        if (verificationState.isNeedVerification() || verificationState.isVerificationInProgress()) {
            VerificationSimInfo(simSlot, verificationState, onVerifyClick)
        } else {
            if (sent < limit || limit == 0) {
                SimInfo(simSlot, sent, limit, onClick)
            } else {
                OutOfSms(simSlot, sent, limit, onResetClick)
            }
        }
    }
}

@Composable
fun OutOfSms(simSlot: Int, sent: Int, limit: Int, onResetClick: () -> Unit) {
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
                text = stringResource(id = R.string.simWithPlaceHolder, simSlot + 1),
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
            Row(Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$sent",
                    style = MaterialTheme.typography.h2,
                    fontFamily = roboto700,
                    color = tintError
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "sms", style = MaterialTheme.typography.body2, color = tintError)
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
            onClick = onResetClick,
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

@Composable
fun VerificationSimInfo(
    simSlot: Int, verificationState: VerificationInfo, onVerifyClick: () -> Unit
) {
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
                text = stringResource(id = R.string.simWithPlaceHolder, simSlot + 1),
                style = MaterialTheme.typography.h5
            )
            Text(
                text = stringResource(id = R.string.simCardNotVerified),
                style = MaterialTheme.typography.body2,
                color = darkGrey
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        if (verificationState.isVerificationInProgress()) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onVerifyClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = primary),
                shape = buttonShape,
                enabled = verificationState.isNeedVerification()
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
fun SimInfo(
    simSlot: Int, sent: Int, limit: Int, onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconWithBackground(
            iconDrawable = if (simSlot == 0) R.drawable.ic_sim_01 else R.drawable.ic_sim_02,
            contentDescription = "Sim card",
            tint = primary
        )
        Spacer(modifier = Modifier.width(5.dp))
        Column(
            Modifier
                .height(IntrinsicSize.Min)
        ) {
            Text(
                text = stringResource(id = R.string.simWithPlaceHolder, simSlot + 1),
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
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward), contentDescription = ""
            )
        }
    }
}


@Composable
private fun SmsInfoComponent(
    componentName: String, @DrawableRes iconId: Int, count: Int = 0
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconWithBackground(
            iconDrawable = iconId,
            tint = primary,
            contentDescription = componentName
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = componentName, style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.h1,
                fontFamily = roboto700,
                color = primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "sms", style = MaterialTheme.typography.body2, color = primary)
        }
    }
}

@Composable
private fun SmsInfo(delivered: Int, undelivered: Int, leftToSend: Int) {
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
                componentName = stringResource(id = R.string.leftToSend),
                count = leftToSend
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(spacerColor, shape = RoundedCornerShape(1.dp))
            )
            SmsInfoComponent(
                iconId = R.drawable.ic_delivered_sms,
                componentName = stringResource(id = R.string.delivered),
                count = delivered
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(spacerColor, shape = RoundedCornerShape(1.dp))
            )
            SmsInfoComponent(
                iconId = R.drawable.ic_undelivered_sms,
                componentName = stringResource(id = R.string.undelivered),
                count = undelivered
            )
        }
    }
}

@Composable
private fun WalletInfo(amount: Float) {
    Container {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconWithBackground(
                iconDrawable = R.drawable.ic_account_balance_wallet,
                background = lightBlue,
                tint = primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.amount), style = MaterialTheme.typography.h5
                )
                Text(
                    text = stringResource(id = R.string.current_balance),
                    style = MaterialTheme.typography.body2,
                    color = darkGrey
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                val doubleAsString: String = java.lang.String.valueOf(amount)
                val indexOfDecimal = doubleAsString.indexOf(".")
                Text(
                    text = "${doubleAsString.substring(0, indexOfDecimal)}.",
                    style = MaterialTheme.typography.h1,
                    color = primary
                )
                Text(
                    text = "${doubleAsString.substring(indexOfDecimal + 1)} €",
                    style = MaterialTheme.typography.h4,
                    color = primary
                )
            }
        }
    }
}

@Composable
private fun ConnectionState(
    isConnected: Boolean, isRunning: Boolean, onStartStopClick: () -> Unit
) {
    Container(Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconWithBackground(
                iconDrawable = if (isConnected) R.drawable.ic_connected else R.drawable.ic_disconnected,
                contentDescription = "Connection state",
                tint = if (isConnected) tintConnected else tintError,
                background = if (isConnected) backgroundConnected else backgroundError
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.status),
                    style = MaterialTheme.typography.body2,
                    color = darkGrey
                )
                Text(
                    text = stringResource(
                        id = if (isConnected) R.string.connected else R.string.disconnected
                    ), style = MaterialTheme.typography.h3
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartStopClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = lightBlue)
            ) {
                Icon(
                    tint = primary,
                    painter = painterResource(
                        id = if (isRunning) R.drawable.ic_stop else R.drawable.ic_play_arrow
                    ), contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun Header(userName: String, initials: String, onLogoutClicked: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(headerBackground)
            .padding(top = 22.dp, start = 25.dp, bottom = 12.dp, end = 30.dp)
    ) {
        ProfilePicture(initials)
        Spacer(modifier = Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName, style = MaterialTheme.typography.h4, color = textColor
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
                text = "v ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.body2,
                color = gray3
            )
        }
        IconButton(modifier = Modifier.padding(top = 12.dp), onClick = onLogoutClicked) {
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
    modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit
) = Box(
    Modifier
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, itemStroke, shape = RoundedCornerShape(16.dp))
        .background(color = MaterialTheme.colors.onBackground)
        .fillMaxWidth()
        .then(modifier)
) {
    content()
}
