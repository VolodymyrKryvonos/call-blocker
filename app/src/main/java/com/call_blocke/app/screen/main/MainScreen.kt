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

        Header()

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
fun Header() = Column(modifier = Modifier.padding(primaryDimens)) {
    Title(text = "User name")
    Label(text = stringResource(id = R.string.main_header_title) + " 0$")
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Menu(navController: NavHostController, mViewMode: MainViewModel) {
    val isExecutorRunning: Boolean by mViewMode.taskExecutorIsRunning.observeAsState(initial = false)
    val context = LocalContext.current

    LazyVerticalGrid(
        cells = GridCells.Fixed(2)
    ) {
        items(4) {
            val i = it + 1
            MenuItem(
                icon = when (i) {
                    1 -> if (isExecutorRunning) Icons.Filled.Close else Icons.Filled.PlayArrow
                    2 -> Icons.Filled.List
                    3 -> Icons.Filled.ShoppingCart
                    else -> Icons.Filled.Settings
                },
                title = when (i) {
                    1 ->  if (isExecutorRunning)
                        stringResource(id = R.string.main_menu_stop_job)
                    else stringResource(id = R.string.main_menu_start_job)
                    2 -> stringResource(id = R.string.main_menu_task_list)
                    3 -> stringResource(id = R.string.main_menu_withdraw_money)
                    else -> stringResource(id = R.string.main_menu_set_sms_per_day)
                },
                isEnable = arrayListOf(1, 4).contains(i)
            ) {
                if (i == 1) {
                    if (isExecutorRunning)
                        mViewMode.stopExecutor(context)
                    else
                        mViewMode.runExecutor(context)
                }
                else if (i == 4)
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
            .requiredSize(200.dp)
            .padding(primaryDimens),
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
                modifier = Modifier.requiredSize(75.dp),
                tint = primaryColor
            )
            Divider(
                modifier = Modifier.height(secondaryDimens),
                color = Color.Transparent
            )
            Text(text = title, contentAlignment = TextAlign.Center)
        }
    }
}