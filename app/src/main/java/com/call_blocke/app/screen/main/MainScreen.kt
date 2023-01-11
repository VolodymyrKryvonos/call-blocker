package com.call_blocke.app.screen.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.SimUtil
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.mainFont
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstvv_unit.loger.SmartLog


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MainScreen(navController: NavHostController, mViewMode: MainViewModel = viewModel()) {

    val isLoading by mViewMode.isLoading.observeAsState(false)
    val openValidateSimCardDialog = mViewMode.openValidateSimCardDialog.collectAsState(false)
    val openOutdatedVersionDialog = mViewMode.openOutdatedVersionDialog.collectAsState(false)
    val context = LocalContext.current
    SwipeRefresh(
        state = rememberSwipeRefreshState(isLoading),
        onRefresh = { mViewMode.reloadSystemInfo() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
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
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextNormal(text = "Version ${BuildConfig.VERSION_NAME}")
                }
            }
            if (openValidateSimCardDialog.value) {
                AlertDialog(
                    title = stringResource(id = R.string.verifyPhoneNumber),
                    modifier = Modifier.fillMaxSize(),
                    onClose = {
                        mViewMode.closeValidateSimCardDialog()
                    },
                    content = {
                        Button(
                            title = stringResource(R.string.ok),
                            modifier = Modifier.fillMaxWidth(),
                            isEnable = true
                        ) {
                            mViewMode.closeValidateSimCardDialog()
                            navController.navigate("sim_info")
                        }
                    }
                )
            }

            if (openOutdatedVersionDialog.value) {
                val profile = SmsBlockerDatabase.profile
                AlertDialog(
                    title = stringResource(id = R.string.update_application),
                    message = stringResource(
                        id = R.string.update_application_to_continue,
                        profile?.latestMajorVersion ?: 0,
                        profile?.latestMinorVersion ?: 0,
                        profile?.latestPatchVersion ?: 0
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onClose = {
                        mViewMode.closeOutdatedVersionDialog()
                    },
                    content = {
                        Button(
                            title = stringResource(R.string.ok),
                            modifier = Modifier.fillMaxWidth(),
                            isEnable = true
                        ) {
                            mViewMode.closeValidateSimCardDialog()
                            val url = "https://free-tokens.info/download_the_latest_app"
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            context.startActivity(i)
                        }
                    }
                )
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
fun Header(mViewMode: MainViewModel) =
    Column(modifier = Modifier.padding(start = primaryDimens, end = primaryDimens)) {
        val systemInfo by mViewMode.systemInfoLiveData.observeAsState(initial = SmsBlockerDatabase.systemDetail)
        val isServerOnline by mViewMode.isServerOnline.collectAsState()
        Title(text = mViewMode.userName())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            androidx.compose.material.Text(
                text = if (isServerOnline) "Status: connected" else "Status: disconnected",
                color = if (isServerOnline) Color.Green else Color.Red,
                fontSize = 18.sp,
                fontFamily = mainFont
            )

        }

        Text(text = stringResource(id = R.string.main_header_amount) + " " + systemInfo.amount + " €")
        Text(text = stringResource(id = R.string.main_header_left_count) + " " + systemInfo.leftCount)
        Text(text = stringResource(id = R.string.main_header_delivered_count) + " " + systemInfo.deliveredCount)
        Text(text = stringResource(id = R.string.main_header_undelivered_count) + " " + systemInfo.undeliveredCount)
        SentSmsInfo(mViewMode)
        Text(text = stringResource(id = R.string.device_id) + mViewMode.deviceID)
    }

@Composable
fun SentSmsInfo(mViewModel: MainViewModel) {
    val sims by mViewModel.simInfoState.collectAsState(initial = null)
    val context = LocalContext.current
    for ((index, fullSimInfoModel) in sims?.withIndex() ?: emptyList()) {
        if (SimUtil.isFirstSimAllow(context) && index == 0) {
            Row {
                Text(text = "Sim 1: ")
                Text(text = "${fullSimInfoModel.simDelivered} SMS of ${fullSimInfoModel.simPerDay} today")
            }
        }

        if (SimUtil.isSecondSimAllow(context) && index == 1) {
            Row {
                Text(text = "Sim 2: ")
                Text(text = "${fullSimInfoModel.simDelivered} SMS of ${fullSimInfoModel.simPerDay} today")
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
    val firstSimValidationInfo = mViewMode.firstSimValidationInfo.collectAsState()
    val secondSimValidationInfo = mViewMode.secondSimValidationInfo.collectAsState()
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                mViewMode.checkSimCards(context)
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
        items(6) {
            val i = it + 1
            MenuItem(
                icon = when (i) {
                    1 -> if (isExecutorRunning) Icons.Filled.Close else Icons.Filled.PlayArrow
                    4 -> Icons.Filled.List
                    3 -> Icons.Filled.Settings
                    2 -> Icons.Filled.Refresh
                    5 -> Icons.Filled.Info
                    else -> Icons.Filled.ExitToApp
                },
                title = when (i) {
                    1 -> if (isExecutorRunning)
                        stringResource(id = R.string.main_menu_stop_job)
                    else stringResource(id = R.string.main_menu_start_job)
                    2 -> stringResource(id = R.string.main_menu_refresh_full)
                    3 -> stringResource(id = R.string.main_menu_set_sms_per_day)
                    4 -> stringResource(id = R.string.main_menu_task_list)
                    5 -> stringResource(id = R.string.main_menu_sim_info)
                    else -> stringResource(id = R.string.main_menu_log_out)
                },
                backgroundColor = getMenuButtonBackground(
                    i,
                    sims ?: listOf(),
                    context,
                    firstSimValidationInfo.value,
                    secondSimValidationInfo.value
                ),
                isEnable = isMenuButtonEnabled(i)
            ) {
                when (i) {
                    1 -> {
                        if (isExecutorRunning) {
                            SmartLog.e("User stop service")
                            mViewMode.stopExecutor(context)
                            mViewMode.notifyServerUserStopService()
                        } else {
                            SmartLog.e("User start service")
                            mViewMode.runExecutor(context)
                            mViewMode.checkSimCards(context)
                            mViewMode.checkIsSimCardsShouldBeValidated()
                        }
                    }
                    2 -> navController.navigate("refresh")
                    3 -> navController.navigate("settings")
                    4 -> navController.navigate("task_list")
                    5 -> navController.navigate("sim_info")
                    else -> mViewMode.logOut(context = context)
                }
            }
        }
    }
}


fun isMenuButtonEnabled(
    index: Int
): Boolean {
    return when (index) {
        1 -> !SmsBlockerDatabase.isSimChanged
        else -> true
    }
}

fun getMenuButtonBackground(
    index: Int,
    sims: List<FullSimInfoModel>,
    context: Context,
    firstSimValidationInfo: SimValidationInfo,
    secondSimValidationInfo: SimValidationInfo
): Color {
    val isAnySimOutOfSMS = sims.any { sim ->
        sim.simPerDay <= sim.simDelivered && SimUtil.isSimAllow(
            context,
            sim.simSlot
        )
    }
    val isAnyCardInvalid =
        firstSimValidationInfo.status == SimValidationStatus.INVALID ||
                secondSimValidationInfo.status == SimValidationStatus.INVALID
    return when {
        index == 5 && isAnyCardInvalid -> Color.Red
        index == 2 && isAnySimOutOfSMS -> Color.Red
        else -> secondaryColor
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
            .padding(5.dp),
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
                .padding(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.requiredSize(55.dp),
                tint = primaryColor
            )
            Divider(
                modifier = Modifier.height(5.dp),
                color = Color.Transparent
            )
            TextNormal(text = title, contentAlignment = TextAlign.Center)
        }
    }
}