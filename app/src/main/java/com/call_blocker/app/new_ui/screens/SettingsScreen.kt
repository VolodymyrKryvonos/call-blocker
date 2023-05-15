package com.call_blocker.app.new_ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.call_blocker.adstv.ui.primaryDimens
import com.call_blocker.app.BuildConfig
import com.call_blocker.app.R
import com.call_blocker.app.new_ui.buttonBackground
import com.call_blocker.app.new_ui.buttonTextColor
import com.call_blocker.app.new_ui.darkGrey
import com.call_blocker.app.new_ui.dividerColor
import com.call_blocker.app.new_ui.itemBackground
import com.call_blocker.app.new_ui.medium24Sp
import com.call_blocker.app.new_ui.screens.home_screen.Container
import com.call_blocker.app.new_ui.widgets.ToggleButton
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.ussd_sender.UssdService
import java.io.File

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val isUssdCommandOn = SmsBlockerDatabase.ussdCommandState.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        Box(
            Modifier
                .background(itemBackground)
                .fillMaxWidth()
                .padding(vertical = 22.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.h2,
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(dividerColor)
        )
        Spacer(modifier = Modifier.height(60.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.requiredSize(
                    size = primaryDimens * 4
                )
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = stringResource(id = R.string.app_name), style = medium24Sp)
            Container {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.enable_ussd))
                    ToggleButton(isUssdCommandOn.value) {
                        SmsBlockerDatabase.isUssdCommandOn = it
                        if (it) {
                            UssdService.enableAccessibilityPermission(
                                context
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            if (BuildConfig.logs) {
                Logs(context)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "v ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.h5,
                color = darkGrey
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Logs(context: Context) {
    Button(
        onClick = {
            viewLog(context)
        },
        shape = RoundedCornerShape(100f),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
    ) {
        Text(
            text = stringResource(id = R.string.viewLogs),
            style = MaterialTheme.typography.h5,
            color = buttonTextColor
        )
    }
    Button(
        onClick = {
            sendLogs(context)
        },
        shape = RoundedCornerShape(100f),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
    ) {
        Text(
            text = stringResource(id = R.string.sendLogs),
            style = MaterialTheme.typography.h5,
            color = buttonTextColor
        )
    }
    Button(
        onClick = {
            clearLogs(context)
        },
        shape = RoundedCornerShape(100f),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonBackground)
    ) {
        Text(
            text = stringResource(id = R.string.clearLogs),
            style = MaterialTheme.typography.h5,
            color = buttonTextColor
        )
    }
}


private fun sendLogs(context: Context) {
    val files = arrayListOf<Uri>()
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "text/plain"
        val directory = File(context.filesDir.absolutePath + "/Log")
        val filesList = directory.listFiles()
        if (filesList != null) {
            for (file in filesList) {
                try {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    files.add(contentUri)
                } catch (e: Exception) {
                    Log.e("getUriForFileException", e.message.toString())
                }

            }
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}


private fun viewLog(context: Context) {
    val directory = File(context.filesDir.absolutePath + "/Log")
    val filesList = directory.listFiles() ?: return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    intent.setDataAndType(
        FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".fileprovider",
            filesList.last()
        ), "text/html"
    )
    context.startActivity(intent)
}

private fun clearLogs(context: Context) {
    val directory = File(context.filesDir.absolutePath + "/Log")
    val filesList = directory.listFiles()
    if (filesList != null) {
        for (file in filesList) {
            Log.e("FileName Path", file.name + " " + file.absolutePath)
            file.delete()
            if (file.exists()) {
                if (!file.canonicalFile.delete()) {
                    context.deleteFile(file.name)
                }
            }
        }
    }
}