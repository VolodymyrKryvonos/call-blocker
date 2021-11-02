package com.call_blocke.app.screen.refresh_full

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
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

    val isLoading by mViewModel.onLoading.observeAsState(false)
    
    Title(text = stringResource(id = R.string.refresh_full_title))
    Label(text = stringResource(id = R.string.refresh_full_label))
    
    Spacer(modifier = Modifier.height(24.dp))
    
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            mViewModel.firstSim(context)?.let {
                SimSlotBtn(simName = it.carrierName.toString()) {
                    mViewModel.reset(0)
                }
            }

            Spacer(modifier = Modifier.width(primaryDimens))

            mViewModel.secondSim(context)?.let {
                SimSlotBtn(simName = it.carrierName.toString()) {
                    mViewModel.reset(1)
                }
            }
        }
    }
    
}

@ExperimentalMaterialApi
@Composable
private fun SimSlotBtn(simName: String, onClick: () -> Unit) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(primaryDimens),
    shape = RoundedCornerShape(15),
    backgroundColor = secondaryColor,
    elevation = 6.dp,
    onClick = onClick,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(primaryDimens)
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