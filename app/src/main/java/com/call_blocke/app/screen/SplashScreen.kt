package com.call_blocke.app.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.R
import com.call_blocke.app.screen.intro.IntroScreen
import com.call_blocke.app.screen.main.MainScreen
import com.call_blocke.app.screen.settings.SettingsScreen
import com.call_blocke.app.screen.task_list.TaskListScreen
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens
import kotlinx.coroutines.delay

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SplashScreen(mViewModel: SplashViewModel) {
    val isPermissionsGranted by mViewModel.isPermissionGranted.observeAsState(initial = null)
    val isAppDefault by mViewModel.isAppDefault.observeAsState(initial = null)

    if (isPermissionsGranted == true && isAppDefault == true)
        Main()
    else if (isPermissionsGranted == false || isAppDefault == false)
        IntroScreen(mViewModel = mViewModel)
    else
        Banner(mViewModel)
}

@Composable
fun Banner(mViewModel: SplashViewModel) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = null,
        modifier = Modifier.requiredSize(
            size = primaryDimens * 5
        )
    )

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Title(text = stringResource(id = R.string.app_name))

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        delay(1000L)
        mViewModel.initMe(context)
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Main() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("task_list") {
            TaskListScreen()
        }
    }
}