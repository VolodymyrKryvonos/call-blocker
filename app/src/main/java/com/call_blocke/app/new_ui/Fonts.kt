package com.call_blocke.app.new_ui

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rokobit.adstv.ui.R

val roboto400 = FontFamily(fonts = arrayListOf(Font(R.font.roboto_regular, FontWeight.Normal)))
val roboto500 = FontFamily(fonts = arrayListOf(Font(R.font.roboto_medium, FontWeight.Medium)))
val roboto700 = FontFamily(fonts = arrayListOf(Font(R.font.roboto_medium, FontWeight.Bold)))

val simInfoCaptionStyle = TextStyle(
    fontFamily = roboto400,
    fontSize = 14.sp,
    lineHeight = 16.sp
)

val simInfoDataStyle = TextStyle(
    fontFamily = roboto700,
    fontSize = 14.sp,
    lineHeight = 16.sp
)

val tabFont = TextStyle(
    fontFamily = roboto500,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

val Typography = Typography(
    h1 = TextStyle(
        fontFamily = roboto500,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    h2 = TextStyle(
        fontFamily = roboto500,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    h3 = TextStyle(
        fontFamily = roboto500,
        fontSize = 22.sp,
        lineHeight = 26.sp
    ),
    h4 = TextStyle(
        fontFamily = roboto500,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    h5 = TextStyle(
        fontFamily = roboto500,
        fontSize = 14.sp,
        lineHeight = 16.sp
    ),
    button = TextStyle(
        fontFamily = roboto500,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    overline = TextStyle(
        fontFamily = roboto400,
        fontSize = 18.sp,
        lineHeight = 21.sp
    ),
    body1 = TextStyle(
        fontFamily = roboto400,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    body2 = TextStyle(
        fontFamily = roboto400,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
)