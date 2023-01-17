package com.call_blocke.app.screen.sim_info

import android.telephony.SubscriptionInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.call_blocke.app.screen.main.OnLifecycleEvent
import com.call_blocke.app.screen.refresh_full.RefreshViewModel
import com.call_blocke.app.screen.refresh_full.SnackbarVisibility
import com.call_blocke.app.worker_manager.SendingSMSWorker
import com.call_blocke.db.AutoValidationResult
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.ValidationState
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.example.common.CountryCodeExtractor
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import kotlinx.coroutines.delay

data class VerifyingSim(
    val subscriptionInfo: SubscriptionInfo,
    val simValidationInfo: SimValidationInfo
)

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@Composable
fun SimInfoScreen(viewModel: RefreshViewModel = viewModel()) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    val firstSimValidationState = SmsBlockerDatabase.firstSimValidationState
        .collectAsState()
    val secondSimValidationState = SmsBlockerDatabase.secondSimValidationState
        .collectAsState()
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val verifyingSim: MutableState<VerifyingSim?> = remember {
        mutableStateOf(null)
    }
    val firstSimValidationInfo = viewModel.firstSimValidationInfo.collectAsState()
    val secondSimValidationInfo = viewModel.secondSimValidationInfo.collectAsState()
    val validationState = viewModel.validationState.collectAsState(Resource.None)

    val snackbarVisibility = viewModel.snackbarVisibility.collectAsState()
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.simsInfo()
                viewModel.checkSimCards(context)
            }
            else -> {}
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(primaryDimens)
    ) {
        Title(text = stringResource(id = R.string.sim_info_title))

        Spacer(modifier = Modifier.height(24.dp))

        val sims by viewModel.simInfoState.collectAsState(initial = null)
        if (sims == null)
            CircularProgressIndicator()
        else {
            for ((index, fullSimInfoModel) in sims!!.withIndex()) {

                if (index == 0) {
                    viewModel.firstSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            phoneNumber = firstSimValidationInfo.value.number,
                            validationState = firstSimValidationState.value
                        ) {
                            openDialog.value = true
                            verifyingSim.value = VerifyingSim(it, firstSimValidationInfo.value)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(primaryDimens))

                if (index == 1) {
                    viewModel.secondSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            phoneNumber = secondSimValidationInfo.value.number,
                            validationState = secondSimValidationState.value
                        ) {
                            openDialog.value = true
                            verifyingSim.value = VerifyingSim(it, secondSimValidationInfo.value)
                        }
                    }
                }

            }
        }
    }
    if (openDialog.value && verifyingSim.value != null) {
        VerifyNumberDialog(
            viewModel = viewModel,
            verifyingSim = verifyingSim.value!!,
            modifier = Modifier.fillMaxSize(),
            validationState = validationState,
            onClose = {
                verifyingSim.value = null
                openDialog.value = false
            }
        )
    }

    AnimatedVisibility(
        modifier = Modifier.align(alignment = Alignment.BottomCenter),
        visible = snackbarVisibility.value == SnackbarVisibility.Visible,
        enter = slideInVertically(
            initialOffsetY = { -40 }
        ),
        exit = slideOutVertically()
    ) {
        Snackbar(backgroundColor = primaryColor) {
            TextNormal(
                text = stringResource(
                    id = when (validationState.value) {
                        is Resource.Error -> R.string.something_went_wrong
                        else -> R.string.process_verification
                    }
                ),
                color = secondaryColor
            )
        }
    }

    if (snackbarVisibility.value == SnackbarVisibility.Visible) {
        LaunchedEffect(key1 = Unit) {
            delay(2500L)
            viewModel.hideSnackbar()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerifyNumberDialog(
    viewModel: RefreshViewModel,
    verifyingSim: VerifyingSim,
    modifier: Modifier,
    validationState: State<Resource<Unit>>,
    onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumber = remember {
        mutableStateOf("+${CountryCodeExtractor.getCountryPhoneCode(verifyingSim.subscriptionInfo.iccId) ?: ""}")
    }
    val monthlyLimit = remember {
        mutableStateOf("")
    }
    var isCorrectNumber by remember {
        mutableStateOf(false)
    }
    if (validationState.value is Resource.Success) {
        onClose()
    }
    val context = LocalContext.current

    AlertDialog(
        modifier = modifier,
        title = stringResource(id = R.string.verifyPhoneNumber),
        onClose = onClose,
        content = {
            PhoneNumberInputField(
                hint = stringResource(id = R.string.phone_number),
                isError = !isCorrectNumber,
                onValueChange = {
                    phoneNumber.value = it
                    isCorrectNumber = isPhoneValid(it)
                },
                value = phoneNumber,
                keyboardType = KeyboardType.Phone,
                icon = Icons.Filled.Phone
            )
            Spacer(modifier = Modifier.height(10.dp))
            Field(
                hint = stringResource(id = R.string.monthly_sms_limit),
                value = monthlyLimit,
                onValueChange = {
                    if ((it.toIntOrNull() ?: 0) < 0 || it.length > 7) {
                        monthlyLimit.value = 5000.toString()
                    } else {
                        monthlyLimit.value = it
                    }
                },
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                title = stringResource(id = R.string.settings_set_sms_per_day_button),
                modifier = Modifier.fillMaxWidth(),
                isEnable = true,
                isProgress = mutableStateOf(validationState.value is Resource.Loading),
                fontSize = 16.sp
            ) {
                if (isCorrectNumber) {
                    if (!SendingSMSWorker.isRunning.value) {
                        SendingSMSWorker.start(context = context)
                    }
                    viewModel.showSnackbar()
                    viewModel.validatePhoneNumber(
                        phoneNumber.value.removePrefix("+"),
                        verifyingSim.subscriptionInfo.iccId,
                        monthlyLimit.value,
                        simSlot = verifyingSim.subscriptionInfo.simSlotIndex
                    )
                    if (verifyingSim.subscriptionInfo.simSlotIndex == 0) {
                        SmsBlockerDatabase.firstSimSlotValidationNumber = phoneNumber.value
                    } else {
                        SmsBlockerDatabase.secondSimSlotValidationNumber = phoneNumber.value
                    }
                    keyboardController?.hide()
                }
            }
            if (verifyingSim.simValidationInfo.status == SimValidationStatus.AUTO_VALIDATION) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    title = stringResource(id = R.string.auto_detect_number),
                    modifier = Modifier.fillMaxWidth(),
                    isEnable = true,
                    fontSize = 16.sp
                ) {
                    viewModel.showSnackbar()
                    viewModel.checkSimCard(
                        verifyingSim.subscriptionInfo.simSlotIndex,
                        context,
                        true
                    )
                    onClose()
                }
            }
        })
}

