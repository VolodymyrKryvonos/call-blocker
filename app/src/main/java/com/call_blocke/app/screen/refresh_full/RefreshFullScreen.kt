package com.call_blocke.app.screen.refresh_full

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.call_blocke.app.screen.main.OnLifecycleEvent
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstv.ui.element.Label
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun RefreshScreen(mViewModel: RefreshViewModel = viewModel()) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(primaryDimens)
) {
    val context = LocalContext.current

    val sims by mViewModel.simInfoState.collectAsState(initial = null)

    val isLoading by mViewModel.onLoading.observeAsState(false)

    Title(text = stringResource(id = R.string.refresh_full_title))
    Label(text = stringResource(id = R.string.refresh_full_label))

    Spacer(modifier = Modifier.height(24.dp))

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SimUtil.firstSim(context)?.let {
                SimSlotBtn(
                    simName = it.carrierName.toString(),
                    simChanged = SmsBlockerDatabase.firstSimChanged,
                    simOutOfSms = sims?.firstOrNull { sim -> sim.simSlot == 0 }.let { sim ->
                        if (sim == null) {
                            false
                        } else
                            sim.simPerDay <= sim.simDelivered
                    }
                ) {
                    mViewModel.reset(0, context)
                }
            }

            Spacer(modifier = Modifier.width(primaryDimens))

            SimUtil.secondSim(context)?.let {
                SimSlotBtn(
                    simName = it.carrierName.toString(),
                    simChanged = SmsBlockerDatabase.secondSimChanged,
                    simOutOfSms = sims?.firstOrNull { sim -> sim.simSlot == 1 }.let { sim ->
                        if (sim == null) {
                            false
                        } else
                            sim.simPerDay <= sim.simDelivered
                    }
                ) {
                    mViewModel.reset(1, context)
                }
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                mViewModel.simsInfo(
                    SimUtil.firstSim(context)?.iccId,
                    SimUtil.secondSim(context)?.iccId
                )
            }
            else -> {}
        }
    }

}

@ExperimentalMaterialApi
@Composable
private fun SimSlotBtn(
    simName: String,
    simChanged: Boolean,
    simOutOfSms: Boolean = false,
    onClick: () -> Unit
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(primaryDimens),
    shape = RoundedCornerShape(15),
    backgroundColor = if (simOutOfSms) errorColor else secondaryColor,
    elevation = 6.dp,
    border = if (simChanged) BorderStroke(2.dp, errorColor) else null,
    onClick = { onClick() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(primaryDimens),
    ) {

        Image(
            imageVector = Icons.Filled.Call,
            contentDescription = null,
            modifier = Modifier.size(
                primaryDimens * 3
            )
        )

        Spacer(modifier = Modifier.height(primaryDimens / 2))

        Label(text = simName)
    }
}