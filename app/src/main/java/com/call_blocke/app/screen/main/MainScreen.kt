package com.call_blocke.app.screen.main

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
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstv.ui.element.Label
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MainScreen(navController: NavHostController, mViewMode: MainViewModel = viewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()) {

        Header(mViewMode)

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Menu(navController = navController, mViewMode = mViewMode)
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(primaryDimens),
            contentAlignment = Alignment.Center) {
            TextNormal(text = "Version ${BuildConfig.VERSION_NAME}")
        }
    }
}

@Composable
fun Header(mViewMode: MainViewModel) = Column(modifier = Modifier.padding(primaryDimens)) {
    Title(text = "User name")


    val systemInfo by mViewMode.systemInfo().observeAsState(initial = SmsBlockerDatabase.systemDetail)

    Text(text = stringResource(id = R.string.main_header_amount) + " " + systemInfo.amount + " €")
    Text(text = stringResource(id = R.string.main_header_left_count) + " " + systemInfo.leftCount)
    Text(text = stringResource(id = R.string.main_header_delivered_count) + " " + systemInfo.deliveredCount)
    Text(text = stringResource(id = R.string.main_header_undelivered_count) + " " + systemInfo.undeliveredCount)
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
        items(5) {
            val i = it + 1
            MenuItem(
                icon = when (i) {
                    1 -> if (isExecutorRunning) Icons.Filled.Close else Icons.Filled.PlayArrow
                    2 -> Icons.Filled.List
                    3 -> Icons.Filled.AccountCircle
                    4 -> Icons.Filled.Lock
                    else -> Icons.Filled.Settings
                },
                title = when (i) {
                    1 ->  if (isExecutorRunning)
                        stringResource(id = R.string.main_menu_stop_job)
                    else stringResource(id = R.string.main_menu_start_job)
                    2 -> stringResource(id = R.string.main_menu_task_list)
                    3 -> stringResource(id = R.string.main_menu_withdraw_money)
                    4 -> stringResource(id = R.string.main_menu_black_list)
                    else -> stringResource(id = R.string.main_menu_set_sms_per_day)
                },
                isEnable = arrayListOf(1, 4, 2, 5).contains(i)
            ) {
                if (i == 1) {
                    if (isExecutorRunning)
                        mViewMode.stopExecutor(context)
                    else
                        mViewMode.runExecutor(context)
                }
                else if (i == 4)
                    navController.navigate("black_list")
                else if (i == 2)
                    navController.navigate("task_list")
                else if (i == 5)
                    navController.navigate("settings")
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MenuItem(icon: ImageVector,
             title: String,
             isEnable: Boolean,
             onClick: () -> Unit) {
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