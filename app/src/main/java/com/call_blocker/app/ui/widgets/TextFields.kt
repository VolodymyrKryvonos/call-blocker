package com.call_blocker.app.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.call_blocker.adstv.ui.element.autofill
import com.call_blocker.app.R
import com.call_blocker.app.ui.primary
import com.call_blocker.app.ui.tabIconColor
import com.call_blocker.app.ui.tabTextColor
import com.call_blocker.app.ui.tintError

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

