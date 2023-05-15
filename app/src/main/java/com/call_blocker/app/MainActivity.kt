package com.call_blocker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocker.adstv.ui.backgroundBrush
import com.call_blocker.app.new_ui.Them
import com.call_blocker.app.screen.SplashScreen
import com.call_blocker.app.screen.SplashViewModel
import com.call_blocker.app.screen.auth.AuthScreen
import com.call_blocker.app.screen.auth.AuthViewModel
import com.call_blocker.app.service.ChangeSimCardNotifierService
import com.call_blocker.db.SmsBlockerDatabase

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        if (SmsBlockerDatabase.isSimChange) {
            ChangeSimCardNotifierService.startService(this)
        }
    }

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
                    val isUserAuth by SmsBlockerDatabase
                        .userIsAuthLiveData
                        .collectAsState(initial = SmsBlockerDatabase.userToken != null)


                    if (isUserAuth)
                        MainView(intent?.data?.toString())
                    else
                        AuthView()
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
        AuthScreen(mViewModel)
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun MainView(deapLink: String? = null) {
        SplashScreen(splashViewModel, deapLink)
    }
}