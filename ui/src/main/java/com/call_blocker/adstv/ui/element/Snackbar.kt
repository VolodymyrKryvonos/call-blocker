package com.call_blocker.adstv.ui.element

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Snackbar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Snackbar(text: String) {
    var show by remember {
        mutableStateOf(true)
    }
    if (show) {
        Snackbar(
            backgroundColor = Color.Black,
            modifier = Modifier
                .padding(8.dp)
        ) { androidx.compose.material.Text(text = text, style = TextStyle(color = Color.White)) }
    }
    LaunchedEffect(key1 = 3, block = {
        delay(3000)
        show = false
    })
}