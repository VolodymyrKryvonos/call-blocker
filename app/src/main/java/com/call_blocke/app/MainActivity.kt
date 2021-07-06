package com.call_blocke.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.screen.SplashScreen
import com.call_blocke.app.screen.SplashViewModel
import com.call_blocke.app.screen.auth.AuthScreen
import com.call_blocke.app.screen.auth.AuthViewModel
import com.call_blocke.app.screen.auth.EnterAnimation
import com.call_blocke.app.screen.auth.login.LoginScreen
import com.call_blocke.app.screen.auth.register.RegisterScreen
import com.call_blocke.app.screen.main.MainScreen
import com.call_blocke.app.screen.settings.SettingsScreen
import com.rokobit.adstv.ui.Them
import com.rokobit.adstv.ui.backgroundBrush
import kotlinx.coroutines.delay
import android.content.Intent

import android.content.ComponentName
import android.content.Context
import com.call_blocke.db.SmsBlockerDatabase

val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_SMS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_MMS,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.CALL_PHONE
)

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Them {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = backgroundBrush
                        )
                ) {
                    if (SmsBlockerDatabase.userToken == null)
                        AuthView()
                    else
                        MainView()
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @Composable
    fun AuthView(mViewModel: AuthViewModel = viewModel()) {
        val isSuccessLogin: Boolean? by mViewModel.isSuccessLogin.observeAsState(initial = null)

        Crossfade(targetState = isSuccessLogin) {
            if (it == true)
                MainView()
            else
                AuthScreen(mViewModel)
        }
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun MainView() {
        SplashScreen(splashViewModel)

        LaunchedEffect(key1 = Unit) {
            delay(2000L)
            if (PERMISSIONS_REQUIRED.all { ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    it
                ) == PackageManager.PERMISSION_GRANTED })
                splashViewModel.isPermissionGranted.postValue(true)
            else
                requestPermissions(PERMISSIONS_REQUIRED, 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        splashViewModel.isPermissionGranted.postValue(
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        )

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
