package com.call_blocker.app.screen.auth.reset_pass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.call_blocker.adstv.ui.element.Button
import com.call_blocker.adstv.ui.element.Field
import com.call_blocker.adstv.ui.primaryDimens
import com.call_blocker.app.R
import com.call_blocker.app.screen.auth.AuthViewModel
import com.call_blocker.app.screen.auth.ResetState
import com.call_blocker.app.screen.auth.ResetState.Loading

@ExperimentalComposeUiApi
@Composable
fun ResetPassword(
    mViewModel: AuthViewModel = viewModel(),
    navController: NavHostController
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val state = mViewModel.resetStatus

    val emailValue = remember {
        mutableStateOf("")
    }

    val isLoading = remember {
        mutableStateOf(state.value is Loading)
    }

    Field(
        hint = stringResource(id = R.string.register_login_field),
        value = emailValue,
        isEnable = !isLoading.value
    )
    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Button(
        title = stringResource(id = R.string.continue_res),
        modifier = Modifier.fillMaxWidth(),
        isProgress = isLoading,
        isEnable = emailValue.value.isNotEmpty()
    ) {
        keyboardController?.hide()
        mViewModel.resetPass(
            email = emailValue.value
        )
    }
    if (state.value is ResetState.Success) {
        LaunchedEffect(key1 = Unit) {
            navController.navigateUp()
        }
    }

}