@ExperimentalMaterialApi
@Composable
private fun SimInfoCard(
    info: SubscriptionInfo,
    data: FullSimInfoModel,
    phoneNumber: String,
    validationState: ValidationState,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth(),
    shape = RoundedCornerShape(15),
    backgroundColor = getBackgroundColor(
        if (data.simSlot == 0) {
            SmsBlockerDatabase.simFirstAutoValidationResult
        } else {
            SmsBlockerDatabase.simSecondAutoValidationResult
        }, validationState
    ),
    enabled = isManualValidationEnabled(validationState),
    elevation = 6.dp,
    onClick = onClick,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(primaryDimens)
    ) {
        Row {
            Text(text = "Operator:", modifier = Modifier.weight(1f))
            Text(text = info.carrierName.toString())
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "IMSI:", modifier = Modifier.weight(1f))
            Text(text = phoneNumber)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "Connected on:", modifier = Modifier.weight(1f))
            Text(text = data.simDate)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "Sent SMS:", modifier = Modifier.weight(1f))
            Text(text = "${data.simDelivered} SMS of ${data.simPerDay} today")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "Sim card ID:", modifier = Modifier.weight(1f))
            Text(text = info.iccId)
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (validationState == ValidationState.FAILED) {
            Text(
                text = stringResource(id = R.string.verification_failed_check_entered_number),
                color = Color.Red
            )
        }
    }
}

fun isManualValidationEnabled(
    validationState: ValidationState
): Boolean {
    return validationState == ValidationState.INVALID ||
            validationState == ValidationState.FAILED ||
            validationState == ValidationState.AUTO_VALIDATION
}

fun getBackgroundColor(
    validationResult: AutoValidationResult,
    validationState: ValidationState
): Color {
    return when (validationState) {
        ValidationState.INVALID -> Color.Red
        ValidationState.AUTO_VALIDATION -> {
            if (validationResult == AutoValidationResult.FAILED) {
                Color.Red
            } else {
                Color.Gray
            }
        }
        ValidationState.PROCESSING, ValidationState.FAILED -> Color.Gray
        else -> secondaryColor
    }
}
