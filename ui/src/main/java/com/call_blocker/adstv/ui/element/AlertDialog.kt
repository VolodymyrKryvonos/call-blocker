package com.call_blocker.adstv.ui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
                TextNormal(text = title)
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable { onClose() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextNormal(text = message)
            content()
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
