package com.rokobit.adstv.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val primaryColor = Color.Black
val secondaryColor = Color.White
val accentColor = Color(73, 73, 73)
val backgroundColor = Color(247, 247, 247)
val errorColor = Color(237, 67, 55)

val primaryDimens by lazy { 24.dp }
val secondaryDimens by lazy { 16.dp }

val progressBarAsLabelWidth by lazy {
    430.dp
}

val progressBarAsLabelHeight by lazy {
    45.dp
}

@Composable
fun Them(content: @Composable () -> Unit) = MaterialTheme(
    colors = lightColors().copy(
        background = primaryColor,
        onBackground = backgroundColor,
        surface = primaryColor,
        onSurface = accentColor,
        primary = primaryColor,
        primaryVariant = primaryColor,
        onPrimary = secondaryColor,
        secondary = secondaryColor,
        secondaryVariant = secondaryColor,
    ),
    content = content,
)

val mainFont = FontFamily(fonts = arrayListOf(Font(R.font.roboto_medium, FontWeight.Normal)))

val backgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(250, 252, 255),
        Color(238, 245, 250)
    )
)