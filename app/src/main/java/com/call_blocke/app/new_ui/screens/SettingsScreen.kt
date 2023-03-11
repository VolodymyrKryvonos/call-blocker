package com.call_blocke.app.new_ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.call_blocke.app.BuildConfig
import com.call_blocke.app.R
import com.call_blocke.app.new_ui.buttonBackground
import com.call_blocke.app.new_ui.buttonTextColor
import com.call_blocke.app.new_ui.dividerColor
import com.call_blocke.app.new_ui.textColor
import java.io.File

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = stringResource(id = R.string.sim_card_info),
            style = MaterialTheme.typography.h2
        )
        Spacer(modifier = Modifier.height(22.dp))
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(dividerColor)
        )
        Spacer(modifier = Modifier.height(50.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.h5,
                color = textColor
            )
        }
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