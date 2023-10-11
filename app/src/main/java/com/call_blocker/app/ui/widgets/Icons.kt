package com.call_blocker.app.ui.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.call_blocker.app.ui.lightBlue

@Composable
fun IconWithBackground(
    @DrawableRes iconDrawable: Int,
    contentDescription: String = "",
    tint: Color = Color.Black,
    background: Color = lightBlue,
    paddingHorizontal: Dp = 12.dp,
    paddingVertical: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(16.dp)
) = Box(
    Modifier
        .clip(shape)
        .background(background)
) {
    Icon(
        modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        painter = painterResource(
            id = iconDrawable
        ),
        tint = tint,
        contentDescription = contentDescription
    )
}