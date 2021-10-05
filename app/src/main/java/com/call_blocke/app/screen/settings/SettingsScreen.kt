package com.call_blocke.app.screen.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens
import kotlinx.coroutines.delay

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SettingsScreen(mViewModel: SettingsViewModel = viewModel()) =
    Column(modifier = Modifier.padding(primaryDimens)) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val isLoading by mViewModel.onLoading.observeAsState(false)
    val isSuccessUpdated by mViewModel.onSuccessUpdated.observeAsState(false)

    val context = LocalContext.current

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
    }

    AnimatedVisibility(
        visible = isSuccessUpdated,
        enter = slideInVertically(
            initialOffsetY = { -40 }
        ),
        exit = slideOutVertically()
    ) {
        Snackbar(backgroundColor = primaryColor) {
            TextNormal(text = stringResource(id = R.string.settings_on_success_updated), color = secondaryColor)
        }
    }

    if (isSuccessUpdated) {
        LaunchedEffect(key1 = Unit) {
            delay(2500L)
            mViewModel.onSuccessUpdated.postValue(false)
        }
    }
}