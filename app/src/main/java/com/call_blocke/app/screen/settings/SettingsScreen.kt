package com.call_blocke.app.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Snackbar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.Navigation
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import com.rokobit.adstv.ui.*
import com.rokobit.adstv.ui.element.*
import kotlinx.coroutines.delay
import java.io.File


@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = viewModel()) {
    Column(modifier = Modifier.padding(primaryDimens)) {

        val isSuccessUpdated by viewModel.onSuccessUpdated.observeAsState(false)
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = scrollState)
        ) {
            Title(text = stringResource(id = R.string.settings_title))
            Label(text = stringResource(id = R.string.settings_set_sms))
            SmsLimitFields(navController, viewModel)
            Divider(
                modifier = Modifier.height(5.dp),
                color = Color.Transparent
            )
            LogsButtons(viewModel)

            Divider(
                modifier = Modifier.height(10.dp),
                color = Color.Transparent
            )

            Profile()

        }

        AnimatedVisibility(
            visible = isSuccessUpdated,
            enter = slideInVertically(
                initialOffsetY = { -40 }
            ),
            exit = slideOutVertically()
        ) {
            Snackbar(backgroundColor = primaryColor) {
                TextNormal(
                    text = stringResource(id = R.string.settings_on_success_updated),
                    color = secondaryColor
                )
            }
        }

        if (isSuccessUpdated) {
            LaunchedEffect(key1 = Unit) {
                delay(2500L)
                viewModel.onSuccessUpdated.postValue(false)
            }
        }
    }
}


@Composable
fun Profile() {
    val profile = SmsBlockerDatabase.profile

    Row {
        Text(text = stringResource(id = R.string.delay_sms_send))
        Text(text = profile?.delaySmsSend?.toString() ?: "3")
    }
    if (profile?.isKeepAlive == true) {
        Row {
            Text(text = stringResource(id = R.string.ping_interval))
            Text(text = profile.keepAliveDelay.toString())
        }
    }
    if (profile?.isConnected == true) {
        Row {
            Text(text = stringResource(id = R.string.check_connection_interval))
            Text(text = profile.delayIsConnected.toString())
        }
    }
}

@Composable
fun LogsButtons(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    Button(
        title = "Send logs",
        modifier = Modifier.fillMaxWidth(),
        isEnable = true,
        fontSize = 16.sp,
        onClick = {
            context.startActivity(getLogsShareIntent(context))
        }
    )
    if (BuildConfig.DEBUG) {
        Divider(
            modifier = Modifier.height(5.dp),
            color = Color.Transparent
        )
        Button(
            title = "Clear logs",
            modifier = Modifier.fillMaxWidth(),
            isEnable = true,
            fontSize = 16.sp,
            onClick = {
                clearLogs(context)
                viewModel.onSuccessUpdated.postValue(true)
            }
        )
    }
}


