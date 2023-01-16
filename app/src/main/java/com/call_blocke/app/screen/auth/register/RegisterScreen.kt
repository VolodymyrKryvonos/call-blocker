package com.call_blocke.app.screen.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.call_blocke.app.R
import com.call_blocke.app.screen.auth.AuthViewModel
import com.rokobit.adstv.ui.accentColor
import com.rokobit.adstv.ui.element.Button
import com.rokobit.adstv.ui.element.Field
import com.rokobit.adstv.ui.element.TextButton
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryDimens

@ExperimentalComposeUiApi
@Composable
fun RegisterScreen(mViewModel: AuthViewModel = viewModel(),
                   navController: NavHostController) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Fields(mViewModel)

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Bottom(navController = navController, mViewModel = mViewModel)
}

@ExperimentalComposeUiApi
@Composable
fun Fields(mViewModel: AuthViewModel = viewModel()) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailValue = remember {
        mutableStateOf("")
    }
    val userNameValue = remember {
        mutableStateOf("")
    }
    val passwordValue = remember {
        mutableStateOf("")
    }
    val rePasswordValue = remember {
        mutableStateOf("")
    }

    val isProgress = mViewModel.isLoading.observeAsState(false)

    val isSuccessLogin: Boolean? by mViewModel.isSuccessLogin.observeAsState(initial = null)

    val context = LocalContext.current
    Field(
        hint = stringResource(id = R.string.register_login_field),
        value = emailValue,
        isEnable = !isProgress.value,
        isError = isSuccessLogin == false
    )

    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)

    /*Field(
        hint = stringResource(id = R.string.register_user_name_field),
        value = userNameValue,
        isEnable = false,
    )

    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)*/

    Field(
        hint = stringResource(id = R.string.register_password_field),
        visualTransformation = PasswordVisualTransformation(),
        value = passwordValue,
        isEnable = !isProgress.value
    )

    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)

    Field(
        hint = stringResource(id = R.string.register_re_password_field),
        visualTransformation = PasswordVisualTransformation(),
        value = rePasswordValue,
        isEnable = !isProgress.value
    )

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Button(
        title = stringResource(id = R.string.register_next_button),
        modifier = Modifier.fillMaxWidth(),
        isProgress = isProgress,
        isEnable = emailValue.value.isNotEmpty()
                && passwordValue.value.isNotBlank()
                && passwordValue.value == rePasswordValue.value) {
        keyboardController?.hide()
        mViewModel.register(
            email = emailValue.value,
            password = passwordValue.value,
            packageName = context.packageName
        )
    }
}

@Composable
fun Bottom(navController: NavHostController,
           mViewModel: AuthViewModel = viewModel()) {
    TextNormal(text = stringResource(id = R.string.register_or_text), color = accentColor.copy(alpha = 0.5f))

    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)

    TextButton(title = stringResource(id = R.string.register_login_button)) {
        mViewModel.isSuccessLogin.postValue(null)
        navController.navigate("login") {
            launchSingleTop = true
        }
    }
}