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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rokobit.adstv.ui.*
import com.rokobit.adstv.ui.element.Circle
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstvv_unit.loger.SmartLog
import kotlinx.coroutines.delay


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MainScreen(navController: NavHostController, mViewMode: MainViewModel = viewModel()) {

    val isLoading by mViewMode.isLoading.observeAsState(false)

    val isServerOnline by mViewMode.isServerOnline.collectAsState()

    val ping by mViewMode.isPingOn.collectAsState(initial = false)
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
                if (ping) {
                    Circle(color = Color.Green, modifier = Modifier.padding(end = 8.dp, top = 8.dp))
                }
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
    if (ping)
        LaunchedEffect(key1 = Unit, block = {
            delay(1000)
            mViewMode.isPingOn.emit(false)
        })
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

    Spacer(modifier = Modifier.height(primaryDimens))

    Text(text = stringResource(id = R.string.device_id) + mViewMode.deviceID)
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Menu(navController: NavHostController, mViewMode: MainViewModel) {
    val isExecutorRunning: Boolean by mViewMode.taskExecutorIsRunning.observeAsState(initial = false)
    val context = LocalContext.current

    LazyVerticalGrid(
        cells = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(primaryDimens / 2)
    ) {
        items(7) {
            val i = it + 1
            MenuItem(
                icon = when (i) {
                    1 -> if (isExecutorRunning) Icons.Filled.Close else Icons.Filled.PlayArrow
                    2 -> Icons.Filled.List
                    3 -> Icons.Filled.Lock
                    4 -> Icons.Filled.Settings
                    5 -> Icons.Filled.Refresh
                    6 -> Icons.Filled.Info
                    else -> Icons.Filled.ExitToApp
                },
                title = when (i) {
                    1 -> if (isExecutorRunning)
                        stringResource(id = R.string.main_menu_stop_job)
                    else stringResource(id = R.string.main_menu_start_job)
                    2 -> stringResource(id = R.string.main_menu_task_list)
                    3 -> stringResource(id = R.string.main_menu_black_list)
                    4 -> stringResource(id = R.string.main_menu_set_sms_per_day)
                    5 -> stringResource(id = R.string.main_menu_refresh_full)
                    6 -> stringResource(id = R.string.main_menu_sim_info)
                    else -> stringResource(id = R.string.main_menu_log_out)
                },
                isEnable = true
            ) {
                if (i == 1) {
                    if (isExecutorRunning) {
                        SmartLog.e("User stop service")
                        WorkManager.getInstance(context).cancelAllWork()
                        mViewMode.stopExecutor(context)
                    } else {
                        SmartLog.e("User start service")
                        mViewMode.runExecutor(context)
                    }
                } else if (i == 3)
                    navController.navigate("black_list")
                else if (i == 2)
                    navController.navigate("task_list")
                else if (i == 4)
                    navController.navigate("settings")
                else if (i == 5)
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            // .size(100.dp)
            // .wrapContentSize()
            .padding(primaryDimens / 2),
        shape = RoundedCornerShape(15),
        backgroundColor = secondaryColor,
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