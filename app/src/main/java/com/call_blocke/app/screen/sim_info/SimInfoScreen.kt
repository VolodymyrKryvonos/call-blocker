package com.call_blocke.app.screen.sim_info

import android.telephony.SubscriptionInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import com.call_blocke.app.worker_manager.SendingSMSWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocke.rest_work_imp.model.Resource
import com.call_blocke.rest_work_imp.model.SimValidationInfo
import com.call_blocke.rest_work_imp.model.SimValidationStatus
import com.rokobit.adstv.ui.element.AlertDialog
import com.rokobit.adstv.ui.element.Button
import com.rokobit.adstv.ui.element.Field
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@Composable
fun SimInfoScreen(mViewModel: RefreshViewModel = viewModel()) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    val isValidationCompleted = SmsBlockerDatabase.isValidationCompleted.collectAsState(false)
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val verifyingSim: MutableState<SubscriptionInfo?> = remember {
        mutableStateOf(null)
    }
    val firstSimValidationInfo = mViewModel.firstSimValidationInfo.collectAsState()
    val secondSimValidationInfo = mViewModel.secondSimValidationInfo.collectAsState()
    val validationState = mViewModel.validationState.collectAsState(Resource.None)
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                mViewModel.simsInfo()
                mViewModel.checkSimCards(context)
            }
            else -> {}
        }
    }
    val coroutineScope = rememberCoroutineScope()
    if (isValidationCompleted.value) {
        mViewModel.checkSimCards(context)
        coroutineScope.launch {
            SmsBlockerDatabase.isValidationCompleted.emit(false)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(primaryDimens)
    ) {
        Title(text = stringResource(id = R.string.sim_info_title))

        Spacer(modifier = Modifier.height(24.dp))

        val sims by mViewModel.simInfoState.collectAsState(initial = null)
        if (sims == null)
            CircularProgressIndicator()
        else {
            for ((index, fullSimInfoModel) in sims!!.withIndex()) {

                if (index == 0) {
                    mViewModel.firstSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            validationInfo = firstSimValidationInfo.value
                        ) {
                            openDialog.value = true
                            verifyingSim.value = it
                        }
                    }
                }

                Spacer(modifier = Modifier.height(primaryDimens))

                if (index == 1) {
                    mViewModel.secondSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            validationInfo = secondSimValidationInfo.value
                        ) {
                            openDialog.value = true
                            verifyingSim.value = it
                        }
                    }
                }

            }
        }
    }
    if (openDialog.value && verifyingSim.value != null) {
        VerifyNumberDialog(
            viewModel = mViewModel,
            simID = verifyingSim.value!!.iccId,
            simSlot = verifyingSim.value!!.simSlotIndex,
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
        visible = validationState.value is Resource.Error || validationState.value is Resource.Success,
        enter = slideInVertically(
            initialOffsetY = { -40 }
        ),
        exit = slideOutVertically()
    ) {
        Snackbar(backgroundColor = primaryColor) {
            TextNormal(
                text = stringResource(
                    id = if (validationState.value is Resource.Error) {
                        R.string.something_went_wrong
                    } else {
                        R.string.process_verification
                    }
                ),
                color = secondaryColor
            )
        }
    }

    if (validationState.value is Resource.Error || validationState.value is Resource.Success) {
        LaunchedEffect(key1 = Unit) {
            delay(2500L)
            mViewModel.validationState.emit(Resource.None)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerifyNumberDialog(
    viewModel: RefreshViewModel,
    simID: String,
    simSlot: Int,
    modifier: Modifier,
    validationState: State<Resource<Unit>>,
    onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumber = remember {
        mutableStateOf("")
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
            Field(
                hint = stringResource(id = R.string.phone_number),
                value = phoneNumber,
                isError = !isCorrectNumber,
                onValueChange = {
                    phoneNumber.value = it
                    isCorrectNumber = isPhoneValid(it)
                },
                keyboardType = KeyboardType.Number,
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
                    viewModel.validatePhoneNumber(
                        phoneNumber.value,
                        simID,
                        monthlyLimit.value,
                        simSlot = simSlot
                    )
                    if (simSlot == 0) {
                        SmsBlockerDatabase.firstSimSlotValidationNumber = phoneNumber.value
                    } else {
                        SmsBlockerDatabase.secondSimSlotValidationNumber = phoneNumber.value
                    }
                    keyboardController?.hide()
                }
            }
        })
}

@ExperimentalMaterialApi
@Composable
private fun SimInfoCard(
    info: SubscriptionInfo,
    data: FullSimInfoModel,
    validationInfo: SimValidationInfo,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth(),
    shape = RoundedCornerShape(15),
    backgroundColor = when (validationInfo.status) {
        SimValidationStatus.INVALID -> Color.Red
        SimValidationStatus.PROCESSING -> Color.Gray
        else -> secondaryColor
    },
    enabled = validationInfo.status == SimValidationStatus.INVALID,
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
            Text(text = validationInfo.number)
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
    }
}