package com.call_blocke.app.screen.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.R
import com.call_blocke.app.screen.auth.login.LoginScreen
import com.call_blocke.app.screen.auth.register.RegisterScreen
import com.rokobit.adstv.ui.accentColor
import com.rokobit.adstv.ui.backgroundBrush
import com.rokobit.adstv.ui.element.Label
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
fun AuthScreen(mViewModel: AuthViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .padding(primaryDimens)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
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
            composable("register") {
                RegisterScreen(navController = navController, mViewModel = mViewModel)
            }
        }
    }
}

@Composable
fun Header(currentRoute: String) {
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = null,
        modifier = Modifier.requiredSize(
            size = primaryDimens * 3
        )
    )

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Title(text = stringResource(id = R.string.login_title))

    //Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)

    Label(text = stringResource(id = when (currentRoute) {
        "login" -> R.string.login_label
        else -> R.string.register_label
    }), color = accentColor.copy(alpha = 0.5f))
}
