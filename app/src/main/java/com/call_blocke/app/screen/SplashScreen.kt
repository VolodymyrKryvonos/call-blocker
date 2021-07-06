package com.call_blocke.app.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
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
import com.call_blocke.app.screen.auth.EnterAnimation
import com.call_blocke.app.screen.main.MainScreen
import com.call_blocke.app.screen.settings.SettingsScreen
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SplashScreen(mViewModel: SplashViewModel) {
    val isPermissionsGranted by mViewModel.isPermissionGranted.observeAsState(initial = false)

    Crossfade(targetState = isPermissionsGranted) {
        if (it)
            Main(mViewModel = mViewModel)
        else
            Intro()
    }
}

@Composable
fun Intro() = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = null,
        modifier = Modifier.requiredSize(
            size = primaryDimens * 10
        )
    )

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Title(text = stringResource(id = R.string.app_name))
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Main(mViewModel: SplashViewModel) {

    mViewModel.openSMSAppChooser(LocalContext.current)

    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            EnterAnimation {
                MainScreen(navController)
            }
        }
        composable("settings") {
            EnterAnimation {
                SettingsScreen()
            }
        }
    }
}