package com.call_blocker.app.new_ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.call_blocker.app.new_ui.primary
import com.call_blocker.app.new_ui.textColor


@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    onClose: () -> Unit = {},
    content: @Composable () -> Unit = {},
    closeable: Boolean = false
) = Box(modifier = modifier
    .background(Color(0f, 0f, 0f, 0.5f))
    .clickable(
        interactionSource = MutableInteractionSource(),
        indication = null
    ) { if (closeable) onClose() }) {
    Card(
        shape = RoundedCornerShape(15.dp),
        backgroundColor = Color.White,
        elevation = 1.dp,
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.8f)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, style = MaterialTheme.typography.h3, color = textColor)
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable { onClose() }, tint = primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.h5, color = textColor)
            content()
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}