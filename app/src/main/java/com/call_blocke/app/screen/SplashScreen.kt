package com.call_blocke.app.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.Navigation
import com.call_blocke.app.R
import com.call_blocke.app.screen.black_list.BlackListScreen
import com.call_blocke.app.screen.intro.IntroScreen
import com.call_blocke.app.screen.main.MainScreen
import com.call_blocke.app.screen.refresh_full.RefreshScreen
import com.call_blocke.app.screen.settings.SettingsScreen
import com.call_blocke.app.screen.sim_info.SimInfoScreen
import com.call_blocke.app.screen.task_list.TaskListScreen
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens
import kotlinx.coroutines.delay

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SplashScreen(mViewModel: SplashViewModel, deapLink: String? = null) {
    val isPermissionsGranted by mViewModel.isPermissionGranted.observeAsState(initial = null)
    val isAppDefault by mViewModel.isAppDefault.observeAsState(initial = null)

    if (isPermissionsGranted == true && isAppDefault == true)
        Main(deapLink)
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
        imageVector = ImageVector.vectorResource(R.drawable.app_logo),
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
fun Main(deapLink: String?) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable(Navigation.MainScreen.destination) {
            MainScreen(navController)
        }
        composable(Navigation.SettingsScreen.destination) {
            SettingsScreen(navController)
        }
        composable(Navigation.TaskListScreen.destination) {
            TaskListScreen()
        }
        composable(Navigation.BlackListScreen.destination) {
            BlackListScreen()
        }
        composable(Navigation.ResetSimScreen.destination) {
            RefreshScreen()
        }
        composable(Navigation.SimInfoScreen.destination) {
            SimInfoScreen()
        }
    }

    if (deapLink != null) {
        navController.navigate(deapLink)
    }
    val isSimChanged by SmsBlockerDatabase
        .onSimChanged
        .collectAsState(initial = SmsBlockerDatabase.isSimChanged)


    if (isSimChanged) {
        Column {
            Box(modifier = Modifier.weight(1f))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = stringResource(id = R.string.sim_slot_change_desc),
                        color = Color.White
                    )
                    Button(onClick = {
                        SmsBlockerDatabase.onSimChanged.tryEmit(false)
                        navController.navigate("refresh")
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}