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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens
import kotlinx.coroutines.delay
import java.io.File


@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
fun SettingsScreen(mViewModel: SettingsViewModel = viewModel()) =
    Column(modifier = Modifier.padding(primaryDimens)) {

        val isSuccessUpdated by mViewModel.onSuccessUpdated.observeAsState(false)
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = scrollState)
        ) {
            Title(text = stringResource(id = R.string.settings_title))
            Label(text = stringResource(id = R.string.settings_set_sms_per_day))
            Divider(
                modifier = Modifier.height(primaryDimens),
                color = Color.Transparent
            )
            SmsLimitFields(mViewModel)
            Divider(
                modifier = Modifier.height(5.dp),
                color = Color.Transparent
            )
            LogsButtons(mViewModel)

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
                mViewModel.onSuccessUpdated.postValue(false)
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
//    Row {
//        Text(text = stringResource(id = R.string.socket_ip))
//        Text(text = profile?.socketIp ?: "")
//    }
//    Row {
//        Text(text = stringResource(id = R.string.base_url))
//        Text(text = profile?.url ?: "")
//    }

}

@Composable
fun LogsButtons(mViewModel: SettingsViewModel) {
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
            mViewModel.onSuccessUpdated.postValue(true)
        }
    )
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SmsLimitFields(mViewModel: SettingsViewModel) {
    val context = LocalContext.current
    var isFirstFieldError: Boolean by remember {
        mutableStateOf(false)
    }

    var isSecondFieldError: Boolean by remember {
        mutableStateOf(false)
    }
    val isFirstSimAllow = remember {
        mViewModel.isFirstSimAllow(context)
    }

    val isSecondSimAllow = remember {
        mViewModel.isSecondSimAllow(context)
    }

    val smsOneCountValue = remember {
        mutableStateOf(mViewModel.fistSimSlotSmsCount.toString())
    }

    val smsTwoCountValue = remember {
        mutableStateOf(
            if (isSecondSimAllow)
                mViewModel.secondSimSlotSmsCount.toString()
            else
                "0"
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val isLoading by mViewModel.onLoading.observeAsState(false)

    Field(
        hint = stringResource(id = R.string.settings_first_sim_slot_sms_count_hint),
        value = smsOneCountValue,
        isEnable = !isLoading && isFirstSimAllow,
        isError = isFirstFieldError,
        onValueChange = {
            if ((it.toIntOrNull() ?: 0) > 1000 || it.length > 4) {
                smsOneCountValue.value = 1000.toString()
            } else {
                smsOneCountValue.value = it
            }
        },
        keyboardType = KeyboardType.Number
    )
    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
    Field(
        hint = stringResource(id = R.string.settings_secound_sim_slot_sms_count_hint),
        value = smsTwoCountValue,
        isEnable = !isLoading && isSecondSimAllow,
        isError = isSecondFieldError,
        onValueChange = {
            if ((it.toIntOrNull() ?: 0) > 1000 || it.length > 4) {
                smsTwoCountValue.value = 1000.toString()
            } else {
                smsTwoCountValue.value = it
            }
        },
        keyboardType = KeyboardType.Number
    )

    Divider(
        modifier = Modifier.height(primaryDimens),
        color = Color.Transparent
    )

    Button(
        title = stringResource(id = R.string.settings_set_sms_per_day_button),
        modifier = Modifier.fillMaxWidth(),
        isEnable = true,
        fontSize = 16.sp,
        isProgress = mViewModel.onLoading.observeAsState(false)
    ) {

        val forFist = run {
            try {
                smsOneCountValue.value.toInt()
            } catch (e: Exception) {
                -1
            }
        }

        val forSecond = run {
            try {
                smsTwoCountValue.value.toInt()
            } catch (e: Exception) {
                -1
            }
        }

        isFirstFieldError = forFist < 0
        if (isSecondSimAllow)
            isSecondFieldError = forSecond < 0

        if (isFirstFieldError || isSecondFieldError)
            return@Button

        keyboardController?.hide()

        mViewModel.updateSmsPerDay(
            forSimFirst = forFist,
            forSimSecond = forSecond
        )
    }
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
            getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach {
                Log.e("getParcelableArrayListExtra", it.toString())
            }
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
