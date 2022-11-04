package com.call_blocke.app.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.SimUtil
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rokobit.adstv.ui.*
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstvv_unit.loger.SmartLog


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MainScreen(navController: NavHostController, mViewMode: MainViewModel = viewModel()) {

    val isLoading by mViewMode.isLoading.observeAsState(false)

    val isServerOnline by mViewMode.isServerOnline.collectAsState()
    SwipeRefresh(
        state = rememberSwipeRefreshState(isLoading),
        onRefresh = { mViewMode.reloadSystemInfo() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = primaryDimens),
                    text = if (isServerOnline) "Server connected" else "Server disconnected",
                    color = if (isServerOnline) Color.Green else Color.Red,
                    fontSize = 24.sp,
                    fontFamily = mainFont
                )

            }

            Header(mViewMode)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Menu(navController = navController, mViewMode = mViewMode)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(primaryDimens),
                contentAlignment = Alignment.Center
            ) {
                TextNormal(text = "Version ${BuildConfig.VERSION_NAME}")
            }
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun Header(mViewMode: MainViewModel) = Column(modifier = Modifier.padding(primaryDimens)) {
    val systemInfo by mViewMode.systemInfoLiveData.observeAsState(initial = SmsBlockerDatabase.systemDetail)

    Title(text = mViewMode.userName())
    //Label(text = mViewMode.userPassword())

    Text(text = stringResource(id = R.string.main_header_amount) + " " + systemInfo.amount + " €")
    Text(text = stringResource(id = R.string.main_header_left_count) + " " + systemInfo.leftCount)
    Text(text = stringResource(id = R.string.main_header_delivered_count) + " " + systemInfo.deliveredCount)
    Text(text = stringResource(id = R.string.main_header_undelivered_count) + " " + systemInfo.undeliveredCount)
    SentSmsInfo(mViewMode)

    Spacer(modifier = Modifier.height(primaryDimens))
    Text(text = stringResource(id = R.string.device_id) + mViewMode.deviceID)
}

@Composable
fun SentSmsInfo(mViewModel: MainViewModel) {
    val sims by mViewModel.simInfoState.collectAsState(initial = null)
    val context = LocalContext.current
    for ((index, fullSimInfoModel) in sims?.withIndex() ?: emptyList()) {
        if (SimUtil.isFirstSimAllow(context) && index == 0) {
            Row {
                Text(text = "Sim 1 ")
                Text(text = "${fullSimInfoModel.simDelivered} SMS of ${SmsBlockerDatabase.smsPerDaySimFirst} today")

            }
        }

        if (SimUtil.isSecondSimAllow(context) && index == 1) {
            Row {
                Text(text = "Sim 2 ")
                Text(text = "${fullSimInfoModel.simDelivered} SMS of ${SmsBlockerDatabase.smsPerDaySimSecond} today")
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Menu(navController: NavHostController, mViewMode: MainViewModel) {
    val isExecutorRunning: Boolean by mViewMode.taskExecutorIsRunning.collectAsState(initial = false)
    val context = LocalContext.current
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                mViewMode.reloadSystemInfo()
                mViewMode.simsInfo()
                mViewMode.getProfile()
                mViewMode.resetSimIfChanged(context)
            }
            else -> {}
        }
    }
    val sims by mViewMode.simInfoState.collectAsState(initial = null)
    LazyVerticalGrid(
        cells = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(primaryDimens / 2)
    ) {
        items(7) {
            val i = it + 1
            MenuItem(
                icon = when (i) {
                    1 -> if (isExecutorRunning) Icons.Filled.Close else Icons.Filled.PlayArrow
                    5 -> Icons.Filled.List
                    3 -> Icons.Filled.Lock
                    4 -> Icons.Filled.Settings
                    2 -> Icons.Filled.Refresh
                    6 -> Icons.Filled.Info
                    else -> Icons.Filled.ExitToApp
                },
                title = when (i) {
                    1 -> if (isExecutorRunning)
                        stringResource(id = R.string.main_menu_stop_job)
                    else stringResource(id = R.string.main_menu_start_job)
                    2 -> stringResource(id = R.string.main_menu_refresh_full)
                    3 -> stringResource(id = R.string.main_menu_black_list)
                    4 -> stringResource(id = R.string.main_menu_set_sms_per_day)
                    5 -> stringResource(id = R.string.main_menu_task_list)
                    6 -> stringResource(id = R.string.main_menu_sim_info)
                    else -> stringResource(id = R.string.main_menu_log_out)
                },
                backgroundColor = if (i == 2) {
                    if (sims?.any { sim ->
                            sim.simPerDay <= sim.simDelivered && SimUtil.isSimAllow(
                                context,
                                sim.simSlot
                            )
                        } == true)
                        Color.Red
                    else secondaryColor
                } else {
                    secondaryColor
                },
                isEnable = if (i == 1) !SmsBlockerDatabase.isSimChanged else true
            ) {
                if (i == 1) {
                    if (isExecutorRunning) {
                        mViewMode.notifyServerUserStopService()
                        SmartLog.e("User stop service")
                        mViewMode.stopExecutor(context)
                    } else {
                        SmartLog.e("User start service")
                        mViewMode.runExecutor(context)
                    }
                } else if (i == 3)
                    navController.navigate("black_list")
                else if (i == 5)
                    navController.navigate("task_list")
                else if (i == 4)
                    navController.navigate("settings")
                else if (i == 2)
                    navController.navigate("refresh")
                else if (i == 6)
                    navController.navigate("sim_info")
                else if (i == 7)
                    mViewMode.logOut(context = context)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    isEnable: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            // .size(100.dp)
            // .wrapContentSize()
            .padding(primaryDimens / 2),
        shape = RoundedCornerShape(15),
        backgroundColor = backgroundColor,
        elevation = 6.dp,
        onClick = onClick,
        enabled = isEnable
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(secondaryDimens)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.requiredSize(55.dp),
                tint = primaryColor
            )
            Divider(
                modifier = Modifier.height(secondaryDimens / 2),
                color = Color.Transparent
            )
            TextNormal(text = title, contentAlignment = TextAlign.Center)
        }
    }
}