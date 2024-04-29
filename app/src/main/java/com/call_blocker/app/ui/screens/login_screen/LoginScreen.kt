package com.call_blocker.app.ui.screens.login_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.R
import com.call_blocker.app.ui.buttonBackground
import com.call_blocker.app.ui.buttonTextColor
import com.call_blocker.app.ui.darkGrey
import com.call_blocker.app.ui.disabledButton
import com.call_blocker.app.ui.medium24Sp
import com.call_blocker.app.ui.primary
import com.call_blocker.app.ui.primaryDimens
import com.call_blocker.app.ui.widgets.PasswordField
import com.call_blocker.app.ui.widgets.TextField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(viewModel: AuthorizationViewModel) {
    Column(
        modifier = Modifier
            .padding(28.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.requiredSize(
                size = primaryDimens * 4
            )
        )
        Text(text = stringResource(id = R.string.app_name), style = medium24Sp)
        Spacer(modifier = Modifier.height(40.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.email,
            onValueChange = {
                viewModel.email = it
                viewModel.isEmailValid()
            },
            hint = stringResource(id = R.string.email),
            isError = viewModel.emailError,
            autofillTypes = listOf(AutofillType.EmailAddress),
            keyboardType = KeyboardType.Email
        )
        if (viewModel.isSignUp) {
            Spacer(modifier = Modifier.height(15.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.whatsApp,
                onValueChange = {
                    viewModel.whatsApp = it
                    viewModel.isWhatsAppNumberValid()
                },
                isError = viewModel.whatsAppError,
                hint = stringResource(id = R.string.whatsApp),
                autofillTypes = listOf(AutofillType.PhoneNumber),
                keyboardType = KeyboardType.Phone
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        PasswordField(
            modifier = Modifier.fillMaxWidth(),
            hint = stringResource(id = R.string.password),
            autofillTypes = listOf(AutofillType.Password),
            isError = viewModel.passwordError,
            value = viewModel.password,
            onValueChange = {
                viewModel.password = it
            },
            keyboardType = KeyboardType.Password
        )
        if (viewModel.isSignUp) {
            Spacer(modifier = Modifier.height(15.dp))
            PasswordField(
                modifier = Modifier.fillMaxWidth(),
                hint = stringResource(id = R.string.confirmPassword),
                autofillTypes = listOf(AutofillType.Password),
                isError = viewModel.confirmPasswordError,
                value = viewModel.confirmPassword,
                onValueChange = {
                    viewModel.confirmPassword = it
                },
                keyboardType = KeyboardType.Password
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        val context = LocalContext.current
        if (viewModel.isLoading) {
            CircularProgressIndicator(color = primary)
        } else {
            Button(
                onClick = { viewModel.signIn(context.packageName, context) },
                shape = RoundedCornerShape(100f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = buttonBackground,
                    disabledBackgroundColor = disabledButton
                ),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
            ) {

                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = stringResource(id = if (viewModel.isSignUp) R.string.signUp else R.string.signIn),
                    style = MaterialTheme.typography.h5,
                    color = buttonTextColor
                )
            }
        }

        if (BuildConfig.showAmount) {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = stringResource(id = R.string.or),
                style = MaterialTheme.typography.h5,
                color = darkGrey
            )
            Spacer(modifier = Modifier.height(22.dp))

            TextButton(onClick = { viewModel.isSignUp = !viewModel.isSignUp }) {
                Text(
                    text = stringResource(id = if (viewModel.isSignUp) R.string.signIn else R.string.createAccount),
                    style = MaterialTheme.typography.h5,
                    color = primary
                )
            }


        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "v ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.h5,
            color = darkGrey
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
