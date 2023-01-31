package com.call_blocke.app.screen.sim_info

import android.content.Context
import android.telephony.SubscriptionInfo
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.call_blocker.verification.domain.VerificationInfoStateHolder
import com.example.common.CountryCodeExtractor
import com.example.common.SimUtil
import com.rokobit.adstv.ui.backgroundColor
import com.rokobit.adstv.ui.element.*
import com.rokobit.adstv.ui.primaryDimens

@ExperimentalMaterialApi
@Composable
fun SimInfoScreen(viewModel: RefreshViewModel = viewModel()) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val verifyingSim: MutableState<SubscriptionInfo?> = remember {
        mutableStateOf(null)
    }
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.simsInfo(
                    SimUtil.firstSim(context)?.iccId,
                    SimUtil.secondSim(context)?.iccId
                )
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
                    val verificationInfo =
                        VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
                            .collectAsState()
                    Log.e("verificationInfo", verificationInfo.toString())
                    SimUtil.firstSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            phoneNumber = verificationInfo.value.phoneNumber,
                            isNeedVerification = verificationInfo.value.isNeedVerification(),
                            isVerificationInProgress = verificationInfo.value.isVerificationInProgress()
                        ) {
                            verifySimCard(
                                context,
                                verificationInfo.value.isAutoVerificationEnabled,
                                viewModel,
                                it,
                                openDialog,
                                verifyingSim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(primaryDimens))

                if (index == 1) {
                    val verificationInfo =
                        VerificationInfoStateHolder.getStateHolderBySimSlotIndex(index)
                            .collectAsState()
                    Log.e("verificationInfo", verificationInfo.toString())
                    SimUtil.secondSim(context = context)?.let {
                        SimInfoCard(
                            info = it,
                            data = fullSimInfoModel,
                            phoneNumber = verificationInfo.value.phoneNumber,
                            isNeedVerification = verificationInfo.value.isNeedVerification(),
                            isVerificationInProgress = verificationInfo.value.isVerificationInProgress()
                        ) {
                            verifySimCard(
                                context,
                                verificationInfo.value.isAutoVerificationEnabled,
                                viewModel,
                                it,
                                openDialog,
                                verifyingSim
                            )
                        }
                    }
                }

            }
        }
    }
    if (openDialog.value && verifyingSim.value != null) {
        VerifyNumberDialog(
            viewModel = viewModel,
            subscriptionInfo = verifyingSim.value!!,
            modifier = Modifier.fillMaxSize(),
            onClose = {
                verifyingSim.value = null
                openDialog.value = false
            }
        )
    }
}

private fun verifySimCard(
    context: Context,
    isAutoVerificationEnabled: Boolean,
    viewModel: RefreshViewModel,
    subscriptionInfo: SubscriptionInfo,
    openDialog: MutableState<Boolean>,
    verifyingSim: MutableState<SubscriptionInfo?>
) {
    if (!SendingSMSWorker.isRunning.value) {
        SendingSMSWorker.start(context = context)
    }
    if (isAutoVerificationEnabled) {
        viewModel.verifySimCard(
            simId = subscriptionInfo.iccId,
            simSlot = subscriptionInfo.simSlotIndex
        )
    } else {
        openDialog.value = true
        verifyingSim.value = subscriptionInfo
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerifyNumberDialog(
    viewModel: RefreshViewModel,
    subscriptionInfo: SubscriptionInfo,
    modifier: Modifier,
    onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneNumber = remember {
        mutableStateOf("+${CountryCodeExtractor.getCountryPhoneCode(subscriptionInfo.iccId) ?: ""}")
    }
    var isCorrectNumber by remember {
        mutableStateOf(false)
    }

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
            Button(
                title = stringResource(id = R.string.verify),
                modifier = Modifier.fillMaxWidth(),
                isEnable = true,
                fontSize = 16.sp
            ) {
                if (isCorrectNumber) {
                    viewModel.verifySimCard(
                        phoneNumber.value,
                        subscriptionInfo.iccId,
                        subscriptionInfo.simSlotIndex
                    )
                    keyboardController?.hide()
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
    phoneNumber: String?,
    isNeedVerification: Boolean = false,
    isVerificationInProgress: Boolean = false,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth(),
    shape = RoundedCornerShape(15),
    backgroundColor = backgroundColor,
    elevation = 6.dp,
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
            Text(text = phoneNumber ?: "unknown")
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
        if (isNeedVerification || isVerificationInProgress) {
            val progress = mutableStateOf(isVerificationInProgress)
            Button(
                title = stringResource(id = R.string.verify),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                isProgress = progress
            ) {
                onClick()
            }
        }

    }
}
