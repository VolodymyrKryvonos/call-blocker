package com.call_blocke.app.screen.auth

import android.media.Image
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.app.screen.auth.login.LoginScreen
import com.call_blocke.app.screen.auth.register.RegisterScreen
import com.call_blocke.app.screen.auth.reset_pass.ResetPassword
import com.rokobit.adstv.ui.accentColor
import com.rokobit.adstv.ui.element.Label
import com.rokobit.adstv.ui.element.Snackbar
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun AuthScreen(mViewModel: AuthViewModel = viewModel()) {
    val state = mViewModel.resetStatus
    Column(
        modifier = Modifier
            .padding(primaryDimens)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val navController: NavHostController = rememberNavController()


        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        Header(
            currentBackStackEntry?.destination?.route ?: ""
        )

        Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

        //val isSuccessLogin: Boolean? by mViewModel.isSuccessLogin.observeAsState(null)

        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(navController = navController, mViewModel = mViewModel)
            }
            composable("reset_pass") {
                ResetPassword(navController = navController, mViewModel = mViewModel)
            }
            composable("register") {
                RegisterScreen(navController = navController, mViewModel = mViewModel)
            }
        }

        Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

        TextNormal(text = "Version ${BuildConfig.VERSION_NAME}")
    }

    Column(modifier = Modifier.wrapContentSize(align = Alignment.BottomCenter)) {
        Spacer(modifier = Modifier.weight(1.0f))
        if (state.value is ResetState.Success) {
            Snackbar(stringResource(id = R.string.reset_link_sent))
        }
        if (state.value is ResetState.Error) {
            Snackbar((state.value as ResetState.Error).error)
        }
    }
}

@Composable
@Preview
fun Header(currentRoute: String) {
    Image(
        imageVector = ImageVector.vectorResource(id = R.drawable.app_logo),
        contentDescription = null,
        modifier = Modifier.requiredSize(
            size = primaryDimens * 3
        )
    )
    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Title(text = stringResource(id = R.string.login_title))

    //Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)

    Label(
        text = stringResource(
            id = when (currentRoute) {
                "login" -> R.string.login_label
                "register" -> R.string.register_label
                else -> R.string.reset_pass
            }
        ), color = accentColor.copy(alpha = 0.5f)
    )
}
