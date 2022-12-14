package com.rokobit.adstv.ui.element

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.rokobit.adstv.ui.mainFont
import com.rokobit.adstv.ui.primaryColor

@Composable
fun Title(text: String, contentAlignment: TextAlign = TextAlign.Start, modifier: Modifier = Modifier) = Text(
    text = text,
    fontSize = 32.sp,
    fontFamily = mainFont,
    color = primaryColor,
    textAlign = contentAlignment,
    modifier = modifier
    //modifier = Modifier.animateContentSize()
)

@Composable
fun Label(
    text: String,
    color: Color = primaryColor,
    contentAlignment: TextAlign = TextAlign.Start
) = Text(
    text = text,
    fontSize = 24.sp,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
)

@Composable
fun Text(
    text: String,
    color: Color = primaryColor,
    contentAlignment: TextAlign = TextAlign.Start
) = Text(
    text = text,
    fontSize = 18.sp,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
    //modifier = Modifier.fillMaxWidth()
)

@Composable
fun TextNormal(
    text: String,
    color: Color = primaryColor,
    contentAlignment: TextAlign = TextAlign.Start,
    fontSize: TextUnit = 14.sp
) = Text(
    text = text,
    fontSize = fontSize,
    fontFamily = mainFont,
    color = color,
    textAlign = contentAlignment
)

@ExperimentalComposeUiApi
@Composable
fun Field(
    hint: String,
    isError: Boolean = false,
    isEnable: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    value: MutableState<String> = remember {
        mutableStateOf("")
    },
    autofillTypes: List<AutofillType> = emptyList(),
    onValueChange: (String) -> Unit = {
        value.value = it
        //isError.value = false
    },
    keyboardType: KeyboardType = KeyboardType.Text,
    icon: ImageVector = Icons.Filled.Email
) {
    val shape = RoundedCornerShape(20)

    OutlinedTextField(
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = true,
            keyboardType = keyboardType
        ),
        value = value.value,
        onValueChange = onValueChange,
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
            fontFamily = mainFont,
        ),
        shape = shape,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .autofill(
                autofillTypes = autofillTypes,
                onFill = { value.value = it },
            ),
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        visualTransformation = visualTransformation,
        isError = isError
    )
}

@Composable
@Preview
fun PhoneNumberInputField(
    hint: String = "",
    isError: Boolean = false,
    isEnable: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Phone,
    icon: ImageVector = Icons.Filled.Email,
    value: MutableState<String> = remember {
        mutableStateOf("")
    },
    onValueChange: (String) -> Unit = {
        value.value = it
    },
) {
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = true,
            keyboardType = keyboardType
        ),
        value = value.value,
        onValueChange = onValueChange,
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
            fontFamily = mainFont,
        ),
        shape = RoundedCornerShape(20),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        isError = isError
    )
}

@ExperimentalComposeUiApi
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}