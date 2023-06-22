package com.call_blocker.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primary = Color(0xFF436BCD)
val background = Color(0x66D5EDFF)
val itemBackground = Color(0xFFFFFFFF)
val itemStroke = Color(0xFFD5EDFF)
val headerBackground = Color(0xFFFFFFFF)
val buttonBackground = primary
val turnOnButtonBackground = Color(0xFFC2B9B9)
val tabTextColor = Color(0xFF49454F)
val tabIconColor = Color(0xFF1C1B1F)
val uniqueIdTextColor = Color(0xFF2F2C2C)
val textColor = Color(0xFF000000)
val error = Color(0xFFF44336)
val buttonTextColor = Color(0xFFFFFFFF)
val spacerColor = Color(0xFFD5EDFF)
val dividerColor = Color(0xFFD5EDFF)
val disabledButton = Color(0x1F1C1B1F)

val gray3 = Color(0xFF828282)
val gray6 = Color(0xFFF2F2F2)
val darkGrey = Color(0xFF454545)
val backgroundError = Color(0x33EB5757)
val backgroundConnected = Color(0x3327AE60)
val tintError = Color(0xFFEB5757)
val tintConnected = Color(0xFF27AE60)
val lightBlue = Color(0xFFD5EDFF)

@Composable
fun Them(content: @Composable () -> Unit) = MaterialTheme(
    typography = Typography,
    colors = lightColors().copy(
        background = background,
        onBackground = itemBackground,
        surface = itemBackground,
        onSurface = turnOnButtonBackground,
        primary = background,
        primaryVariant = headerBackground,
        onPrimary = itemBackground,
        secondary = buttonBackground,
        error = error,
        onError = error
    ),
    content = content,
)

val buttonShape = RoundedCornerShape(100f)