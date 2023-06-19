package com.call_blocker.app.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.call_blocker.adstv.ui.element.Title
import com.call_blocker.adstv.ui.primaryDimens
import com.call_blocker.app.R
import kotlinx.coroutines.delay


@Composable
fun Banner(mViewModel: SplashViewModel) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.app_logo),
        contentDescription = null,
        modifier = Modifier.requiredSize(
            size = primaryDimens * 5
        )
    )

    Divider(modifier = Modifier.height(primaryDimens), color = Color.Transparent)

    Title(text = stringResource(id = R.string.app_name))

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        delay(1000L)
        mViewModel.initMe(context)
    }
}
