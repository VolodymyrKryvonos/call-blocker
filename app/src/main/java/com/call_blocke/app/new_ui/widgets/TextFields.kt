package com.call_blocke.app.new_ui.widgets

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.call_blocke.app.new_ui.tabIconColor
import com.call_blocke.app.new_ui.tabTextColor
import com.rokobit.adstv.ui.element.autofill

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    hint: String = "",
    isError: Boolean = false,
    isEnable: Boolean = true,
    value: MutableState<String> = remember {
        mutableStateOf("")
    },
    autofillTypes: List<AutofillType> = emptyList(),
    onValueChange: (String) -> Unit = {
        value.value = it
    },
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    val shape = RoundedCornerShape(10f)
    OutlinedTextField(
        value = value.value,
        onValueChange = onValueChange,
        modifier = modifier.autofill(
            autofillTypes = autofillTypes,
            onFill = {
                value.value = it
            }
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = true,
            keyboardType = keyboardType
        ),
        label = { Text(text = hint, style = MaterialTheme.typography.body2) },
        singleLine = singleLine,
        enabled = isEnable,
        shape = shape,
        textStyle = MaterialTheme.typography.body1,
        colors = TextFieldDefaults.textFieldColors(
            textColor = tabIconColor,
            backgroundColor = Color.Transparent,
            cursorColor = tabTextColor,
            focusedLabelColor = tabTextColor,
            unfocusedLabelColor = tabTextColor,
            unfocusedIndicatorColor = tabTextColor,
            focusedIndicatorColor = tabTextColor
        ),
        isError = isError
    )
}
