package com.call_blocker.app.ui.screens.intro

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.call_blocker.app.R
import com.call_blocker.app.ui.*

@Composable
private fun IntroItem(
    title: String,
    label: String,
    desc: String,
    btnTitle: String,
    onClick: () -> Unit
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Text(text = title, style = MaterialTheme.typography.h1, textAlign = TextAlign.Center)

    Text(text = label, style = MaterialTheme.typography.h3, textAlign = TextAlign.Center)

    Divider(Modifier.height(primaryDimens * 2), color = Color.Transparent)

    Text(text = desc, style = bold16Sp, textAlign = TextAlign.Center)

    Divider(Modifier.height(primaryDimens), color = Color.Transparent)

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(100f),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonBackground,
            disabledBackgroundColor = disabledButton
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(horizontal = 5.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = btnTitle,
            style = MaterialTheme.typography.h5,
            color = buttonTextColor
        )
    }
}

@Composable
fun IntroScreen(mViewModel: SplashViewModel) {
    val isPermissionsGranted by mViewModel.isPermissionGranted.collectAsState()
    val isAppDefault by mViewModel.isAppDefault.collectAsState()

    LaunchedEffect(isAppDefault) {
        Log.e("IntroScreen", "isAppDefault = $isAppDefault")
    }

    var isWelcomeScreen: Boolean by remember {
        mutableStateOf(true)
    }

    Crossfade(targetState = isWelcomeScreen) {
        if (it)
            Welcome(mViewModel = mViewModel) {
                isWelcomeScreen = false
            }
        else {
            if (!isPermissionsGranted)
                Permissions(mViewModel = mViewModel)
            else
                if (!isAppDefault)
                    AppAsDefault(mViewModel = mViewModel)
        }
    }

}

@Composable
private fun Welcome(mViewModel: SplashViewModel, onNextClick: () -> Unit) = IntroItem(
    title = stringResource(id = R.string.intro_welcome_title),
    label = stringResource(id = R.string.intro_welcome_label),
    desc = stringResource(id = R.string.intro_welcome_to_desc),
    btnTitle = stringResource(id = R.string.intro_welcome_next_btn_title),
    onClick = onNextClick
)

@Composable
private fun Permissions(mViewModel: SplashViewModel) {
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { pers ->
            val all = pers.all {
                it.value
            }
            mViewModel.isPermissionGranted.tryEmit(all)
        }

    IntroItem(
        title = stringResource(id = R.string.intro_permissions_title),
        label = stringResource(id = R.string.intro_permissions_label),
        desc = stringResource(id = R.string.intro_welcome_to_desc),
        btnTitle = stringResource(id = R.string.intro_permissions_next_btn_title)
    ) {
        requestPermissionLauncher.launch(PERMISSIONS_REQUIRED)
    }
}

@Composable
private fun AppAsDefault(mViewModel: SplashViewModel) {
    val context = LocalContext.current
    val requestAppAsDefaultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            mViewModel.isAppDefault.tryEmit(it.resultCode == Activity.RESULT_OK)
        }


    IntroItem(
        title = stringResource(id = R.string.intro_app_as_default_title),
        label = stringResource(id = R.string.intro_app_as_default_label),
        desc = stringResource(id = R.string.intro_app_as_default_to_desc),
        btnTitle = stringResource(id = R.string.intro_app_as_default_next_btn_title)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)

            if (!roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS))
                return@IntroItem
            if (roleManager.isRoleHeld(RoleManager.ROLE_SMS))
                return@IntroItem

            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            requestAppAsDefaultLauncher.launch(intent)
            context.startActivity(
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).putExtra(
                    Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    context.packageName
                )
            )
        } else {
            requestAppAsDefaultLauncher.launch(
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            )
        }
    }
}