package com.call_blocke.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.screen.SplashScreen
import com.call_blocke.app.screen.SplashViewModel
import com.call_blocke.app.screen.auth.AuthScreen
import com.call_blocke.app.screen.auth.AuthViewModel
import com.call_blocke.app.worker_manager.ServiceWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.rokobit.adstv.ui.Them
import com.rokobit.adstv.ui.backgroundBrush

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Them {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = backgroundBrush
                        )
                ) {
                    val isUserAuth by SmsBlockerDatabase
                        .userIsAuthLiveData
                        .observeAsState(initial = SmsBlockerDatabase.userToken != null)

                    if (isUserAuth)
                        MainView()
                    else
                        AuthView()

                    val isSimChanged by SmsBlockerDatabase
                        .onSimChanged
                        .observeAsState(initial = SmsBlockerDatabase.isSimChanged)

                    if (isSimChanged) {
                        ServiceWorker.stop(this@MainActivity)
                        Column {
                            Box(modifier = Modifier.weight(1f))

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.sim_slot_change_desc),
                                        color = Color.White
                                    )
                                    Button(onClick = {
                                        SmsBlockerDatabase.isSimChanged = false
                                    }) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        checkBatteryOptimizations()
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @Composable
    fun AuthView(mViewModel: AuthViewModel = viewModel()) {
        AuthScreen(mViewModel)
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun MainView() {
        SplashScreen(splashViewModel)
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)
    }

    private fun checkBatteryOptimizations() {
        val name = resources.getString(R.string.app_name)
        if (!isIgnoringBatteryOptimizations(this)) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.turn_of_optimization)
                .setMessage("Battery optimization -> All apps -> $name -> Don't optimize")
                .setPositiveButton(R.string.ok) { _, _ ->
                    Toast.makeText(
                        applicationContext,
                        "Battery optimization -> All apps -> $name -> Don't optimize",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                }.show()

        }
    }
}
