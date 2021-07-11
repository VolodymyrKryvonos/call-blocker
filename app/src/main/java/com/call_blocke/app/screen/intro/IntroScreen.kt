package com.call_blocke.app.screen.intro

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.call_blocke.app.R
import com.call_blocke.app.screen.PERMISSIONS_REQUIRED
import com.call_blocke.app.screen.SplashViewModel
import com.rokobit.adstv.ui.element.Button
import com.rokobit.adstv.ui.element.Label
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens

@Composable
private fun IntroItem(mViewModel: SplashViewModel,
                      title: String,
                      label: String,
                      desc: String,
                      btnTitle: String,
                      onClick: () -> Unit)  = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Title(text = title, contentAlignment = TextAlign.Center)

    Label(text = label, contentAlignment = TextAlign.Center)

    Divider(Modifier.height(primaryDimens * 4), color = Color.Transparent)

    Text(text = desc, contentAlignment = TextAlign.Center)

    Divider(Modifier.height(primaryDimens * 2), color = Color.Transparent)

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { pers ->
            //requestPermission(context = context)
            val all = pers.all { it.value }

            mViewModel.isPermissionGranted.postValue(all)
        }

    val requestAppAsDefaultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            mViewModel.isAppDefault.postValue(it.resultCode == Activity.RESULT_OK)
        }

    val context = LocalContext.current

    Button(title = btnTitle, onClick = onClick)
}

@Composable
fun IntroScreen(mViewModel: SplashViewModel) {
    val isPermissionsGranted by mViewModel.isPermissionGranted.observeAsState(initial = false)
    val isAppDefault by mViewModel.isAppDefault.observeAsState(initial = false)

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
                AppAsDefault(mViewModel = mViewModel)
        }
    }

}

@Composable
private fun Welcome(mViewModel: SplashViewModel, onNextClick: () -> Unit) = IntroItem(
    mViewModel = mViewModel,
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
            //requestPermission(context = context)
            val all = pers.all { it.value }

            mViewModel.isPermissionGranted.postValue(all)
        }

    IntroItem(
        mViewModel = mViewModel,
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
    val requestAppAsDefaultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            mViewModel.isAppDefault.postValue(it.resultCode == Activity.RESULT_OK)
        }

    val context = LocalContext.current

    IntroItem(
        mViewModel = mViewModel,
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
        } else {
            requestAppAsDefaultLauncher.launch(
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            )
        }
    }
}