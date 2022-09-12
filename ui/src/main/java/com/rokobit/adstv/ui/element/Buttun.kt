package com.rokobit.adstv.ui.element

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rokobit.adstv.ui.*

@Composable
fun Button(
    title: String,
    isEnable: Boolean = true,
    modifier: Modifier = Modifier.defaultMinSize(minWidth = primaryDimens * 10),
    isProgress: State<Boolean> = remember {
        mutableStateOf(false)
    },
    onClick: () -> Unit
) {
    Crossfade(targetState = isProgress.value) {
        if (it) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            OutlinedButton(
                onClick = onClick,
                enabled = isEnable,
                shape = RoundedCornerShape(20),
                border = BorderStroke(1.dp, secondaryColor),
                elevation = ButtonDefaults.elevation(),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.copy(alpha = if (isEnable) 1f else 0.5f)
                ),
                contentPadding = PaddingValues(secondaryDimens),
                modifier = modifier
            ) {
                TextNormal(
                    text = title,
                    color = secondaryColor.copy(alpha = if (isEnable) 1f else 0.5f),
                    contentAlignment = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TextButton(
    title: String,
    isEnable: Boolean = true,
    onClick: () -> Unit,
) {

    androidx.compose.material.TextButton(onClick = onClick, enabled = isEnable) {
        Text(text = title, color = accentColor)
    }

    /*OutlinedButton(
        onClick = onClick,
        enabled = isEnable,
        shape = RoundedCornerShape(35),
        border = BorderStroke(3.dp, accentColor),
        elevation = ButtonDefaults.elevation(),
        colors = ButtonDefaults.outlinedButtonColors(
            *//* backgroundColor = primaryColor.copy(alpha = if (isEnable) if (isFocus.value) 1f else 0.8f else 0.5f),
             contentColor = secondaryColor,
             disabledContentColor = accentColor.copy(alpha = 0.5f)*//*
        ),
        contentPadding = PaddingValues(primaryDimens),
        modifier = Modifier.defaultMinSize(minWidth = primaryDimens * 10)
    ) {
        TextNormal(text = title, color = accentColor, contentAlignment = TextAlign.Center)
    }*/
}