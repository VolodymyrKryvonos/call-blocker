package com.rokobit.adstv.ui.element

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokobit.adstv.ui.accentColor
import com.rokobit.adstv.ui.mainFont
import com.rokobit.adstv.ui.primaryColor
import com.rokobit.adstv.ui.secondaryColor

@Composable
fun Title(text: String) = Text(
    text = text,
    fontSize = 32.sp,
    fontFamily = mainFont,
    color = primaryColor,
    //modifier = Modifier.animateContentSize()
)

@Composable
fun Label(text: String,
          color: Color = primaryColor,
          contentAlignment: TextAlign = TextAlign.Start) = Text(
    text = text,
    fontSize = 24.sp,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
)

@Composable
fun Text(text: String,
         color: Color = primaryColor,
         contentAlignment: TextAlign = TextAlign.Start) = Text(
    text = text,
    fontSize = 18.sp,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
    //modifier = Modifier.fillMaxWidth()
)

@Composable
fun TextNormal(text: String,
         color: Color = primaryColor,
         contentAlignment: TextAlign = TextAlign.Start) = Text(
    text = text,
    fontSize = 14.sp,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
)

@Composable
fun Field(
    hint: String,
    isError: Boolean = false,
    isEnable: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    value: MutableState<String> = remember {
        mutableStateOf("")
    }
) {
    val shape = RoundedCornerShape(20)

    OutlinedTextField(
        value = value.value,
        onValueChange = {
            value.value = it
            //isError.value = false
        },
        label = {
            Text(
                text = hint,
                fontFamily = mainFont,
                fontSize = 14.sp
            )
        },
        enabled = isEnable,
        textStyle = TextStyle(
            fontSize = 18.sp,
            fontFamily = mainFont
        ),
        shape = shape,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null
            )
        },
        visualTransformation = visualTransformation,
        isError = isError
    )
}