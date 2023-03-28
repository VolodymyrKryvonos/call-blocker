package com.call_blocke.app.new_ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.call_blocke.app.R
import com.call_blocke.app.new_ui.primary
import com.call_blocke.app.new_ui.tabIconColor
import com.call_blocke.app.new_ui.tabTextColor
import com.call_blocke.app.new_ui.tintError
import com.rokobit.adstv.ui.element.autofill

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    hint: String = "",
    isError: Boolean = false,
    isEnable: Boolean = true,
    value: String = "",
    autofillTypes: List<AutofillType> = emptyList(),
    onValueChange: (String) -> Unit = {

    },
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(10f),

    ) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.autofill(
            autofillTypes = autofillTypes,
            onFill = {
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
            cursorColor = primary,
            focusedLabelColor = primary,
            unfocusedLabelColor = tabTextColor,
            unfocusedIndicatorColor = tabTextColor,
            focusedIndicatorColor = primary
        ),
        isError = isError
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextFieldWithErrorMsg(
    modifier: Modifier = Modifier,
    hint: String = "",
    isError: Boolean = false,
    isEnable: Boolean = true,
    value: String = "",
    autofillTypes: List<AutofillType> = emptyList(),
    onValueChange: (String) -> Unit = {

    },
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(10f),
    errorMsg: String = ""
) {
    Column(modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            keyboardType = keyboardType,
            hint = hint,
            singleLine = singleLine,
            isEnable = isEnable,
            shape = shape,
            autofillTypes = autofillTypes,
            isError = isError,
        )
        if (isError)
            Text(text = errorMsg, style = MaterialTheme.typography.body2, color = tintError)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordField(
    modifier: Modifier = Modifier,
    hint: String = "",
    isError: Boolean = false,
    isEnable: Boolean = true,
    value: String = "",
    autofillTypes: List<AutofillType> = emptyList(),
    onValueChange: (String) -> Unit = {

    },
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(10f)
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.autofill(
            autofillTypes = autofillTypes,
            onFill = {
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
            cursorColor = primary,
            focusedLabelColor = primary,
            unfocusedLabelColor = tabTextColor,
            unfocusedIndicatorColor = tabTextColor,
            focusedIndicatorColor = primary
        ),
        isError = isError,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.password_invisible else R.drawable.password_visible),
                    description,
                    tint = primary
                )
            }
        }
    )
}

