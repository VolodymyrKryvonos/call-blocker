package com.call_blocke.app.screen.sim_info

import android.content.ContentResolver
import android.telephony.SubscriptionInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.call_blocke.app.screen.refresh_full.RefreshViewModel
import com.call_blocke.rest_work_imp.FullSimInfoModel
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor

@ExperimentalMaterialApi
@Composable
fun SimInfoScreen(mViewModel: RefreshViewModel = viewModel()) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(primaryDimens)
) {
    val context = LocalContext.current

    mViewModel.simsInfo()
    Title(text = stringResource(id = R.string.sim_info_title))

    val resolver: ContentResolver = context.contentResolver

    Spacer(modifier = Modifier.height(24.dp))

    val sims by mViewModel.simInfoState.collectAsState(initial = null)
    if (sims == null)
        CircularProgressIndicator()
    else {
        for ((index, fullSimInfoModel) in sims!!.withIndex()) {

            if (index == 0) {
                mViewModel.firstSim(context = context)?.let {
                    SimInfoCard(info = it, data = fullSimInfoModel)
                }
            }

            Spacer(modifier = Modifier.height(primaryDimens))

            if (index == 1) {
                mViewModel.secondSim(context = context)?.let {
                    SimInfoCard(info = it, data = fullSimInfoModel)
                }
            }

        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun SimInfoCard(info: SubscriptionInfo, data: FullSimInfoModel) = Card(
    modifier = Modifier
        .fillMaxWidth(),
    shape = RoundedCornerShape(15),
    backgroundColor = secondaryColor,
    elevation = 6.dp,
    onClick = {},
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(primaryDimens)) {
        Row {
           Text(text = "Operator:", modifier = Modifier.weight(1f))
           Text(text = info.carrierName.toString())
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(text = "IMSI:", modifier = Modifier.weight(1f))
            Text(text = info.number ?: "unknown")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(text = "Connected on:", modifier = Modifier.weight(1f))
            Text(text = data.simDate)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(text = "Sent sms", modifier = Modifier.weight(1f))
            Text(text = "${data.simDelivered} SMS of ${data.simPerDay} today")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(text = "Sim id", modifier = Modifier.weight(1f))
            Text(text = "${info.iccId}")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}