@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SmsLimitFields(navController: NavHostController, viewModel: SettingsViewModel) {
    val context = LocalContext.current

    var isFirstSimSmsPerDayError: Boolean by remember {
        mutableStateOf(false)
    }
    var isFirstSimSmsPerMonthError: Boolean by remember {
        mutableStateOf(false)
    }
    var isSecondSimSmsPerDayError: Boolean by remember {
        mutableStateOf(false)
    }
    var isSecondSimSmsPerMonthError: Boolean by remember {
        mutableStateOf(false)
    }
    val isFirstSimAllow = remember {
        viewModel.isFirstSimAllow(context)
    }
    val isSecondSimAllow = remember {
        viewModel.isSecondSimAllow(context)
    }
    val firstSimSmsDayLimit = remember {
        mutableStateOf(viewModel.firstSimSlotSmsCountPerDay.toString())
    }
    val secondSimSmsDayLimit = remember {
        mutableStateOf(viewModel.secondSimSlotSmsCountPerDay.toString())
    }
    val firstSimSmsMonthLimit = remember {
        mutableStateOf(viewModel.firstSimSlotSmsCountPerMonth.toString())
    }
    val secondSimSmsMonthLimit = remember {
        mutableStateOf(viewModel.secondSimSlotSmsCountPerMonth.toString())
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.onLoading.observeAsState(false)

    val isFirstSimNeedVerification =
        VerificationInfoStateHolder.checkIsFirstSimVerified().collectAsState(
            initial = false
        )
    val isSecondSimNeedVerification =
        VerificationInfoStateHolder.checkIsSecondSimVerified().collectAsState(
            initial = false
        )

    if (isFirstSimAllow) {
        if (isFirstSimNeedVerification.value) {
            Label(
                text = stringResource(id = R.string.first_sim_card_not_verified),
                color = errorColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                title = stringResource(id = R.string.verify),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp
            ) {
                navController.navigate(Navigation.SimInfoScreen.destination)
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {

            Label(
                text = stringResource(id = R.string.first_sim_card)
            )
            SimLimitFields(
                firstSimSmsDayLimit,
                firstSimSmsMonthLimit,
                isLoading,
                isFirstSimSmsPerDayError,
                isFirstSimSmsPerMonthError
            )
        }
    }
    if (isSecondSimAllow) {
        if (isSecondSimNeedVerification.value) {
            Label(
                text = stringResource(id = R.string.second_sim_card_not_verified),
                color = errorColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                title = stringResource(id = R.string.verify),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp
            ) {
                navController.navigate(Navigation.SimInfoScreen.destination)
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Label(
                text = stringResource(id = R.string.second_sim_card)
            )
            SimLimitFields(
                secondSimSmsDayLimit,
                secondSimSmsMonthLimit,
                isLoading,
                isSecondSimSmsPerDayError,
                isSecondSimSmsPerMonthError
            )
        }
    }
    Divider(
        modifier = Modifier.height(primaryDimens),
        color = Color.Transparent
    )

    Button(
        title = stringResource(id = R.string.settings_set_sms_per_day_button),
        modifier = Modifier.fillMaxWidth(),
        isEnable = !isFirstSimNeedVerification.value || !isSecondSimNeedVerification.value,
        fontSize = 16.sp,
        isProgress = viewModel.onLoading.observeAsState(false)
    ) {

        val firstSimSmsPerDay = firstSimSmsDayLimit.value.toIntOrNull() ?: -1
        val secondSimSmsPerDay = secondSimSmsDayLimit.value.toIntOrNull() ?: -1
        val firstSimSmsPerMonth = firstSimSmsMonthLimit.value.toIntOrNull() ?: -1
        val secondSimSmsPerMonth = secondSimSmsMonthLimit.value.toIntOrNull() ?: -1

        isFirstSimSmsPerDayError = firstSimSmsPerDay < 0
        isFirstSimSmsPerMonthError = firstSimSmsPerMonth < 0
        if (isSecondSimAllow) {
            isSecondSimSmsPerDayError = secondSimSmsPerDay < 0
            isSecondSimSmsPerMonthError = secondSimSmsPerMonth < 0
        }
        if (isFirstSimSmsPerDayError ||
            isSecondSimSmsPerDayError ||
            isSecondSimSmsPerMonthError ||
            isFirstSimSmsPerMonthError
        )
            return@Button

        keyboardController?.hide()

        viewModel.updateSmsPerDay(
            context = context,
            smsPerDaySimFirst = firstSimSmsPerDay,
            smsPerDaySimSecond = secondSimSmsPerDay,
            smsPerMonthSimFirst = firstSimSmsPerMonth,
            smsPerMonthSimSecond = secondSimSmsPerMonth
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SimLimitFields(
    simSmsDayLimit: MutableState<String>,
    simSmsMonthLimit: MutableState<String>,
    isLoading: Boolean,
    isSimSmsPerDayError: Boolean,
    isSimSmsPerMonthError: Boolean
) {
    Field(
        hint = stringResource(id = R.string.sms_count_per_day),
        value = simSmsDayLimit,
        isEnable = !isLoading,
        isError = isSimSmsPerDayError,
        onValueChange = {
            if ((it.toIntOrNull() ?: 0) > 1000 || it.length > 4) {
                simSmsDayLimit.value = 1000.toString()
            } else {
                simSmsDayLimit.value = it
            }
        },
        keyboardType = KeyboardType.Number
    )
    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
    Field(
        hint = stringResource(id = R.string.sms_count_per_month),
        value = simSmsMonthLimit,
        isEnable = !isLoading,
        isError = isSimSmsPerMonthError,
        onValueChange = {
            if (it.length > 5) {
                simSmsMonthLimit.value = 5000.toString()
            } else {
                simSmsMonthLimit.value = it
            }
        },
        keyboardType = KeyboardType.Number
    )
}


fun getLogsShareIntent(context: Context): Intent {
    val files = arrayListOf<Uri>()
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "text/plain"
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles()
        if (filesList != null) {
            for (file in filesList) {
                try {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    files.add(contentUri)
                } catch (e: Exception) {
                    Log.e("getUriForFileException", e.message.toString())
                }

            }
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
    return Intent.createChooser(sendIntent, null)
}

private fun clearLogs(context: Context) {
    val directory = File(context.filesDir.absolutePath + "/Log")
    val filesList = directory.listFiles()
    if (filesList != null) {
        for (file in filesList) {
            Log.e("FileName Path", file.name + " " + file.absolutePath)
            file.delete()
            if (file.exists()) {
                if (!file.canonicalFile.delete()) {
                    context.deleteFile(file.name)
                }
            }
        }
    }
}
