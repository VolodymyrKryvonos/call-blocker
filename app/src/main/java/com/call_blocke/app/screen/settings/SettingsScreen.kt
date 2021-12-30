package com.call_blocke.app.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.call_blocke.db.Preference
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens
import kotlinx.coroutines.delay
import java.io.File


@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SettingsScreen(mViewModel: SettingsViewModel = viewModel()) =
    Column(modifier = Modifier.padding(primaryDimens)) {
        val keyboardController = LocalSoftwareKeyboardController.current

        val isLoading by mViewModel.onLoading.observeAsState(false)
        val isSuccessUpdated by mViewModel.onSuccessUpdated.observeAsState(false)
        val context = LocalContext.current

        val preference = Preference(context)
        var selected by remember { mutableStateOf(preference.ipType) }
        val radioGroupOptions = listOf("Test", "Production", "Custom")
        Column(modifier = Modifier.weight(1f)) {
            Title(text = stringResource(id = R.string.settings_title))
            Label(text = stringResource(id = R.string.settings_set_sms_per_day))

            var isFirstFieldError: Boolean by remember {
                mutableStateOf(false)
            }

            var isSecondFieldError: Boolean by remember {
                mutableStateOf(false)
            }

            Divider(
                modifier = Modifier.height(primaryDimens),
                color = Color.Transparent
            )

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

            Field(
                hint = stringResource(id = R.string.settings_first_sim_slot_sms_count_hint),
                value = smsOneCountValue,
                isEnable = !isLoading && isFirstSimAllow,
                isError = isFirstFieldError
            )
            Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
            Field(
                hint = stringResource(id = R.string.settings_secound_sim_slot_sms_count_hint),
                value = smsTwoCountValue,
                isEnable = !isLoading && isSecondSimAllow,
                isError = isSecondFieldError
            )

            Divider(
                modifier = Modifier.height(primaryDimens),
                color = Color.Transparent
            )

            Button(
                title = stringResource(id = R.string.settings_set_sms_per_day_button),
                modifier = Modifier.fillMaxWidth(),
                isEnable = true,
                isProgress = mViewModel.onLoading.observeAsState(false)
            ) {

                val forFist = kotlin.run {
                    try {
                        smsOneCountValue.value.toInt()
                    } catch (e: Exception) {
                        -1
                    }
                }

                val forSecond = kotlin.run {
                    try {
                        smsTwoCountValue.value.toInt()
                    } catch (e: Exception) {
                        -1
                    }
                }

                isFirstFieldError = forFist <= 0
                if (isSecondSimAllow)
                    isSecondFieldError = forSecond <= 0

                if (isFirstFieldError || isSecondFieldError)
                    return@Button

                keyboardController?.hide()

                mViewModel.updateSmsPerDay(
                    forSimFirst = forFist,
                    forSimSecond = forSecond
                )
            }
            Divider(
                modifier = Modifier.height(primaryDimens),
                color = Color.Transparent
            )

            Button(
                title = "Send logs",
                modifier = Modifier.fillMaxWidth(),
                isEnable = true,
                onClick = {
                    context.startActivity(getLogsShareIntent(context))
                }
            )
            //Test only
            Divider(
                modifier = Modifier.height(primaryDimens),
                color = Color.Transparent
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                val onSelectedChange = { text: String ->
                    selected = text
                    if (text != "Custom") {
                        preference.ipType = text
                    }
                }
                Column {
                    radioGroupOptions.forEach { text ->
                        Row(Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (text == selected),
                                onClick = { onSelectedChange(text) }
                            )
                            .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = (text == selected),
                                onClick = { onSelectedChange(text) },
                                colors = RadioButtonDefaults.colors(
                                    unselectedColor = Color.White,
                                    selectedColor = Color.White
                                )
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.body1.merge(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
            if (selected == "Custom") {
                CustomIpInput(preference)
            }
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

fun getLogsShareIntent(context: Context): Intent {
    val files = arrayListOf<Uri>()
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "text/plain"
        val directory = File(context.filesDir.absolutePath + "/Log")
        for (file in directory.listFiles()) {
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
    return Intent.createChooser(sendIntent, null)
}

@Composable
fun CustomIpInput(preference: Preference) {
    var ip by remember { mutableStateOf(preference.customIp) }
    val invalidInput = isIpValid(ip)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = ip,
            onValueChange = { ip = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        val textColor = MaterialTheme.colors.error
        if (!invalidInput)
            Text(
                textAlign = TextAlign.Center,
                text = "Ip does not match the template",
                style = MaterialTheme.typography.caption.copy(color = textColor),
            )
        if (invalidInput) {
            Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
            Button(
                title = "Confirm",
                modifier = Modifier.fillMaxWidth(),
                isEnable = true,
            ) {
                preference.ipType = "Custom"
                preference.customIp = ip
            }
        }
    }
}
