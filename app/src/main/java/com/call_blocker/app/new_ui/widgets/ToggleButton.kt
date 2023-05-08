package com.call_blocker.app.new_ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.material.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.call_blocker.app.R

@Composable
fun ToggleButton(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    IconToggleButton(checked = isChecked, onCheckedChange = {
        onCheckedChange(it)
    }) {
        val icon =
            painterResource(id = if (isChecked) R.drawable.toggle_on else R.drawable.toggle_off)

        Image(icon, contentDescription = "Localized description")
    }
}
