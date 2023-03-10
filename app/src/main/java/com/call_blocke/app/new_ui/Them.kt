package com.call_blocke.app.new_ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val background = Color(0xFFF5F5F5)
val itemBackground = Color(0xFFD9D9D9)
val headerBackground = Color(0xFFDCDCDC)
val buttonBackground = Color(0xFF6750A4)
val turnOnButtonBackground = Color(0xFFC2B9B9)
val tabTextColor = Color(0xFF49454F)
val tabIconColor = Color(0xFF1C1B1F)
val uniqueIdTextColor = Color(0xFF2F2C2C)
val textFieldBorder = Color(0xFF79747E)
val textColor = Color(0xFF000000)
val error = Color(0xFFF44336)
val buttonTextColor = Color(0xFFFFFFFF)
val spacerColor = Color(0xFFFFFFFF)
val dividerColor = Color(0xFFE7E0EC)

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